package ai.shreds.shared.dtos;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;

/**
 * Event DTO for requesting step-up authentication for sensitive operations.
 * Consumed from other shreds like Financial & Payment Management.
 */
public class SharedSensitiveOperationRequestEventDTO implements Serializable {

    @NotBlank(message = "Operation type must not be blank")
    private String operationType;

    @NotBlank(message = "Account ID must not be blank")
    private String accountId;

    @NotBlank(message = "Required auth level must not be blank")
    private String requiredAuthLevel;

    private String operationId; // Additional field for tracking the operation
    private String sessionId; // Current session requesting the operation

    public SharedSensitiveOperationRequestEventDTO() {}

    public SharedSensitiveOperationRequestEventDTO(String operationType, String accountId, String requiredAuthLevel) {
        this.operationType = operationType;
        this.accountId = accountId;
        this.requiredAuthLevel = requiredAuthLevel;
    }

    public SharedSensitiveOperationRequestEventDTO(String operationType, String accountId, String requiredAuthLevel, String operationId, String sessionId) {
        this.operationType = operationType;
        this.accountId = accountId;
        this.requiredAuthLevel = requiredAuthLevel;
        this.operationId = operationId;
        this.sessionId = sessionId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getRequiredAuthLevel() {
        return requiredAuthLevel;
    }

    public void setRequiredAuthLevel(String requiredAuthLevel) {
        this.requiredAuthLevel = requiredAuthLevel;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "SharedSensitiveOperationRequestEventDTO{" +
                "operationType='" + operationType + '\'' +
                ", accountId='" + accountId + '\'' +
                ", requiredAuthLevel='" + requiredAuthLevel + '\'' +
                ", operationId='" + operationId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}