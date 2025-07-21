package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainAccountEntity;
import ai.shreds.domain.entities.DomainAuthenticationSessionEntity;

/**
 * Inbound port for authentication operations in the domain layer.
 */
public interface DomainInputPortAuthentication {

    /**
     * Authenticate user credentials and return the account entity.
     */
    DomainAccountEntity authenticateWithCredentials(String username, String password);

    /**
     * Check if the given account is in a valid status (e.g., ACTIVE).
     */
    boolean validateAccountStatus(String accountId);

    /**
     * Record a failed login attempt for the account.
     */
    void recordFailedLoginAttempt(String accountId);

    /**
     * Record a successful login for the account.
     */
    void recordSuccessfulLogin(String accountId);

    /**
     * Create a new authentication session for the account using provided tokens.
     */
    DomainAuthenticationSessionEntity createAuthenticationSession(
            String accountId,
            String accessToken,
            String refreshToken
    );
}
