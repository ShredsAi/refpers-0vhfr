package ai.shreds.infrastructure.cache;

import java.time.Instant;

/**
 * Represents a cached JWKS (JSON Web Key Set) entry in Redis.
 * This class is used to store and retrieve cached JWKS data.
 */
public class InfrastructureCachedJwks {
    private String jwksJson;
    private Instant cachedAt;
    private Instant expiresAt;

    public InfrastructureCachedJwks() {
    }

    public InfrastructureCachedJwks(String jwksJson, Instant cachedAt, Instant expiresAt) {
        this.jwksJson = jwksJson;
        this.cachedAt = cachedAt;
        this.expiresAt = expiresAt;
    }

    public String getJwksJson() {
        return jwksJson;
    }

    public void setJwksJson(String jwksJson) {
        this.jwksJson = jwksJson;
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
        return "InfrastructureCachedJwks{" +
                "jwksJson='" + jwksJson + '\'' +
                ", cachedAt=" + cachedAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}