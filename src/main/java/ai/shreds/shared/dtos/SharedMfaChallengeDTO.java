package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for MFA challenge data transfer between layers.
 * Contains challenge information for multi-factor authentication flow.
 */
public class SharedMfaChallengeDTO implements Serializable {
    
    @NotBlank(message = "Challenge ID must not be blank")
    private String challengeId;
    
    @NotBlank(message = "Account ID must not be blank")
    private String accountId;
    
    @NotBlank(message = "MFA method must not be blank")
    private String method;
    
    @NotBlank(message = "Expiration timestamp must not be blank")
    private String expiresAt;
    
    @NotBlank(message = "Creation timestamp must not be blank")
    private String createdAt;
    
    private Boolean isUsed;
    private String usedAt;
    private Integer remainingAttempts;

    public SharedMfaChallengeDTO() {}

    public SharedMfaChallengeDTO(
            String challengeId,
            String accountId,
            String method,
            String expiresAt,
            String createdAt) {
        this.challengeId = challengeId;
        this.accountId = accountId;
        this.method = method;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.isUsed = false;
    }

    public SharedMfaChallengeDTO(
            String challengeId,
            String accountId,
            String method,
            String expiresAt,
            String createdAt,
            Boolean isUsed,
            String usedAt,
            Integer remainingAttempts) {
        this.challengeId = challengeId;
        this.accountId = accountId;
        this.method = method;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.isUsed = isUsed;
        this.usedAt = usedAt;
        this.remainingAttempts = remainingAttempts;
    }

    // Convenience methods
    public boolean isActive() {
        return isUsed == null || !isUsed;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public String getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(String usedAt) {
        this.usedAt = usedAt;
    }

    public Integer getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(Integer remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

    @Override
    public String toString() {
        return "SharedMfaChallengeDTO{" +
                "challengeId='" + challengeId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", method='" + method + '\'' +
                ", expiresAt='" + expiresAt + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", isUsed=" + isUsed +
                ", usedAt='" + usedAt + '\'' +
                ", remainingAttempts=" + remainingAttempts +
                '}';
    }
}