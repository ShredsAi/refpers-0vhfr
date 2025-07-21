package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;

/**
 * Event DTO for password change notifications.
 * Published when a user's password is successfully changed.
 */
public class SharedPasswordChangedEventDTO implements Serializable {
    
    @NotBlank(message = "Account ID must not be blank")
    private String accountId;
    
    @NotBlank(message = "Changed timestamp must not be blank")
    private String changedAt;
    
    @NotBlank(message = "Change method must not be blank")
    private String method;
    
    private String ipAddress;
    private String userAgent;
    private String sessionId; // Session that initiated the change
    private Boolean wasResetToken; // True if changed via reset token, false if via current password

    public SharedPasswordChangedEventDTO() {
    }

    public SharedPasswordChangedEventDTO(String accountId, String changedAt, String method) {
        this.accountId = accountId;
        this.changedAt = changedAt;
        this.method = method;
    }

    public SharedPasswordChangedEventDTO(String accountId, String changedAt, String method, String ipAddress, String userAgent, String sessionId, Boolean wasResetToken) {
        this.accountId = accountId;
        this.changedAt = changedAt;
        this.method = method;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
        this.wasResetToken = wasResetToken;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(String changedAt) {
        this.changedAt = changedAt;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Boolean getWasResetToken() {
        return wasResetToken;
    }

    public void setWasResetToken(Boolean wasResetToken) {
        this.wasResetToken = wasResetToken;
    }

    @Override
    public String toString() {
        return "SharedPasswordChangedEventDTO{" +
                "accountId='" + accountId + '\'' +
                ", changedAt='" + changedAt + '\'' +
                ", method='" + method + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", wasResetToken=" + wasResetToken +
                '}';
    }
}