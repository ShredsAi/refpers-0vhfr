package ai.shreds.application.services;

import ai.shreds.application.dtos.ApplicationSessionDTO;
import ai.shreds.application.ports.ApplicationAuthenticationInputPort;
import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import ai.shreds.application.services.ApplicationJwtService;
import ai.shreds.application.services.ApplicationFailedAuthenticationService;
import ai.shreds.domain.ports.DomainInputPortAuthentication;
import ai.shreds.domain.ports.DomainInputPortSecuritySettings;
import ai.shreds.domain.ports.DomainOutputPortAccountRepository;
import ai.shreds.shared.dtos.SharedLoginRequestDTO;
import ai.shreds.shared.dtos.SharedLoginResponseDTO;
import ai.shreds.shared.dtos.SharedAuthenticationAttemptEventDTO;
import ai.shreds.shared.dtos.SharedAuthenticationSuccessfulEventDTO;
import ai.shreds.shared.dtos.SharedUserLoggedOutEventDTO;
import ai.shreds.domain.entities.DomainAccountEntity;
import ai.shreds.domain.exceptions.DomainInvalidCredentialsException;
import ai.shreds.domain.exceptions.DomainAccountNotActiveException;
import ai.shreds.domain.exceptions.DomainMfaRequiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Application service implementing authentication operations.
 */
@Service
public class ApplicationAuthenticationService implements ApplicationAuthenticationInputPort {

    private final DomainInputPortAuthentication domainAuthenticationService;
    private final DomainInputPortSecuritySettings domainSecurityService;
    private final ApplicationEventPublisherOutputPort eventPublisher;
    private final ApplicationJwtService jwtService;
    private final DomainOutputPortAccountRepository accountRepository;
    private final ApplicationFailedAuthenticationService failedAuthenticationService;

    @Autowired
    public ApplicationAuthenticationService(
            DomainInputPortAuthentication domainAuthenticationService,
            DomainInputPortSecuritySettings domainSecurityService,
            ApplicationEventPublisherOutputPort eventPublisher,
            ApplicationJwtService jwtService,
            DomainOutputPortAccountRepository accountRepository,
            ApplicationFailedAuthenticationService failedAuthenticationService) {
        this.domainAuthenticationService = domainAuthenticationService;
        this.domainSecurityService = domainSecurityService;
        this.eventPublisher = eventPublisher;
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
        this.failedAuthenticationService = failedAuthenticationService;
    }

    @Override
    @Transactional
    public SharedLoginResponseDTO authenticateUser(SharedLoginRequestDTO request) {
        String accountId = null;
        try {
            // Try to get account ID from username for event publishing
            DomainAccountEntity existingAccount = accountRepository.findByUsername(request.getUsername());
            if (existingAccount != null) {
                accountId = existingAccount.getAccountId().toString();
            }

            // Publish authentication attempt event
            SharedAuthenticationAttemptEventDTO attemptEvent = new SharedAuthenticationAttemptEventDTO();
            attemptEvent.setAccountId(accountId != null ? accountId : request.getUsername()); // Fallback to username if no account found
            Map<String, Object> context = new HashMap<>();
            context.put("username", request.getUsername());
            context.put("timestamp", Instant.now().toString());
            attemptEvent.setAuthenticationContext(context);
            eventPublisher.publishEvent(attemptEvent);

            // Authenticate credentials through domain service
            DomainAccountEntity account = domainAuthenticationService.authenticateWithCredentials(
                    request.getUsername(), 
                    request.getPassword()
            );
            
            accountId = account.getAccountId().toString();

            // Validate account status
            validateAccountStatus(accountId);

            // Check if account is locked
            if (domainSecurityService.checkAccountLockStatus(accountId)) {
                failedAuthenticationService.recordFailedAttempt(accountId, "Account is locked");
                throw new DomainAccountNotActiveException("Account is locked", "LOCKED");
            }

            // Check if MFA is required
            var securitySettings = domainSecurityService.getSecuritySettings(accountId);
            if (securitySettings.isMfaEnabled()) {
                // Generate MFA challenge
                String challengeId = UUID.randomUUID().toString();
                
                SharedLoginResponseDTO response = SharedLoginResponseDTO.withMfaChallenge(challengeId);
                return response;
            }

            // Complete authentication if no MFA required
            ApplicationSessionDTO session = completeAuthentication(accountId);
            return SharedLoginResponseDTO.withTokens(session.getAccessToken(), session.getRefreshToken());

        } catch (DomainInvalidCredentialsException e) {
            // Handle failed authentication in separate transaction to avoid rollback
            if (accountId != null) {
                failedAuthenticationService.recordFailedAttempt(accountId, "Invalid credentials");
            }
            throw e;
        } catch (DomainAccountNotActiveException e) {
            // Handle failed authentication in separate transaction to avoid rollback
            if (accountId != null) {
                failedAuthenticationService.recordFailedAttempt(accountId, "Account not active: " + e.getAccountStatus());
            }
            throw e;
        } catch (DomainMfaRequiredException e) {
            // Return MFA challenge response
            return SharedLoginResponseDTO.withMfaChallenge(e.getChallengeId());
        }
    }

