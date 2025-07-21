package ai.shreds.shared.dtos;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for standardized error responses across the authentication system.
 * Follows OAuth 2.0 error response format when applicable.
 */
public class SharedErrorResponseDTO implements Serializable {
    
    private String error;
    private String errorDescription;
    private String timestamp;
    private String path;

    public SharedErrorResponseDTO() {
        this.timestamp = Instant.now().toString();
    }

    public SharedErrorResponseDTO(String error, String errorDescription, String timestamp, String path) {
        this.error = error;
        this.errorDescription = errorDescription;
        this.timestamp = timestamp != null ? timestamp : Instant.now().toString();
        this.path = path;
    }

    // Factory methods for common error types
    public static SharedErrorResponseDTO invalidRequest(String description, String path) {
        return new SharedErrorResponseDTO("invalid_request", description, null, path);
    }

    public static SharedErrorResponseDTO invalidGrant(String description, String path) {
        return new SharedErrorResponseDTO("invalid_grant", description, null, path);
    }

    public static SharedErrorResponseDTO invalidClient(String description, String path) {
        return new SharedErrorResponseDTO("invalid_client", description, null, path);
    }

    public static SharedErrorResponseDTO accessDenied(String description, String path) {
        return new SharedErrorResponseDTO("access_denied", description, null, path);
    }

    public static SharedErrorResponseDTO serverError(String description, String path) {
        return new SharedErrorResponseDTO("server_error", description, null, path);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "SharedErrorResponseDTO{" +
                "error='" + error + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}