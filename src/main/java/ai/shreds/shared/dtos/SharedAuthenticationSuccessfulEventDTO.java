package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;

/**
 * Event DTO for successful authentication notifications.
 * Published when a user completes full authentication including MFA if required.
 */
public class SharedAuthenticationSuccessfulEventDTO implements Serializable {

    @NotBlank(message = "Account ID must not be blank")
    private String accountId;

    @NotBlank(message = "Session ID must not be blank")
    private String sessionId;

    @NotBlank(message = "Login timestamp must not be blank")
    private String loginAt;

    private String ipAddress;
    private String userAgent;
    private String authenticationMethod; // e.g., "password+mfa", "password_only"

    public SharedAuthenticationSuccessfulEventDTO() {
    }

    public SharedAuthenticationSuccessfulEventDTO(String accountId, String sessionId, String loginAt) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.loginAt = loginAt;
    }

    public SharedAuthenticationSuccessfulEventDTO(String accountId, String sessionId, String loginAt, String ipAddress, String userAgent, String authenticationMethod) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.loginAt = loginAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.authenticationMethod = authenticationMethod;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(String loginAt) {
        this.loginAt = loginAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    @Override
    public String toString() {
        return "SharedAuthenticationSuccessfulEventDTO{" +
                "accountId='" + accountId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", loginAt='" + loginAt + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", authenticationMethod='" + authenticationMethod + '\'' +
                '}';
    }
}