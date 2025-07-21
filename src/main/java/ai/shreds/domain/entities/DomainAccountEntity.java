package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedAccountDTO;
import ai.shreds.shared.enums.SharedAccountStatusTypeEnum;

import java.time.Instant;
import java.util.UUID;

public class DomainAccountEntity {

    private UUID accountId;
    private String username;
    private String passwordHash;
    private String email;
    private SharedAccountStatusTypeEnum accountStatus;
    private Instant createdAt;
    private Instant lastLoginAt;

    public DomainAccountEntity(UUID accountId,
                             String username,
                             String passwordHash,
                             String email,
                             SharedAccountStatusTypeEnum accountStatus,
                             Instant createdAt,
                             Instant lastLoginAt) {
        this.accountId = accountId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public SharedAccountStatusTypeEnum getAccountStatus() {
        return accountStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public boolean isActive() {
        return SharedAccountStatusTypeEnum.ACTIVE.equals(accountStatus);
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
    }

    public SharedAccountDTO toDTO() {
        SharedAccountDTO dto = new SharedAccountDTO();
        dto.setAccountId(accountId.toString());
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setAccountStatus(accountStatus.name());
        dto.setCreatedAt(createdAt != null ? createdAt.toString() : null);
        dto.setLastLoginAt(lastLoginAt != null ? lastLoginAt.toString() : null);
        return dto;
    }
}
