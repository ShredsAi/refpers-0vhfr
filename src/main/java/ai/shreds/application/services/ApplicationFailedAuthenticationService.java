package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import ai.shreds.domain.ports.DomainInputPortAuthentication;
import ai.shreds.domain.ports.DomainInputPortSecuritySettings;
import ai.shreds.shared.dtos.SharedAuthenticationFailedEventDTO;
import ai.shreds.shared.dtos.SharedAccountLockedEventDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Separate service to handle failed authentication attempts in isolated transactions.
 * This ensures that failed attempt recording is committed even when the main
 * authentication transaction is rolled back.
 */
@Service
public class ApplicationFailedAuthenticationService {

    private final DomainInputPortAuthentication domainAuthenticationService;
    private final DomainInputPortSecuritySettings domainSecurityService;
    private final ApplicationEventPublisherOutputPort eventPublisher;

    public ApplicationFailedAuthenticationService(
            DomainInputPortAuthentication domainAuthenticationService,
            DomainInputPortSecuritySettings domainSecurityService,
            ApplicationEventPublisherOutputPort eventPublisher) {
        this.domainAuthenticationService = domainAuthenticationService;
        this.domainSecurityService = domainSecurityService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Records a failed authentication attempt in a separate transaction.
     * This method uses REQUIRES_NEW propagation to ensure that the failed
     * attempt is recorded even if the main authentication transaction rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(String accountId, String reason) {
        try {
            System.out.println("Recording failed attempt for account: " + accountId + ", reason: " + reason);
            
            // Record failed attempt in domain
            domainAuthenticationService.recordFailedLoginAttempt(accountId);

            // Get security settings to check attempts
            var securitySettings = domainSecurityService.getSecuritySettings(accountId);
            
            System.out.println("Current login attempts after recording: " + securitySettings.getLoginAttempts());
            
            // Publish failed authentication event
            SharedAuthenticationFailedEventDTO failedEvent = new SharedAuthenticationFailedEventDTO();
            failedEvent.setAccountId(accountId);
            failedEvent.setReason(reason);
            failedEvent.setAttempts(securitySettings.getLoginAttempts());
            eventPublisher.publishEvent(failedEvent);

            // Check if account should be locked (5 failed attempts)
            if (securitySettings.getLoginAttempts() >= 5) {
                System.out.println("Applying lockout policy for account: " + accountId);
                
                // Apply lockout policy
                domainSecurityService.applyLockoutPolicy(accountId, securitySettings.getLoginAttempts());
                
                // Publish account locked event
                SharedAccountLockedEventDTO lockedEvent = new SharedAccountLockedEventDTO();
                lockedEvent.setAccountId(accountId);
                lockedEvent.setLockReason("Too many failed login attempts");
                lockedEvent.setLockedUntil(Instant.now().plusSeconds(15 * 60).toString()); // 15 minutes
                eventPublisher.publishEvent(lockedEvent);
                
                System.out.println("Account locked until: " + lockedEvent.getLockedUntil());
            }
        } catch (Exception e) {
            // Log error but don't throw to avoid masking original authentication error
            System.err.println("Failed to record failed authentication attempt: " + e.getMessage());
            e.printStackTrace();
            throw e;  // Re-throw to ensure transaction rollback if there's a real issue
        }
    }
}