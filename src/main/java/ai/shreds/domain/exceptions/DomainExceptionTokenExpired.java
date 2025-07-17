package ai.shreds.domain.exceptions;

import java.time.Instant;

/**
 * Exception thrown when a JWT token has expired based on its 'exp' claim.
 */
public class DomainExceptionTokenExpired extends RuntimeException {
    private final Instant expiredAt;

    /**
     * Constructs a new exception with the specified detail message and expiration timestamp.
     *
     * @param message the detail message
     * @param expiredAt the timestamp when the token expired
     */
    public DomainExceptionTokenExpired(String message, Instant expiredAt) {
        super(String.format("%s. Token expired at %s (current time: %s)", 
            message, expiredAt, Instant.now()));
        this.expiredAt = expiredAt;
    }

    /**
     * Gets the timestamp when the token expired.
     *
     * @return the expiration timestamp
     */
    public Instant getExpiredAt() {
        return expiredAt;
    }

    /**
     * Gets the duration since expiration.
     *
     * @return duration in seconds since token expired
     */
    public long getSecondsSinceExpiration() {
        return Instant.now().getEpochSecond() - expiredAt.getEpochSecond();
    }
}