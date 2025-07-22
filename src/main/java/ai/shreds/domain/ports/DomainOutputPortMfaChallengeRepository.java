package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainMfaChallengeEntity;

/**
 * Outbound port for MFA challenge repository operations.
 */
public interface DomainOutputPortMfaChallengeRepository {

    /**
     * Save MFA challenge entity.
     */
    DomainMfaChallengeEntity save(DomainMfaChallengeEntity challenge);

    /**
     * Find MFA challenge by challenge ID.
     */
    DomainMfaChallengeEntity findById(String challengeId);

    /**
     * Mark challenge as used by challenge ID.
     */
    void markAsUsed(String challengeId);
}
