package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for password reset request containing user's email address.
 * Used to initiate password reset workflow.
 */
public class SharedPasswordResetRequestDTO implements Serializable {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String email;

    public SharedPasswordResetRequestDTO() {
    }

    public SharedPasswordResetRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "SharedPasswordResetRequestDTO{" +
                "email='" + email + '\'' +
                '}';
    }
}