package ai.shreds.domain.exceptions;

/**
 * Exception thrown when a JWT token's audience claim doesn't match the expected audience.
 */
public class DomainExceptionInvalidAudience extends RuntimeException {
    private final String actualAudience;
    private final String expectedAudience;
    private static final String DEFAULT_MESSAGE = "Invalid token audience";

    /**
     * Constructs a new exception with the actual and expected audience values.
     *
     * @param actualAudience the audience found in the token
     * @param expectedAudience the audience that was expected
     */
    public DomainExceptionInvalidAudience(String actualAudience, String expectedAudience) {
        super(formatMessage(actualAudience, expectedAudience));
        this.actualAudience = actualAudience != null ? actualAudience : "[null]";
        this.expectedAudience = expectedAudience != null ? expectedAudience : "[null]";
    }

    /**
     * Gets the actual audience from the token.
     *
     * @return the actual audience
     */
    public String getActualAudience() {
        return actualAudience;
    }

    /**
     * Gets the expected audience value.
     *
     * @return the expected audience
     */
    public String getExpectedAudience() {
        return expectedAudience;
    }

    private static String formatMessage(String actual, String expected) {
        return String.format("%s. Expected: %s, but got: %s", 
            DEFAULT_MESSAGE,
            expected != null ? expected : "[null]",
            actual != null ? actual : "[null]");
    }

    @Override
    public String toString() {
        return String.format("%s{actualAudience='%s', expectedAudience='%s'}",
            getClass().getSimpleName(), actualAudience, expectedAudience);
    }
}