package ai.shreds.shared.dtos;

import java.io.Serializable;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Event DTO for authentication attempt notifications.
 * Published to trigger account status validation from Account Management Shred.
 */
public class SharedAuthenticationAttemptEventDTO implements Serializable {

    @NotBlank(message = "Account ID must not be blank")
    private String accountId;

    @NotNull(message = "Authentication context must not be null")
    private Map<String, Object> authenticationContext;

    private String ipAddress;
    private String userAgent;
    private String timestamp;

    public SharedAuthenticationAttemptEventDTO() {}

    public SharedAuthenticationAttemptEventDTO(String accountId, Map<String, Object> authenticationContext) {
        this.accountId = accountId;
        this.authenticationContext = authenticationContext;
    }

    public SharedAuthenticationAttemptEventDTO(String accountId, Map<String, Object> authenticationContext, String ipAddress, String userAgent, String timestamp) {
        this.accountId = accountId;
        this.authenticationContext = authenticationContext;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = timestamp;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Map<String, Object> getAuthenticationContext() {
        return authenticationContext;
    }

    public void setAuthenticationContext(Map<String, Object> authenticationContext) {
        this.authenticationContext = authenticationContext;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SharedAuthenticationAttemptEventDTO{" +
                "accountId='" + accountId + '\'' +
                ", authenticationContext=" + authenticationContext +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}