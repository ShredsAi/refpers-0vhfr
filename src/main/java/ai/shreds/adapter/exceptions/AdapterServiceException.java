package ai.shreds.adapter.exceptions;

/**
 * Adapter layer specific exception for handling errors in the adapter layer.
 * This exception wraps application layer exceptions and provides adapter-specific error handling.
 */
public class AdapterServiceException extends RuntimeException {
    
    private final String errorCode;
    private final String path;
    
    public AdapterServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.path = null;
    }
    
    public AdapterServiceException(String message, String errorCode, String path) {
        super(message);
        this.errorCode = errorCode;
        this.path = path;
    }
    
    public AdapterServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.path = null;
    }
    
    public AdapterServiceException(String message, String errorCode, String path, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.path = path;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getPath() {
        return path;
    }
    
    /**
     * Creates an adapter exception from an application layer exception.
     *
     * @param applicationException The application layer exception to wrap
     * @param path The request path where the error occurred
     * @return A new AdapterServiceException wrapping the application exception
     */
    public static AdapterServiceException fromApplicationException(
            ai.shreds.application.exceptions.ApplicationServiceException applicationException, 
            String path) {
        return new AdapterServiceException(
            applicationException.getMessage(),
            applicationException.getCode(),
            path,
            applicationException
        );
    }
}