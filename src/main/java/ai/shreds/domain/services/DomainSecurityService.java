package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainSecuritySettingsEntity;
import ai.shreds.domain.ports.DomainInputPortSecuritySettings;
import ai.shreds.domain.ports.DomainOutputPortSecurityRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class DomainSecurityService implements DomainInputPortSecuritySettings {

    private final DomainOutputPortSecurityRepository securityRepository;

    public DomainSecurityService(DomainOutputPortSecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
    }

    @Override
    public DomainSecuritySettingsEntity getSecuritySettings(String accountId) {
        DomainSecuritySettingsEntity settings = securityRepository.findByAccountId(accountId);
        
        // If no security settings exist, create default ones
        if (settings == null) {
            settings = new DomainSecuritySettingsEntity(
                UUID.randomUUID(),
                UUID.fromString(accountId),
                false, // MFA disabled by default
                null,  // No MFA method set initially
                0,     // No failed login attempts
                null,  // Not locked
                Instant.now() // Password changed at creation time
            );
            settings = securityRepository.save(settings);
        }
        
        return settings;
    }

    @Override
    public void updateSecuritySettings(String accountId, DomainSecuritySettingsEntity settings) {
        // Validate that the accountId matches the settings
        if (!accountId.equals(settings.getAccountId().toString())) {
            throw new IllegalArgumentException("Account ID mismatch in security settings update");
        }

        // Check if settings already exist
        DomainSecuritySettingsEntity existingSettings = securityRepository.findByAccountId(accountId);
        if (existingSettings == null) {
            // Create new settings
            securityRepository.save(settings);
        } else {
            // Update existing settings
            securityRepository.update(settings);
        }
    }

    @Override
    public boolean checkAccountLockStatus(String accountId) {
        DomainSecuritySettingsEntity settings = securityRepository.findByAccountId(accountId);
        return settings != null && settings.isAccountLocked();
    }

    @Override
    public void applyLockoutPolicy(String accountId, Integer attempts) {
        DomainSecuritySettingsEntity settings = getSecuritySettings(accountId);
        
        // Apply progressive lockout policy based on number of attempts
        Duration lockoutDuration;
        if (attempts >= 5 && attempts < 10) {
            // First lockout: 15 minutes
            lockoutDuration = Duration.ofMinutes(15);
        } else if (attempts >= 10 && attempts < 20) {
            // Second level lockout: 1 hour
            lockoutDuration = Duration.ofHours(1);
        } else if (attempts >= 20) {
            // Third level lockout: 24 hours
            lockoutDuration = Duration.ofHours(24);
        } else {
            // No lockout needed
            return;
        }

        // Apply the lockout
        settings.lockAccount(lockoutDuration);
        updateSecuritySettings(accountId, settings);
    }
}
