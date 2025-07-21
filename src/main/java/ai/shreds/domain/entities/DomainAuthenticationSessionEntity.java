package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedAuthenticationSessionDTO;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "authentication_sessions")
public class DomainAuthenticationSessionEntity {

    @Id
    @Column(name = "session_id")
    private UUID sessionId;
    
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    
    @Column(name = "access_token_hash", nullable = false)
    private String accessTokenHash;
    
    @Column(name = "refresh_token_hash", nullable = false, unique = true)
    private String refreshTokenHash;
    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked;

    // Default constructor for JPA
    public DomainAuthenticationSessionEntity() {
    }

    public DomainAuthenticationSessionEntity(UUID sessionId,
                                           UUID accountId,
                                           String accessTokenHash,
                                           String refreshTokenHash,
                                           Instant expiresAt,
                                           Instant createdAt,
                                           boolean isRevoked) {
        this.sessionId = sessionId;
        this.accountId = accountId;
        this.accessTokenHash = accessTokenHash;
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.isRevoked = isRevoked;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getAccessTokenHash() {
        return accessTokenHash;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void revoke() {
        this.isRevoked = true;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isValid() {
        return !isRevoked && !isExpired();
    }

    public SharedAuthenticationSessionDTO toDTO() {
        SharedAuthenticationSessionDTO dto = new SharedAuthenticationSessionDTO();
        dto.setSessionId(sessionId.toString());
        dto.setAccountId(accountId.toString());
        dto.setAccessTokenHash(accessTokenHash);
        dto.setRefreshTokenHash(refreshTokenHash);
        dto.setExpiresAt(expiresAt != null ? expiresAt.toString() : null);
        dto.setCreatedAt(createdAt != null ? createdAt.toString() : null);
        dto.setIsRevoked(isRevoked);
        return dto;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setAccessTokenHash(String accessTokenHash) {
        this.accessTokenHash = accessTokenHash;
    }

    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }
}
