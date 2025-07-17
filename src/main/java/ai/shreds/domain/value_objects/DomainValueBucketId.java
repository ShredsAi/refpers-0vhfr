package ai.shreds.domain.value_objects;

import java.util.UUID;

/**
 * Value object representing a unique identifier for a rate limit bucket.
 * Provides methods for generating and validating bucket IDs used in rate limiting.
 */
public class DomainValueBucketId {
    private final String value;
    private static final int MAX_LENGTH = 128;
    private static final String VALID_CHARS_PATTERN = "^[a-zA-Z0-9_:-]+$";
    private static final String DELIMITER = ":"; 

    public DomainValueBucketId(String value) {
        this.value = value;
        validate();
    }

    /**
     * Creates a new bucket ID with a random UUID.
     *
     * @return a new bucket ID instance
     */
    public static DomainValueBucketId generate() {
        return new DomainValueBucketId(UUID.randomUUID().toString());
    }

    /**
     * Creates a bucket ID from user ID and route ID combination.
     * This method ensures consistent bucket ID generation for user-route pairs.
     *
     * @param userId the user identifier
     * @param routeId the route identifier
     * @return a new bucket ID instance
     * @throws IllegalArgumentException if either userId or routeId is null
     */
    public static DomainValueBucketId fromUserAndRoute(String userId, String routeId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (routeId == null || routeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be null or empty");
        }
        String combinedId = String.format("%s%s%s", userId, DELIMITER, routeId);
        if (combinedId.length() > MAX_LENGTH) {
            // If combined ID is too long, use a hash-based approach
            combinedId = String.format("%s%s%d", 
                userId.substring(0, Math.min(userId.length(), 32)),
                DELIMITER,
                routeId.hashCode());
        }
        return new DomainValueBucketId(combinedId);
    }

    private void validate() {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket ID cannot be null or empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(String.format(
                "Bucket ID exceeds maximum length of %d characters", MAX_LENGTH));
        }
        if (!value.matches(VALID_CHARS_PATTERN)) {
            throw new IllegalArgumentException(
                "Bucket ID contains invalid characters. Only alphanumeric, underscore, colon, and hyphen are allowed.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainValueBucketId that = (DomainValueBucketId) o;
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