package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedAuthenticationSessionDTO;

import java.time.Instant;
import java.util.UUID;

public class DomainAuthenticationSessionEntity {

    private UUID sessionId;
    private UUID accountId;
    private String accessTokenHash;
    private String refreshTokenHash;
    private Instant expiresAt;
    private Instant createdAt;
    private boolean isRevoked;

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
}
