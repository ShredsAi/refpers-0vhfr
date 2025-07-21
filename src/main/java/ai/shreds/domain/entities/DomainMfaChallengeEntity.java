package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedMfaChallengeDTO;
import ai.shreds.shared.enums.SharedMfaMethodEnum;

import java.time.Instant;
import java.util.UUID;

public class DomainMfaChallengeEntity {

    private UUID challengeId;
    private UUID accountId;
    private String challengeCodeHash;
    private SharedMfaMethodEnum method;
    private Instant expiresAt;
    private boolean isUsed;
    private Instant createdAt;

    public DomainMfaChallengeEntity(UUID challengeId,
                                  UUID accountId,
                                  String challengeCodeHash,
                                  SharedMfaMethodEnum method,
                                  Instant expiresAt,
                                  boolean isUsed,
                                  Instant createdAt) {
        this.challengeId = challengeId;
        this.accountId = accountId;
        this.challengeCodeHash = challengeCodeHash;
        this.method = method;
        this.expiresAt = expiresAt;
        this.isUsed = isUsed;
        this.createdAt = createdAt;
    }

    public UUID getChallengeId() {
        return challengeId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getChallengeCodeHash() {
        return challengeCodeHash;
    }

    public SharedMfaMethodEnum getMethod() {
        return method;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markAsUsed() {
        this.isUsed = true;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isValid() {
        return !isUsed && !isExpired();
    }

    public SharedMfaChallengeDTO toDTO() {
        SharedMfaChallengeDTO dto = new SharedMfaChallengeDTO();
        dto.setChallengeId(challengeId.toString());
        dto.setAccountId(accountId.toString());
        dto.setMethod(method.name());
        dto.setExpiresAt(expiresAt != null ? expiresAt.toString() : null);
        dto.setCreatedAt(createdAt != null ? createdAt.toString() : null);
        return dto;
    }
}
