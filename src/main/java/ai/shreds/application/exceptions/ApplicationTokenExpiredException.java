package ai.shreds.application.exceptions;

/**
 * Exception thrown when attempting to use an expired token.
 */
public class ApplicationTokenExpiredException extends RuntimeException {
    private final String tokenType;

    public ApplicationTokenExpiredException(String message, String tokenType) {
        super(message);
        this.tokenType = tokenType;
    }

    public ApplicationTokenExpiredException(String message, String tokenType, Throwable cause) {
        super(message, cause);
        this.tokenType = tokenType;
    }

    public String getTokenType() {
        return tokenType;
    }
}
