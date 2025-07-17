package ai.shreds.domain.entities;

import java.time.Instant;

import ai.shreds.shared.value_objects.SharedValueSessionId;
import ai.shreds.shared.value_objects.SharedValueUserId;

/**
 * Entity representing a user session in the system.
 */
public class DomainEntityUserSession {
    private final SharedValueSessionId sessionId;
    private final SharedValueUserId userId;
    private final Instant createdAt;
    private Instant lastAccessedAt;
    private final Instant expiresAt;
    private boolean isActive;
    
    public DomainEntityUserSession(
            SharedValueSessionId sessionId,
            SharedValueUserId userId,
            Instant createdAt,
            Instant lastAccessedAt,
            Instant expiresAt,
            boolean isActive) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.lastAccessedAt = lastAccessedAt;
        this.expiresAt = expiresAt;
        this.isActive = isActive;
        validate();
    }
    
    private void validate() {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        // userId can be null for anonymous sessions
        if (createdAt == null) {
            throw new IllegalArgumentException("Created at cannot be null");
        }
        if (lastAccessedAt == null) {
            throw new IllegalArgumentException("Last accessed at cannot be null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("Expires at cannot be null");
        }
        if (expiresAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Expires at must be after created at");
        }
        if (lastAccessedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Last accessed at must be after created at");
        }
    }
    
    public SharedValueSessionId getSessionId() {
        return sessionId;
    }
    
    public SharedValueUserId getUserId() {
        return userId;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public Instant getExpiresAt() {
        return expiresAt;
    }
    
    public boolean isActive() {
        return isActive && !isExpired();
    }
    
    /**
     * Updates the last accessed timestamp to the current time.
     */
    public void updateLastAccessed() {
        this.lastAccessedAt = Instant.now();
    }
    
    /**
     * Expires the session by marking it as inactive.
     */
    public void expire() {
        this.isActive = false;
    }
    
    /**
     * Checks if the session has expired based on the expiration time.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
    
    /**
     * Checks if the session is valid (active and not expired).
     */
    public boolean isValid() {
        return isActive && !isExpired();
    }
    
    @Override
    public String toString() {
        return String.format("UserSession{sessionId=%s, userId=%s, createdAt=%s, lastAccessed=%s, expiresAt=%s, active=%s}",
            sessionId, userId, createdAt, lastAccessedAt, expiresAt, isActive);
    }
}