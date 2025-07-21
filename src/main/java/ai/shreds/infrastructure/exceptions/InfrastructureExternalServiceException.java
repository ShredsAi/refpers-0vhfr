package ai.shreds.infrastructure.exceptions;

/**
 * Exception thrown when external service calls fail in the infrastructure layer.
 * This exception provides context about the service name and status code if available.
 */
public class InfrastructureExternalServiceException extends RuntimeException {

    private final String serviceName;
    private final Integer statusCode;

    public InfrastructureExternalServiceException(String message, String serviceName, Integer statusCode) {
        super(message);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public InfrastructureExternalServiceException(String message, String serviceName, Integer statusCode, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return String.format("InfrastructureExternalServiceException{serviceName='%s', statusCode=%d, message='%s'}",
                serviceName, statusCode, getMessage());
    }
}