package ai.shreds.infrastructure.cache;

import java.time.Instant;

/**
 * Represents a cached authentication context entry in Redis.
 * This class is used to store and retrieve cached authentication contexts.
 */
public class InfrastructureCachedAuthContext {
    private String jwtHash;
    private String authContext;
    private Instant cachedAt;
    private Instant expiresAt;

    public InfrastructureCachedAuthContext() {
    }

    public InfrastructureCachedAuthContext(String jwtHash, String authContext, Instant cachedAt, Instant expiresAt) {
        this.jwtHash = jwtHash;
        this.authContext = authContext;
        this.cachedAt = cachedAt;
        this.expiresAt = expiresAt;
    }

    public String getJwtHash() {
        return jwtHash;
    }

    public void setJwtHash(String jwtHash) {
        this.jwtHash = jwtHash;
    }

    public String getAuthContext() {
        return authContext;
    }

    public void setAuthContext(String authContext) {
        this.authContext = authContext;
    }

    public Instant getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(Instant cachedAt) {
        this.cachedAt = cachedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    @Override
    public String toString() {
        return "InfrastructureCachedAuthContext{" +
                "jwtHash='" + jwtHash + '\'' +
                ", authContext='" + authContext + '\'' +
                ", cachedAt=" + cachedAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}