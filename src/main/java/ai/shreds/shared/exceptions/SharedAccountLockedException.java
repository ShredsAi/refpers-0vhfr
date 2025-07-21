package ai.shreds.shared.exceptions;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Shared exception for account lockout scenarios.
 * Thrown when authentication is attempted on a locked account.
 */
public class SharedAccountLockedException extends RuntimeException {

    private final String lockedUntil;
    private final Integer attempts;

    public SharedAccountLockedException(String message, String lockedUntil, Integer attempts) {
        super(message);
        this.lockedUntil = lockedUntil;
        this.attempts = attempts;
    }

    public SharedAccountLockedException(String message, String lockedUntil, Integer attempts, Throwable cause) {
        super(message, cause);
        this.lockedUntil = lockedUntil;
        this.attempts = attempts;
    }

    // Factory methods for common lockout scenarios
    public static SharedAccountLockedException failedAttempts(String accountId, String lockedUntil, Integer attempts) {
        String message = String.format("Account %s is locked due to %d failed login attempts until %s", 
                                     accountId, attempts, lockedUntil);
        return new SharedAccountLockedException(message, lockedUntil, attempts);
    }

    public static SharedAccountLockedException securityPolicy(String accountId, String lockedUntil, String reason) {
        String message = String.format("Account %s is locked due to security policy: %s until %s", 
                                     accountId, reason, lockedUntil);
        return new SharedAccountLockedException(message, lockedUntil, null);
    }

    // Utility methods
    public boolean isCurrentlyLocked() {
        if (lockedUntil == null) {
            return false;
        }
        try {
            Instant lockExpiry = Instant.parse(lockedUntil);
            return Instant.now().isBefore(lockExpiry);
        } catch (DateTimeParseException e) {
            // If we can't parse the date, assume it's locked for safety
            return true;
        }
    }

    public String getLockedUntil() {
        return lockedUntil;
    }

    public Integer getAttempts() {
        return attempts;
    }

    @Override
    public String toString() {
        return "SharedAccountLockedException{" +
                "lockedUntil='" + lockedUntil + '\'' +
                ", attempts=" + attempts +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}