package ai.shreds.domain.exceptions;

/**
 * Exception thrown when user credentials are invalid during authentication.
 */
public class DomainInvalidCredentialsException extends DomainException {

    private static final String ERROR_CODE = "INVALID_CREDENTIALS";

    /**
     * Constructs a new DomainInvalidCredentialsException with the specified detail message.
     *
     * @param message the detail message
     */
    public DomainInvalidCredentialsException(String message) {
        super(message, ERROR_CODE);
    }

    /**
     * Constructs a new DomainInvalidCredentialsException with a default message.
     */
    public DomainInvalidCredentialsException() {
        super("The provided credentials are invalid", ERROR_CODE);
    }
}
