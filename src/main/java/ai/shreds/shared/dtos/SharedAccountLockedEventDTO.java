package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Event DTO for account lockout notifications.
 * Published when an account is locked due to security policies (e.g., failed login attempts).
 */
public class SharedAccountLockedEventDTO implements Serializable {

    @NotBlank(message = "Account ID must not be blank")
    private String accountId;

    @NotBlank(message = "Lock reason must not be blank")
    private String lockReason;

    @NotBlank(message = "Locked until timestamp must not be blank")
    private String lockedUntil;

    private String timestamp;
    private Integer failedAttempts;
    private String ipAddress;
    private String lockType; // e.g., "AUTOMATIC", "MANUAL", "SECURITY_POLICY"

    public SharedAccountLockedEventDTO() {
    }

    public SharedAccountLockedEventDTO(String accountId, String lockReason, String lockedUntil) {
        this.accountId = accountId;
        this.lockReason = lockReason;
        this.lockedUntil = lockedUntil;
    }

    public SharedAccountLockedEventDTO(String accountId, String lockReason, String lockedUntil, String timestamp, Integer failedAttempts, String ipAddress, String lockType) {
        this.accountId = accountId;
        this.lockReason = lockReason;
        this.lockedUntil = lockedUntil;
        this.timestamp = timestamp;
        this.failedAttempts = failedAttempts;
        this.ipAddress = ipAddress;
        this.lockType = lockType;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getLockReason() {
        return lockReason;
    }

    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    public String getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(String lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLockType() {
        return lockType;
    }

    public void setLockType(String lockType) {
        this.lockType = lockType;
    }

    @Override
    public String toString() {
        return "SharedAccountLockedEventDTO{" +
                "accountId='" + accountId + '\'' +
                ", lockReason='" + lockReason + '\'' +
                ", lockedUntil='" + lockedUntil + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", failedAttempts=" + failedAttempts +
                ", ipAddress='" + ipAddress + '\'' +
                ", lockType='" + lockType + '\'' +
                '}';
    }
}