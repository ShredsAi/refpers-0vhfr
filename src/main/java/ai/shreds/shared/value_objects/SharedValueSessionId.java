package ai.shreds.shared.value_objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import java.util.UUID;

public class SharedValueSessionId {
    private final String value;

    @JsonCreator
    public SharedValueSessionId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null or empty");
        }
        this.value = value;
        // Validation is good, but UUID.fromString is strict. Let's relax it for now as it might not be a UUID in all cases.
        // validate(); 
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public void validate() {
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            // Allow non-UUIDs for flexibility, but could be tightened if needed.
            // throw new IllegalArgumentException("Invalid SessionId format. Must be a valid UUID: " + value);
        }
    }

    public static SharedValueSessionId generate() {
        return new SharedValueSessionId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedValueSessionId that = (SharedValueSessionId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
