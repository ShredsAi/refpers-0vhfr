package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainSecuritySettingsEntity;

/**
 * Outbound port for security settings repository operations.
 */
public interface DomainOutputPortSecurityRepository {

    /**
     * Find security settings by account ID.
     */
    DomainSecuritySettingsEntity findByAccountId(String accountId);

    /**
     * Save security settings entity.
     */
    DomainSecuritySettingsEntity save(DomainSecuritySettingsEntity settings);

    /**
     * Update existing security settings entity.
     */
    void update(DomainSecuritySettingsEntity settings);
}
