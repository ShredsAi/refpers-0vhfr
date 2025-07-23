package ai.shreds.infrastructure.exceptions;

import java.util.Map;

public class InfrastructureRepositoryException extends RuntimeException {
    
    private final String errorCode;
    private final Map<String, Object> details;

    public InfrastructureRepositoryException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "REPOSITORY_ERROR";
        this.details = Map.of(
            "message", message,
            "cause", cause != null ? cause.getMessage() : "Unknown"
        );
    }

    public InfrastructureRepositoryException(String message, String errorCode, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public InfrastructureRepositoryException(String message, String errorCode, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}