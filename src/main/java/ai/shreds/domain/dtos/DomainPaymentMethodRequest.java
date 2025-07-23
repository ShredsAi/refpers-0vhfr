package ai.shreds.domain.dtos;

import ai.shreds.domain.value_objects.DomainAddressValue;
import java.time.Year;

public class DomainPaymentMethodRequest {
    private final String accountId;
    private final String cardNumber;
    private final Integer expiryMonth;
    private final Integer expiryYear;
    private final String cvv;
    private final DomainAddressValue billingAddress;

    public DomainPaymentMethodRequest(String accountId,
                                      String cardNumber,
                                      Integer expiryMonth,
                                      Integer expiryYear,
                                      String cvv,
                                      DomainAddressValue billingAddress) {
        this.accountId = accountId;
        this.cardNumber = cardNumber;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
        this.billingAddress = billingAddress;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    public Integer getExpiryYear() {
        return expiryYear;
    }

    public String getCvv() {
        return cvv;
    }

    public DomainAddressValue getBillingAddress() {
        return billingAddress;
    }

    public boolean validate() {
        if (accountId == null || accountId.isEmpty()) {
            return false;
        }
        if (cardNumber == null || cardNumber.length() < 12) {
            return false;
        }
        if (expiryMonth == null || expiryMonth < 1 || expiryMonth > 12) {
            return false;
        }
        int currentYear = Year.now().getValue();
        if (expiryYear == null || expiryYear < currentYear) {
            return false;
        }
        if (cvv == null || cvv.length() < 3 || cvv.length() > 4) {
            return false;
        }
        if (billingAddress == null || !billingAddress.validate()) {
            return false;
        }
        return true;
    }
}