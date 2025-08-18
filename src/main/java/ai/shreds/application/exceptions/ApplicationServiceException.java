package ai.shreds.application.exceptions;

public class ApplicationServiceException extends RuntimeException {
    private final String code;

    public ApplicationServiceException(String message, String code) {
        super(message);
        this.code = code;
    }

    public ApplicationServiceException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}