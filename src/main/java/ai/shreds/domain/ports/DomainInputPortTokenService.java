package ai.shreds.domain.ports;

import java.util.Map;

/**
 * Inbound port for token generation and validation in the domain layer.
 */
public interface DomainInputPortTokenService {

    /**
     * Generates a JWT access token with the provided account ID and claims.
     *
     * @param accountId the account identifier
     * @param claims the custom claims to include in the token
     * @return the generated access token string
     */
    String generateAccessToken(String accountId, Map<String, Object> claims);

    /**
     * Generates a new refresh token for the given account.
     *
     * @param accountId the account identifier
     * @return the generated refresh token string
     */
    String generateRefreshToken(String accountId);

    /**
     * Validates the provided token for signature, expiration, and revocation.
     *
     * @param token the token to validate
     * @return true if valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Extracts claims from a valid token.
     *
     * @param token the token to parse
     * @return a map of claims
     */
    Map<String, Object> extractClaims(String token);

    /**
     * Revokes the given token, preventing further use.
     *
     * @param token the token to revoke
     */
    void revokeToken(String token);
}
