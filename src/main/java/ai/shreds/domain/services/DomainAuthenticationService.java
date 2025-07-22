package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainAccountEntity;
import ai.shreds.domain.entities.DomainAuthenticationSessionEntity;
import ai.shreds.domain.entities.DomainSecuritySettingsEntity;
import ai.shreds.domain.exceptions.DomainAccountNotActiveException;
import ai.shreds.domain.exceptions.DomainInvalidCredentialsException;
import ai.shreds.domain.ports.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class DomainAuthenticationService implements DomainInputPortAuthentication {

    private final DomainOutputPortAccountRepository accountRepository;
    private final DomainOutputPortSecurityRepository securityRepository;
    private final DomainOutputPortSessionRepository sessionRepository;
    private final DomainOutputPortCryptoService cryptoService;

    public DomainAuthenticationService(
            DomainOutputPortAccountRepository accountRepository,
            DomainOutputPortSecurityRepository securityRepository,
            DomainOutputPortSessionRepository sessionRepository,
            DomainOutputPortCryptoService cryptoService) {
        this.accountRepository = accountRepository;
        this.securityRepository = securityRepository;
        this.sessionRepository = sessionRepository;
        this.cryptoService = cryptoService;
    }

    @Override
    public DomainAccountEntity authenticateWithCredentials(String username, String password) {
        // Find account by username
        DomainAccountEntity account = accountRepository.findByUsername(username);
        if (account == null) {
            throw new DomainInvalidCredentialsException("Invalid credentials");
        }

        // Check if account is active
        if (!account.isActive()) {
            throw new DomainAccountNotActiveException(
                "Account is not active", 
                account.getAccountStatus().name()
            );
        }

        // Check if account is locked
        DomainSecuritySettingsEntity securitySettings = securityRepository.findByAccountId(account.getAccountId().toString());
        if (securitySettings != null && securitySettings.isAccountLocked()) {
            throw new DomainAccountNotActiveException(
                "Account is temporarily locked due to failed login attempts",
                "LOCKED"
            );
        }

        // Verify password
        if (!cryptoService.verifyPassword(password, account.getPasswordHash())) {
            // Don't record failed attempt here - let the application service handle it
            // in a separate transaction to avoid rollback issues
            throw new DomainInvalidCredentialsException("Invalid credentials");
        }

        return account;
    }

    @Override
    public boolean validateAccountStatus(String accountId) {
        DomainAccountEntity account = accountRepository.findById(accountId);
        if (account == null) {
            return false;
        }

        // Check if account is active
        if (!account.isActive()) {
            return false;
        }

        // Check if account is locked
        DomainSecuritySettingsEntity securitySettings = securityRepository.findByAccountId(accountId);
        if (securitySettings != null && securitySettings.isAccountLocked()) {
            return false;
        }

        return true;
    }

    @Override
    public void recordFailedLoginAttempt(String accountId) {
        DomainSecuritySettingsEntity securitySettings = securityRepository.findByAccountId(accountId);
        if (securitySettings == null) {
            // Create default security settings if not exists
            securitySettings = new DomainSecuritySettingsEntity(
                UUID.randomUUID(),
                UUID.fromString(accountId),
                false,
                null,
                0,
                null,
                Instant.now()
            );
        }

        // Record failed login
        securitySettings.recordFailedLogin();

        // Apply lockout policy if threshold exceeded (5 attempts)
        if (securitySettings.getLoginAttempts() >= 5) {
            // Lock account for 15 minutes initially
            securitySettings.lockAccount(java.time.Duration.ofMinutes(15));
        }

        // Save or update security settings
        if (securityRepository.findByAccountId(accountId) == null) {
            securityRepository.save(securitySettings);
        } else {
            securityRepository.update(securitySettings);
        }
    }

    @Override
    public void recordSuccessfulLogin(String accountId) {
        DomainAccountEntity account = accountRepository.findById(accountId);
        if (account != null) {
            account.recordLogin();
        }

        // Reset failed login attempts
        DomainSecuritySettingsEntity securitySettings = securityRepository.findByAccountId(accountId);
        if (securitySettings != null) {
            securitySettings.resetLoginAttempts();
            securityRepository.update(securitySettings);
        }
    }

    @Override
    public DomainAuthenticationSessionEntity createAuthenticationSession(
            String accountId, String accessToken, String refreshToken) {
        
        DomainAuthenticationSessionEntity session = new DomainAuthenticationSessionEntity(
            UUID.randomUUID(),
            UUID.fromString(accountId),
            cryptoService.hashToken(accessToken),
            cryptoService.hashToken(refreshToken),
            Instant.now().plus(1, ChronoUnit.HOURS), // 1 hour expiration
            Instant.now(),
            false
        );

        return sessionRepository.save(session);
    }
}