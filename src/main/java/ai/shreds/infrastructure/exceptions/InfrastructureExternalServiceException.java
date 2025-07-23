package ai.shreds.infrastructure.exceptions;

public class InfrastructureExternalServiceException extends RuntimeException {
    
    private final String serviceName;
    private final Integer statusCode;
    private final String response;

    public InfrastructureExternalServiceException(String service, String message) {
        super(message);
        this.serviceName = service;
        this.statusCode = null;
        this.response = null;
    }

    public InfrastructureExternalServiceException(String service, String message, Integer statusCode, String response) {
        super(message);
        this.serviceName = service;
        this.statusCode = statusCode;
        this.response = response;
    }

    public InfrastructureExternalServiceException(String service, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = service;
        this.statusCode = null;
        this.response = null;
    }

    public InfrastructureExternalServiceException(String service, String message, Integer statusCode, String response, Throwable cause) {
        super(message, cause);
        this.serviceName = service;
        this.statusCode = statusCode;
        this.response = response;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResponse() {
        return response;
    }
}