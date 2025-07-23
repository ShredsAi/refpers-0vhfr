package ai.shreds.domain.exceptions;

import java.math.BigDecimal;

public class DomainInsufficientFundsException extends RuntimeException {
    private final String accountId;
    private final BigDecimal requestedAmount;
    private final BigDecimal availableBalance;

    public DomainInsufficientFundsException(String accountId, BigDecimal requestedAmount, BigDecimal availableBalance) {
        super(String.format("Insufficient funds in account %s. Requested: %s, Available: %s", accountId, requestedAmount, availableBalance));
        this.accountId = accountId;
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
}