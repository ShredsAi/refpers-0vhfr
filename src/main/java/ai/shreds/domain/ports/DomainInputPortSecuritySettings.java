package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainSecuritySettingsEntity;

/**
 * Inbound port for security settings operations in the domain layer.
 */
public interface DomainInputPortSecuritySettings {

    /**
     * Retrieve security settings for the given account.
     * @param accountId the account identifier
     * @return the DomainSecuritySettingsEntity for the account
     */
    DomainSecuritySettingsEntity getSecuritySettings(String accountId);

    /**
     * Update security settings for the given account.
     * @param accountId the account identifier
     * @param settings the new security settings
     */
    void updateSecuritySettings(String accountId, DomainSecuritySettingsEntity settings);

    /**
     * Check if the account is currently locked.
     * @param accountId the account identifier
     * @return true if locked, false otherwise
     */
    boolean checkAccountLockStatus(String accountId);

    /**
     * Apply account lockout policy based on failed attempts.
     * @param accountId the account identifier
     * @param attempts the number of failed attempts
     */
    void applyLockoutPolicy(String accountId, Integer attempts);
}
