package ai.shreds.domain.exceptions;

/**
 * Base exception for domain layer errors.
 */
public class DomainException extends RuntimeException {

    private final String errorCode;

    /**
     * Constructs a new DomainException with the specified detail message and error code.
     *
     * @param message the detail message
     * @param errorCode the application-specific error code
     */
    public DomainException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the application-specific error code for this exception.
     *
     * @return error code string
     */
    public String getErrorCode() {
        return errorCode;
    }
}
