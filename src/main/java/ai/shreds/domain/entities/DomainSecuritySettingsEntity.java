package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedSecuritySettingsDTO;
import ai.shreds.shared.enums.SharedMfaMethodEnum;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_settings")
public class DomainSecuritySettingsEntity {

    @Id
    @Column(name = "security_settings_id")
    private UUID securitySettingsId;
    
    @Column(name = "account_id", unique = true, nullable = false)
    private UUID accountId;
    
    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "mfa_method", length = 10)
    private SharedMfaMethodEnum mfaMethod;
    
    @Column(name = "login_attempts", nullable = false)
    private int loginAttempts;
    
    @Column(name = "locked_until")
    private Instant lockedUntil;
    
    @Column(name = "password_changed_at", nullable = false)
    private Instant passwordChangedAt;

    // Default constructor for JPA
    public DomainSecuritySettingsEntity() {
    }

    public DomainSecuritySettingsEntity(UUID securitySettingsId,
                                        UUID accountId,
                                        boolean mfaEnabled,
                                        SharedMfaMethodEnum mfaMethod,
                                        int loginAttempts,
                                        Instant lockedUntil,
                                        Instant passwordChangedAt) {
        this.securitySettingsId = securitySettingsId;
        this.accountId = accountId;
        this.mfaEnabled = mfaEnabled;
        this.mfaMethod = mfaMethod;
        this.loginAttempts = loginAttempts;
        this.lockedUntil = lockedUntil;
        this.passwordChangedAt = passwordChangedAt;
    }

    public UUID getSecuritySettingsId() {
        return securitySettingsId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public SharedMfaMethodEnum getMfaMethod() {
        return mfaMethod;
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void enableMfa(SharedMfaMethodEnum method) {
        this.mfaEnabled = true;
        this.mfaMethod = method;
    }

    public void recordFailedLogin() {
        this.loginAttempts++;
    }

    public void resetLoginAttempts() {
        this.loginAttempts = 0;
    }

    public void lockAccount(Duration duration) {
        this.lockedUntil = Instant.now().plus(duration);
    }

    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    public SharedSecuritySettingsDTO toDTO() {
        SharedSecuritySettingsDTO dto = new SharedSecuritySettingsDTO();
        dto.setSecuritySettingsId(securitySettingsId.toString());
        dto.setAccountId(accountId.toString());
        dto.setMfaEnabled(mfaEnabled);
        dto.setMfaMethod(mfaMethod != null ? mfaMethod.name() : null);
        dto.setLoginAttempts(loginAttempts);
        dto.setLockedUntil(lockedUntil != null ? lockedUntil.toString() : null);
        dto.setPasswordChangedAt(passwordChangedAt != null ? passwordChangedAt.toString() : null);
        return dto;
    }

    public void setSecuritySettingsId(UUID securitySettingsId) {
        this.securitySettingsId = securitySettingsId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public void setMfaMethod(SharedMfaMethodEnum mfaMethod) {
        this.mfaMethod = mfaMethod;
    }

    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public void setPasswordChangedAt(Instant passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }
}
