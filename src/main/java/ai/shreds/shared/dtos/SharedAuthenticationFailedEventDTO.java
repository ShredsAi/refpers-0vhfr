package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Event DTO for failed authentication notifications.
 * Published when authentication fails to track attempts and trigger security policies.
 */
public class SharedAuthenticationFailedEventDTO implements Serializable {
    
    @NotBlank(message = "Account ID must not be blank")
    private String accountId;
    
    @NotBlank(message = "Failure reason must not be blank")
    private String reason;
    
    @NotNull(message = "Attempt count must not be null")
    private Integer attempts;
    
    private String timestamp;
    private String ipAddress;
    private String userAgent;
    private String failureType; // e.g., "invalid_credentials", "mfa_failed", "account_locked"

    public SharedAuthenticationFailedEventDTO() {
    }

    public SharedAuthenticationFailedEventDTO(String accountId, String reason, Integer attempts) {
        this.accountId = accountId;
        this.reason = reason;
        this.attempts = attempts;
    }

    public SharedAuthenticationFailedEventDTO(String accountId, String reason, Integer attempts, String timestamp, String ipAddress, String userAgent, String failureType) {
        this.accountId = accountId;
        this.reason = reason;
        this.attempts = attempts;
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.failureType = failureType;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public String getFailureType() {
        return failureType;
    }

    public void setFailureType(String failureType) {
        this.failureType = failureType;
    }

    @Override
    public String toString() {
        return "SharedAuthenticationFailedEventDTO{" +
                "accountId='" + accountId + '\'' +
                ", reason='" + reason + '\'' +
                ", attempts=" + attempts +
                ", timestamp='" + timestamp + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", failureType='" + failureType + '\'' +
                '}';
    }
}