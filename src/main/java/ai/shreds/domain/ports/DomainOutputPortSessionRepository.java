package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainAuthenticationSessionEntity;

/**
 * Outbound port for authentication session repository operations.
 */
public interface DomainOutputPortSessionRepository {

    /**
     * Save authentication session entity.
     */
    DomainAuthenticationSessionEntity save(DomainAuthenticationSessionEntity session);

    /**
     * Find session by refresh token hash.
     */
    DomainAuthenticationSessionEntity findByRefreshTokenHash(String tokenHash);

    /**
     * Find session by access token hash.
     */
    DomainAuthenticationSessionEntity findByAccessTokenHash(String tokenHash);

    /**
     * Revoke a session by session ID.
     */
    void revokeSession(String sessionId);

    /**
     * Revoke all sessions for an account.
     */
    void revokeAllSessionsForAccount(String accountId);
}
