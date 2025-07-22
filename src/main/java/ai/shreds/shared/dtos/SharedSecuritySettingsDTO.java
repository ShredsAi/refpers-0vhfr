package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for security settings data transfer between layers.
 * Contains account security configuration and lockout information.
 */
public class SharedSecuritySettingsDTO implements Serializable {
    
    @NotBlank(message = "Security settings ID must not be blank")
    private String securitySettingsId;
    
    @NotBlank(message = "Account ID must not be blank")
    private String accountId;
    
    @NotNull(message = "MFA enabled flag must not be null")
    private Boolean mfaEnabled;
    
    private String mfaMethod;
    
    @NotNull(message = "Login attempts must not be null")
    private Integer loginAttempts;
    
    private String lockedUntil;
    
    @NotBlank(message = "Password changed timestamp must not be blank")
    private String passwordChangedAt;

    public SharedSecuritySettingsDTO() {}

    public SharedSecuritySettingsDTO(
            String securitySettingsId,
            String accountId,
            Boolean mfaEnabled,
            String mfaMethod,
            Integer loginAttempts,
            String lockedUntil,
            String passwordChangedAt) {
        this.securitySettingsId = securitySettingsId;
        this.accountId = accountId;
        this.mfaEnabled = mfaEnabled;
        this.mfaMethod = mfaMethod;
        this.loginAttempts = loginAttempts;
        this.lockedUntil = lockedUntil;
        this.passwordChangedAt = passwordChangedAt;
    }

    // Convenience methods
    public boolean isAccountLocked() {
        return lockedUntil != null;
    }

    public boolean isMfaConfigured() {
        return mfaEnabled != null && mfaEnabled && mfaMethod != null;
    }

    public String getSecuritySettingsId() {
        return securitySettingsId;
    }

    public void setSecuritySettingsId(String securitySettingsId) {
        this.securitySettingsId = securitySettingsId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Boolean getMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(Boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public String getMfaMethod() {
        return mfaMethod;
    }

    public void setMfaMethod(String mfaMethod) {
        this.mfaMethod = mfaMethod;
    }

    public Integer getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(Integer loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public String getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(String lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(String passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    @Override
    public String toString() {
        return "SharedSecuritySettingsDTO{" +
                "securitySettingsId='" + securitySettingsId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", mfaEnabled=" + mfaEnabled +
                ", mfaMethod='" + mfaMethod + '\'' +
                ", loginAttempts=" + loginAttempts +
                ", lockedUntil='" + lockedUntil + '\'' +
                ", passwordChangedAt='" + passwordChangedAt + '\'' +
                '}';
    }
}