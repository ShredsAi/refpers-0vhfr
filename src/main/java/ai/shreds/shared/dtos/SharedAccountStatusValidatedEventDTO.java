package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Event DTO for account status validation response from Account Management Shred.
 * Used to confirm if an account is in valid state for authentication.
 */
public class SharedAccountStatusValidatedEventDTO implements Serializable {

    @NotBlank(message = "Account ID must not be blank")
    private String accountId;

    @NotNull(message = "Validation flag must not be null")
    private Boolean isValid;

    @NotBlank(message = "Account status must not be blank")
    private String accountStatus;

    public SharedAccountStatusValidatedEventDTO() {}

    public SharedAccountStatusValidatedEventDTO(String accountId, Boolean isValid, String accountStatus) {
        this.accountId = accountId;
        this.isValid = isValid;
        this.accountStatus = accountStatus;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    @Override
    public String toString() {
        return "SharedAccountStatusValidatedEventDTO{" +
                "accountId='" + accountId + '\'' +
                ", isValid=" + isValid +
                ", accountStatus='" + accountStatus + '\'' +
                '}';
    }
}