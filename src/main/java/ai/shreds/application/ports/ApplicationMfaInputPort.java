package ai.shreds.application.ports;

import ai.shreds.application.dtos.ApplicationMfaChallengeDTO;
import ai.shreds.shared.dtos.SharedMfaVerifyRequestDTO;
import ai.shreds.shared.dtos.SharedOAuth2TokenResponseDTO;
import ai.shreds.shared.dtos.SharedSensitiveOperationRequestEventDTO;

/**
 * Input port for multi-factor authentication operations.
 */
public interface ApplicationMfaInputPort {

    /**
     * Generate MFA challenge for a given account and method.
     * @param accountId the account identifier
     * @param method the MFA method (SMS, EMAIL, TOTP)
     * @return MFA challenge DTO
     */
    ApplicationMfaChallengeDTO generateMfaChallenge(String accountId, String method);

    /**
     * Verify MFA code and complete authentication.
     * @param request MFA verification request
     * @return token response if successful
     */
    SharedOAuth2TokenResponseDTO verifyMfaCode(SharedMfaVerifyRequestDTO request);

    /**
     * Initiate step-up authentication for sensitive operations.
     * @param event sensitive operation request event
     */
    void initiateStepUpAuthentication(SharedSensitiveOperationRequestEventDTO event);
}