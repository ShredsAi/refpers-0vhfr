package ai.shreds.application.exceptions;

public class ApplicationAuthenticationException extends RuntimeException {
    private final String errorCode;

    public ApplicationAuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationAuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static ApplicationAuthenticationException invalidToken(String message) {
        return new ApplicationAuthenticationException(message, "INVALID_TOKEN");
    }

    public static ApplicationAuthenticationException tokenExpired(String message) {
        return new ApplicationAuthenticationException(message, "TOKEN_EXPIRED");
    }

    public static ApplicationAuthenticationException invalidIssuer(String message) {
        return new ApplicationAuthenticationException(message, "INVALID_ISSUER");
    }

    public static ApplicationAuthenticationException invalidAudience(String message) {
        return new ApplicationAuthenticationException(message, "INVALID_AUDIENCE");
    }
}