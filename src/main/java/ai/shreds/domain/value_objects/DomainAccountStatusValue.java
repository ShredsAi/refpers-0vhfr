package ai.shreds.domain.value_objects;

import ai.shreds.shared.enums.SharedAccountStatusTypeEnum;

import java.time.Instant;
import java.util.Objects;

public class DomainAccountStatusValue {

    private final SharedAccountStatusTypeEnum status;
    private final String reason;
    private final Instant changedAt;

    public DomainAccountStatusValue(SharedAccountStatusTypeEnum status, String reason) {
        this.status = status;
        this.reason = reason;
        this.changedAt = Instant.now();
        validate();
    }

    public DomainAccountStatusValue(SharedAccountStatusTypeEnum status, String reason, Instant changedAt) {
        this.status = status;
        this.reason = reason;
        this.changedAt = changedAt;
        validate();
    }

    public SharedAccountStatusTypeEnum getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public boolean isActive() {
        return SharedAccountStatusTypeEnum.ACTIVE.equals(status);
    }

    private void validate() {
        if (status == null) {
            throw new IllegalArgumentException("Account status cannot be null");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Status change reason cannot be null or empty");
        }
        if (changedAt == null) {
            throw new IllegalArgumentException("Changed at timestamp cannot be null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainAccountStatusValue that = (DomainAccountStatusValue) o;
        return status == that.status &&
               Objects.equals(reason, that.reason) &&
               Objects.equals(changedAt, that.changedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, reason, changedAt);
    }

    @Override
    public String toString() {
        return "DomainAccountStatusValue{" +
                "status=" + status +
                ", reason='" + reason + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
