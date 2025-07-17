package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedAuthenticationContextDTO;

public interface ApplicationOutputPortTokenCache {

    /**
     * Retrieve a cached authentication context by JWT hash.
     * @param jwtHash hashed JWT token
     * @return cached authentication context or null if not found or expired
     */
    SharedAuthenticationContextDTO getCachedAuthContext(String jwtHash);

    /**
     * Cache the authentication context for a JWT hash with given TTL in milliseconds.
     * @param jwtHash hashed JWT token
     * @param context authentication context to cache
     * @param ttl time-to-live in milliseconds
     */
    void cacheAuthContext(String jwtHash, SharedAuthenticationContextDTO context, long ttl);

    /**
     * Generate a secure hash for the given JWT token (e.g., SHA-256).
     * @param jwt raw JWT token
     * @return hashed representation
     */
    String generateTokenHash(String jwt);
}