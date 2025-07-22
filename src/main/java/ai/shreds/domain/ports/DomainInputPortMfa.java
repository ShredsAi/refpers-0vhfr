package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainMfaChallengeEntity;
import ai.shreds.shared.enums.SharedMfaMethodEnum;

/**
 * Inbound port for multi-factor authentication operations in the domain layer.
 */
public interface DomainInputPortMfa {

    /**
     * Generate an MFA challenge for the specified account and method.
     */
    DomainMfaChallengeEntity generateMfaChallenge(String accountId, SharedMfaMethodEnum method);

    /**
     * Verify the MFA code against the challenge.
     */
    boolean verifyMfaCode(String challengeId, String code);

    /**
     * Check if MFA is required for the account.
     */
    boolean isMfaRequired(String accountId);
}
