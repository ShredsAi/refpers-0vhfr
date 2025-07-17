package ai.shreds.shared.value_objects;

import java.util.Objects;

public class SharedValueRouteId {
    private final String value;

    public SharedValueRouteId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("RouteId cannot be null or empty");
        }
        this.value = value;
        validate();
    }

    public String getValue() {
        return value;
    }

    public void validate() {
        // Alphanumeric and hyphens allowed
        if (!value.matches("^[A-Za-z0-9\\-]+$")) {
            throw new IllegalArgumentException("Invalid RouteId format: " + value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedValueRouteId that = (SharedValueRouteId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "SharedValueRouteId{" +
                "value='" + value + '\'' +
                '}';
    }
}
