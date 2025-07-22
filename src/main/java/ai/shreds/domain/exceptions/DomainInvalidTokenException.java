package ai.shreds.domain.exceptions;

/**
 * Exception thrown when a token is invalid or expired.
 */
public class DomainInvalidTokenException extends DomainException {

    /**
     * Constructs a new DomainInvalidTokenException with the specified detail message.
     *
     * @param message the detail message
     */
    public DomainInvalidTokenException(String message) {
        super(message, "INVALID_TOKEN");
    }
}
