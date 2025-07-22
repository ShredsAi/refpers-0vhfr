package ai.shreds.application.dtos;

import ai.shreds.shared.dtos.SharedLoginResponseDTO;

public class ApplicationMfaChallengeDTO {
    private String challengeId;
    private String accountId;
    private String method;
    private long expiresAt;

    public ApplicationMfaChallengeDTO() {}

    public ApplicationMfaChallengeDTO(String challengeId, String accountId, String method, long expiresAt) {
        this.challengeId = challengeId;
        this.accountId = accountId;
        this.method = method;
        this.expiresAt = expiresAt;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public SharedLoginResponseDTO toLoginResponse() {
        SharedLoginResponseDTO response = new SharedLoginResponseDTO();
        response.setMfaRequired(true);
        response.setChallengeId(this.challengeId);
        return response;
    }
}