package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for password change requests containing account ID, current password, and new password.
 * Used for user-initiated password changes.
 */
public class SharedPasswordChangeRequestDTO implements Serializable {

    @NotBlank(message = "Account ID must not be blank")
    private String accountId;

    @NotBlank(message = "Current password must not be blank")
    @Size(min = 1, max = 128, message = "Current password length must be between 1 and 128 characters")
    private String currentPassword;

    @NotBlank(message = "New password must not be blank")
    @Size(min = 8, max = 128, message = "New password must be between 8 and 128 characters")
    private String newPassword;

    public SharedPasswordChangeRequestDTO() {
    }

    public SharedPasswordChangeRequestDTO(String accountId, String currentPassword, String newPassword) {
        this.accountId = accountId;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public String toString() {
        return "SharedPasswordChangeRequestDTO{" +
                "accountId='" + accountId + '\'' +
                ", currentPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                '}';
    }
}