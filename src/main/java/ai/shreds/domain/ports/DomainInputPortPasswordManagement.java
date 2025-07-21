package ai.shreds.domain.ports;

/**
 * Inbound port for password management operations in the domain layer.
 */
public interface DomainInputPortPasswordManagement {

    /**
     * Validates that the password meets the required policy rules.
     */
    boolean validatePasswordPolicy(String password);

    /**
     * Generates a secure password reset token for the given email.
     */
    String generatePasswordResetToken(String email);

    /**
     * Changes the password for the account after validating the current password.
     */
    void changePassword(String accountId, String currentPassword, String newPassword);

    /**
     * Resets the password using a valid reset token.
     */
    void resetPassword(String token, String newPassword);
}
