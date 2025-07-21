package ai.shreds.application.exceptions;

/**
 * Exception thrown when an invalid authorization code is used.
 */
public class ApplicationInvalidAuthCodeException extends RuntimeException {
    private final String code;

    public ApplicationInvalidAuthCodeException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
