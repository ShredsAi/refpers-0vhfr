package ai.shreds.shared.value_objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public class SharedValueUserId {
    private final String value;

    @JsonCreator
    public SharedValueUserId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        this.value = value;
        validate();
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public void validate() {
        // UserId should be alphanumeric and can contain dashes, underscores
        if (!value.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Invalid UserId format. Must be alphanumeric with dashes and underscores allowed: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedValueUserId that = (SharedValueUserId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
