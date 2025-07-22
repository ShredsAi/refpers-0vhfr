package ai.shreds.domain.exceptions;

/**
 * Exception thrown when an account is not in ACTIVE status during authentication.
 */
public class DomainAccountNotActiveException extends DomainException {

    private static final String ERROR_CODE = "ACCOUNT_NOT_ACTIVE";
    private final String accountStatus;

    /**
     * Constructs a new DomainAccountNotActiveException with the specified detail message and account status.
     *
     * @param message the detail message
     * @param accountStatus the current status of the account
     */
    public DomainAccountNotActiveException(String message, String accountStatus) {
        super(message, ERROR_CODE);
        this.accountStatus = accountStatus;
    }

    /**
     * Returns the current status of the account.
     *
     * @return the account status
     */
    public String getAccountStatus() {
        return accountStatus;
    }
}
