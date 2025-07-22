package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedMfaChallengeDTO;
import ai.shreds.shared.enums.SharedMfaMethodEnum;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mfa_challenges")
public class DomainMfaChallengeEntity {

    @Id
    @Column(name = "challenge_id")
    private UUID challengeId;
    
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    
    @Column(name = "challenge_code_hash", nullable = false)
    private String challengeCodeHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 10)
    private SharedMfaMethodEnum method;
    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    @Column(name = "is_used", nullable = false)
    private boolean isUsed;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Default constructor for JPA
    public DomainMfaChallengeEntity() {
    }

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

    public void setChallengeId(UUID challengeId) {
        this.challengeId = challengeId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setChallengeCodeHash(String challengeCodeHash) {
        this.challengeCodeHash = challengeCodeHash;
    }

    public void setMethod(SharedMfaMethodEnum method) {
        this.method = method;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
