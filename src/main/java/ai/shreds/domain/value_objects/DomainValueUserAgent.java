package ai.shreds.domain.value_objects;

/**
 * Value object representing a user agent string with validation.
 */
public class DomainValueUserAgent {
    private final String value;
    private static final int MAX_LENGTH = 512;

    public DomainValueUserAgent(String value) {
        this.value = value;
        validate();
    }

    private void validate() {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("User agent cannot be null or empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("User agent exceeds maximum length of " + MAX_LENGTH + " characters");
        }
        if (!value.matches("^[\\x20-\\x7E]*$")) {
            throw new IllegalArgumentException("User agent contains invalid characters");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainValueUserAgent that = (DomainValueUserAgent) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}