package ai.shreds.domain.exceptions;

public class DomainInvalidCurrencyException extends RuntimeException {
    private final String expectedCurrency;
    private final String receivedCurrency;

    public DomainInvalidCurrencyException(String expectedCurrency, String receivedCurrency) {
        super(String.format("Invalid currency: expected %s but received %s", expectedCurrency, receivedCurrency));
        this.expectedCurrency = expectedCurrency;
        this.receivedCurrency = receivedCurrency;
    }

    public String getExpectedCurrency() {
        return expectedCurrency;
    }

    public String getReceivedCurrency() {
        return receivedCurrency;
    }
}