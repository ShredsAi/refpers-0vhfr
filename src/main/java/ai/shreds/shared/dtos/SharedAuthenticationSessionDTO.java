package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for authentication session data transfer between layers.
 * Contains session information and token hashes for security tracking.
 */
public class SharedAuthenticationSessionDTO implements Serializable {
    
    @NotBlank(message = "Session ID must not be blank")
    private String sessionId;
    
    @NotBlank(message = "Account ID must not be blank")
    private String accountId;
    
    @NotBlank(message = "Access token hash must not be blank")
    private String accessTokenHash;
    
    @NotBlank(message = "Refresh token hash must not be blank")
    private String refreshTokenHash;
    
    @NotBlank(message = "Expires at timestamp must not be blank")
    private String expiresAt;
    
    @NotBlank(message = "Created at timestamp must not be blank")
    private String createdAt;
    
    @NotNull(message = "Revoked flag must not be null")
    private Boolean isRevoked;

    public SharedAuthenticationSessionDTO() {}

    public SharedAuthenticationSessionDTO(
            String sessionId,
            String accountId,
            String accessTokenHash,
            String refreshTokenHash,
            String expiresAt,
            String createdAt,
            Boolean isRevoked) {
        this.sessionId = sessionId;
        this.accountId = accountId;
        this.accessTokenHash = accessTokenHash;
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.isRevoked = isRevoked;
    }

    // Convenience methods
    public boolean isActive() {
        return isRevoked == null || !isRevoked;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccessTokenHash() {
        return accessTokenHash;
    }

    public void setAccessTokenHash(String accessTokenHash) {
        this.accessTokenHash = accessTokenHash;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
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

    public Boolean getIsRevoked() {
        return isRevoked;
    }

    public void setIsRevoked(Boolean isRevoked) {
        this.isRevoked = isRevoked;
    }

    @Override
    public String toString() {
        return "SharedAuthenticationSessionDTO{" +
                "sessionId='" + sessionId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", accessTokenHash='[PROTECTED]'" +
                ", refreshTokenHash='[PROTECTED]'" +
                ", expiresAt='" + expiresAt + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", isRevoked=" + isRevoked +
                '}';
    }
}