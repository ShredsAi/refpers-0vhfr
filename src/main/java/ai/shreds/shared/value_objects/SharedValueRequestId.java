package ai.shreds.shared.value_objects;

import java.util.UUID;

public class SharedValueRequestId {
    private final String value;

    public SharedValueRequestId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("RequestId cannot be null or empty");
        }
        this.value = value;
        validate();
    }

    public String getValue() {
        return value;
    }

    public void validate() {
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid RequestId format. Must be a valid UUID: " + value);
        }
    }

    public static SharedValueRequestId generate() {
        return new SharedValueRequestId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedValueRequestId that = (SharedValueRequestId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
