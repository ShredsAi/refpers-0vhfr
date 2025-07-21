package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedSecuritySettingsDTO;
import ai.shreds.shared.enums.SharedMfaMethodEnum;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class DomainSecuritySettingsEntity {

    private UUID securitySettingsId;
    private UUID accountId;
    private boolean mfaEnabled;
    private SharedMfaMethodEnum mfaMethod;
    private int loginAttempts;
    private Instant lockedUntil;
    private Instant passwordChangedAt;

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
}
