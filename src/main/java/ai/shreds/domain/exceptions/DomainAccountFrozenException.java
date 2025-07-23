package ai.shreds.domain.exceptions;

public class DomainAccountFrozenException extends RuntimeException {
    private final String accountId;
    private final String reason;

    public DomainAccountFrozenException(String accountId, String reason) {
        super(String.format("Account %s is frozen: %s", accountId, reason));
        this.accountId = accountId;
        this.reason = reason;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getReason() {
        return reason;
    }
}