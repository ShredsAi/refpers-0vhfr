package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;

/**
 * Event DTO for user logout notifications.
 * Published when a user session is terminated (logout, token revocation).
 */
public class SharedUserLoggedOutEventDTO implements Serializable {

    @NotBlank(message = "Account ID must not be blank")
    private String accountId;

    @NotBlank(message = "Session ID must not be blank")
    private String sessionId;

    private String logoutAt;
    private String logoutReason; // e.g., "USER_INITIATED", "TOKEN_EXPIRED", "ADMIN_REVOKED", "PASSWORD_CHANGED"
    private String ipAddress;
    private String userAgent;

    public SharedUserLoggedOutEventDTO() {}

    public SharedUserLoggedOutEventDTO(String accountId, String sessionId) {
        this.accountId = accountId;
        this.sessionId = sessionId;
    }

    public SharedUserLoggedOutEventDTO(String accountId, String sessionId, String logoutAt, String logoutReason, String ipAddress, String userAgent) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.logoutAt = logoutAt;
        this.logoutReason = logoutReason;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
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

    public String getLogoutAt() {
        return logoutAt;
    }

    public void setLogoutAt(String logoutAt) {
        this.logoutAt = logoutAt;
    }

    public String getLogoutReason() {
        return logoutReason;
    }

    public void setLogoutReason(String logoutReason) {
        this.logoutReason = logoutReason;
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

    @Override
    public String toString() {
        return "SharedUserLoggedOutEventDTO{" +
                "accountId='" + accountId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", logoutAt='" + logoutAt + '\'' +
                ", logoutReason='" + logoutReason + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}