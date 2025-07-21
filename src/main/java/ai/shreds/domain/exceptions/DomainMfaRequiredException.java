package ai.shreds.domain.exceptions;

/**
 * Exception thrown when multi-factor authentication is required.
 */
public class DomainMfaRequiredException extends DomainException {

    private final String challengeId;

    /**
     * Constructs a new DomainMfaRequiredException with the specified detail message and challenge ID.
     *
     * @param message the detail message
     * @param challengeId the ID of the MFA challenge
     */
    public DomainMfaRequiredException(String message, String challengeId) {
        super(message, "MFA_REQUIRED");
        this.challengeId = challengeId;
    }

    /**
     * Returns the MFA challenge ID associated with this exception.
     *
     * @return the challenge ID
     */
    public String getChallengeId() {
        return challengeId;
    }
}
