package ai.shreds.domain.exceptions;

/**
 * Exception thrown when a JWT token's issuer claim doesn't match the expected issuer.
 */
public class DomainExceptionInvalidIssuer extends RuntimeException {
    private final String actualIssuer;
    private final String expectedIssuer;
    private static final String DEFAULT_MESSAGE = "Invalid token issuer";

    /**
     * Constructs a new exception with the actual and expected issuer values.
     *
     * @param actualIssuer the issuer found in the token
     * @param expectedIssuer the issuer that was expected
     */
    public DomainExceptionInvalidIssuer(String actualIssuer, String expectedIssuer) {
        super(formatMessage(actualIssuer, expectedIssuer));
        this.actualIssuer = actualIssuer != null ? actualIssuer : "[null]";
        this.expectedIssuer = expectedIssuer != null ? expectedIssuer : "[null]";
    }

    /**
     * Gets the actual issuer from the token.
     *
     * @return the actual issuer
     */
    public String getActualIssuer() {
        return actualIssuer;
    }

    /**
     * Gets the expected issuer value.
     *
     * @return the expected issuer
     */
    public String getExpectedIssuer() {
        return expectedIssuer;
    }

    private static String formatMessage(String actual, String expected) {
        return String.format("%s. Expected: %s, but got: %s", 
            DEFAULT_MESSAGE,
            expected != null ? expected : "[null]",
            actual != null ? actual : "[null]");
    }

    @Override
    public String toString() {
        return String.format("%s{actualIssuer='%s', expectedIssuer='%s'}",
            getClass().getSimpleName(), actualIssuer, expectedIssuer);
    }
}