    @Override
    public void validateAccountStatus(String accountId) {
        try {
            boolean isValid = domainAuthenticationService.validateAccountStatus(accountId);
            if (!isValid) {
                throw new DomainAccountNotActiveException("Account status is not active", "INACTIVE");
            }
        } catch (Exception e) {
            throw new DomainAccountNotActiveException("Failed to validate account status: " + e.getMessage(), "UNKNOWN");
        }
    }

    @Override
    public void handleFailedAuthentication(String accountId, String reason) {
        // Delegate to the separate service
        failedAuthenticationService.recordFailedAttempt(accountId, reason);
    }

    @Override
    @Transactional
    public ApplicationSessionDTO completeAuthentication(String accountId) {
        try {
            // Record successful login
            domainAuthenticationService.recordSuccessfulLogin(accountId);

            // Generate tokens
            Map<String, Object> claims = new HashMap<>();
            claims.put("account_id", accountId);
            claims.put("scope", "openid profile email");
            
            String accessToken = jwtService.generateAccessToken(accountId, claims);
            String refreshToken = jwtService.generateRefreshToken(accountId);

            // Create session through domain service
            var session = domainAuthenticationService.createAuthenticationSession(accountId, accessToken, refreshToken);

            // Create session DTO
            ApplicationSessionDTO sessionDTO = new ApplicationSessionDTO(
                    session.getSessionId().toString(),
                    accountId,
                    accessToken,
                    refreshToken,
                    session.getExpiresAt().toEpochMilli()
            );

            // Publish successful authentication event
            SharedAuthenticationSuccessfulEventDTO successEvent = new SharedAuthenticationSuccessfulEventDTO();
            successEvent.setAccountId(accountId);
            successEvent.setSessionId(session.getSessionId().toString());
            successEvent.setLoginAt(Instant.now().toString());
            eventPublisher.publishEvent(successEvent);

            return sessionDTO;

        } catch (Exception e) {
            throw new RuntimeException("Failed to complete authentication: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void logout(String token) {
        try {
            // Extract account ID from token
            String accountId = jwtService.extractAccountId(token);
            
            // Revoke the token
            jwtService.revokeToken(token);

            // Publish logout event
            SharedUserLoggedOutEventDTO logoutEvent = new SharedUserLoggedOutEventDTO();
            logoutEvent.setAccountId(accountId);
            logoutEvent.setSessionId(UUID.randomUUID().toString()); // Could be extracted from token
            eventPublisher.publishEvent(logoutEvent);

        } catch (Exception e) {
            throw new RuntimeException("Failed to logout user: " + e.getMessage(), e);
        }
    }
}