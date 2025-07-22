package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for account data transfer between layers.
 * Contains basic account information for authentication purposes.
 */
public class SharedAccountDTO implements Serializable {
    
    @NotBlank(message = "Account ID must not be blank")
    private String accountId;
    
    @NotBlank(message = "Username must not be blank")
    private String username;
    
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Account status must not be blank")
    private String accountStatus;
    
    @NotBlank(message = "Creation timestamp must not be blank")
    private String createdAt;
    
    private String lastLoginAt;
    private String phoneNumber;
    private Boolean emailVerified;
    private Boolean phoneVerified;

    public SharedAccountDTO() {}

    public SharedAccountDTO(
            String accountId,
            String username,
            String email,
            String accountStatus,
            String createdAt,
            String lastLoginAt) {
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public SharedAccountDTO(
            String accountId,
            String username,
            String email,
            String accountStatus,
            String createdAt,
            String lastLoginAt,
            String phoneNumber,
            Boolean emailVerified,
            Boolean phoneVerified) {
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.phoneNumber = phoneNumber;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
    }

    // Convenience methods
    public boolean isActive() {
        return "ACTIVE".equals(accountStatus);
    }

    public boolean hasLoggedInBefore() {
        return lastLoginAt != null;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(String lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    @Override
    public String toString() {
        return "SharedAccountDTO{" +
                "accountId='" + accountId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", lastLoginAt='" + lastLoginAt + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", emailVerified=" + emailVerified +
                ", phoneVerified=" + phoneVerified +
                '}';
    }
}