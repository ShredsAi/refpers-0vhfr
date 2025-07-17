package ai.shreds.infrastructure.exceptions;

/**
 * Exception thrown when JWKS retrieval from the Account Service fails.
 */
public class InfrastructureJwksRetrievalException extends RuntimeException {
    private final String url;
    private final int statusCode;

    public InfrastructureJwksRetrievalException(String message, String url, int statusCode) {
        super(message);
        this.url = url;
        this.statusCode = statusCode;
    }

    public String getUrl() {
        return url;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
