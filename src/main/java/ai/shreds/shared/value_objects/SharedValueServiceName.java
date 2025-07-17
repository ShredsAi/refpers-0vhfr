package ai.shreds.shared.value_objects;

public class SharedValueServiceName {
    private final String value;

    public SharedValueServiceName(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be null or empty");
        }
        this.value = value;
        validate();
    }

    public String getValue() {
        return value;
    }

    public void validate() {
        if (!value.matches("^[a-zA-Z0-9\\-_]+$")) {
            throw new IllegalArgumentException("Invalid service name format. Must be alphanumeric with hyphens and underscores: " + value);
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
        SharedValueServiceName that = (SharedValueServiceName) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
