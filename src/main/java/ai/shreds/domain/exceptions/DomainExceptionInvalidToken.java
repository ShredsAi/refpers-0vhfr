package ai.shreds.domain.exceptions;

/**
 * Exception thrown when a JWT token is invalid (malformed, corrupted or has invalid signature).
 */
public class DomainExceptionInvalidToken extends RuntimeException {
    private final String token;
    private final String reason;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public DomainExceptionInvalidToken(String message) {
        super(message);
        this.token = null;
        this.reason = null;
    }

    /**
     * Constructs a new exception with the specified detail message and token.
     *
     * @param message the detail message
     * @param token the invalid token (may be partially masked for security)
     * @param reason specific reason for invalidity
     */
    public DomainExceptionInvalidToken(String message, String token, String reason) {
        super(message);
        this.token = maskToken(token);
        this.reason = reason;
    }

    /**
     * Constructs a new exception with the specified detail message, token, and cause.
     *
     * @param message the detail message
     * @param token the invalid token (may be partially masked for security)
     * @param reason specific reason for invalidity
     * @param cause the cause of the exception
     */
    public DomainExceptionInvalidToken(String message, String token, String reason, Throwable cause) {
        super(message, cause);
        this.token = maskToken(token);
        this.reason = reason;
    }

    /**
     * Gets the invalid token (masked for security).
     *
     * @return the masked token
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the specific reason for token invalidity.
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "[INVALID_TOKEN]";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}