package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedPasswordResetRequestDTO;
import ai.shreds.shared.dtos.SharedPasswordChangeRequestDTO;

/**
 * Input port for password management operations.
 */
public interface ApplicationPasswordInputPort {

    /**
     * Initiate password reset process.
     * @param request password reset request containing email
     */
    void initiatePasswordReset(SharedPasswordResetRequestDTO request);

    /**
     * Change user password.
     * @param request password change request with current and new password
     */
    void changePassword(SharedPasswordChangeRequestDTO request);
}