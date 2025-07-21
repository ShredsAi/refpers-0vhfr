package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for login response that can contain tokens or MFA challenge information.
 * When MFA is required, tokens will be null and challengeId will be provided.
 */
public class SharedLoginResponseDTO implements Serializable {

    private String accessToken;

    private String refreshToken;

    @NotNull(message = "MFA required flag must not be null")
    private Boolean mfaRequired;

    private String challengeId;

    public SharedLoginResponseDTO() {}

    public SharedLoginResponseDTO(String accessToken, String refreshToken, Boolean mfaRequired, String challengeId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.mfaRequired = mfaRequired != null ? mfaRequired : false;
        this.challengeId = challengeId;
    }

    // Static factory methods for cleaner object creation
    public static SharedLoginResponseDTO withTokens(String accessToken, String refreshToken) {
        return new SharedLoginResponseDTO(accessToken, refreshToken, false, null);
    }

    public static SharedLoginResponseDTO withMfaChallenge(String challengeId) {
        return new SharedLoginResponseDTO(null, null, true, challengeId);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Boolean getMfaRequired() {
        return mfaRequired;
    }

    public void setMfaRequired(Boolean mfaRequired) {
        this.mfaRequired = mfaRequired;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    @Override
    public String toString() {
        return "SharedLoginResponseDTO{" +
                "accessToken='" + (accessToken != null ? "[PROTECTED]" : "null") + '\'' +
                ", refreshToken='" + (refreshToken != null ? "[PROTECTED]" : "null") + '\'' +
                ", mfaRequired=" + mfaRequired +
                ", challengeId='" + challengeId + '\'' +
                '}';
    }
}