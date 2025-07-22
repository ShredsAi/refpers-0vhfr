package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedAccountDTO;
import ai.shreds.shared.enums.SharedAccountStatusTypeEnum;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class DomainAccountEntity {

    @Id
    @Column(name = "account_id")
    private UUID accountId;
    
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private SharedAccountStatusTypeEnum accountStatus;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    // Default constructor for JPA
    public DomainAccountEntity() {
    }

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

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAccountStatus(SharedAccountStatusTypeEnum accountStatus) {
        this.accountStatus = accountStatus;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
