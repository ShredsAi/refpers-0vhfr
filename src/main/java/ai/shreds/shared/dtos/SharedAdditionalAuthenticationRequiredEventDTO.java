package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;

/**
 * Event DTO for notifying other shreds that additional authentication is required.
 * Published when step-up MFA challenge is initiated for sensitive operations.
 */
public class SharedAdditionalAuthenticationRequiredEventDTO implements Serializable {

    @NotBlank(message = "Operation ID must not be blank")
    private String operationId;

    @NotBlank(message = "Challenge ID must not be blank")
    private String challengeId;

    @NotBlank(message = "Method must not be blank")
    private String method;

    private String accountId; // Additional field for tracking which account
    private String expiresAt; // When the challenge expires

    public SharedAdditionalAuthenticationRequiredEventDTO() {}

    public SharedAdditionalAuthenticationRequiredEventDTO(String operationId, String challengeId, String method) {
        this.operationId = operationId;
        this.challengeId = challengeId;
        this.method = method;
    }

    public SharedAdditionalAuthenticationRequiredEventDTO(String operationId, String challengeId, String method, String accountId, String expiresAt) {
        this.operationId = operationId;
        this.challengeId = challengeId;
        this.method = method;
        this.accountId = accountId;
        this.expiresAt = expiresAt;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "SharedAdditionalAuthenticationRequiredEventDTO{" +
                "operationId='" + operationId + '\'' +
                ", challengeId='" + challengeId + '\'' +
                ", method='" + method + '\'' +
                ", accountId='" + accountId + '\'' +
                ", expiresAt='" + expiresAt + '\'' +
                '}';
    }
}