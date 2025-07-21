package ai.shreds.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for MFA verification request containing challenge ID and verification code.
 * Used to complete multi-factor authentication.
 */
public class SharedMfaVerifyRequestDTO {
    
    @NotBlank(message = "Challenge ID is required")
    private String challengeId;
    
    @NotBlank(message = "Verification code is required")
    @Size(min = 4, max = 10, message = "Verification code must be between 4 and 10 characters")
    @Pattern(regexp = "^[0-9]+$", message = "Verification code must contain only digits")
    private String code;

    public SharedMfaVerifyRequestDTO() {}

    public SharedMfaVerifyRequestDTO(String challengeId, String code) {
        this.challengeId = challengeId;
        this.code = code;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "SharedMfaVerifyRequestDTO{" +
                "challengeId='" + challengeId + '\'' +
                ", code='[PROTECTED]'" +
                '}';
    }
}