package ai.shreds.shared.exceptions;

/**
 * Shared exception for authorization-related errors across the authentication system.
 * Used for OAuth 2.0 and general authorization failures.
 */
public class SharedAuthorizationException extends RuntimeException {

    private final String errorCode;

    public SharedAuthorizationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public SharedAuthorizationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    // Factory methods for common authorization errors
    public static SharedAuthorizationException invalidGrant(String message) {
        return new SharedAuthorizationException(message, "invalid_grant");
    }

    public static SharedAuthorizationException invalidClient(String message) {
        return new SharedAuthorizationException(message, "invalid_client");
    }

    public static SharedAuthorizationException invalidRequest(String message) {
        return new SharedAuthorizationException(message, "invalid_request");
    }

    public static SharedAuthorizationException accessDenied(String message) {
        return new SharedAuthorizationException(message, "access_denied");
    }

    public static SharedAuthorizationException unsupportedGrantType(String message) {
        return new SharedAuthorizationException(message, "unsupported_grant_type");
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "SharedAuthorizationException{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}