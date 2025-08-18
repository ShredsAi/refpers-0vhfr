package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainEntityFinancialAccount;
import ai.shreds.domain.enums.DomainAccountStatusEnum;
import ai.shreds.domain.value_objects.DomainCurrencyValue;
import ai.shreds.domain.value_objects.DomainMoneyValue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DomainFinancialAccountFactory {

    /**
     * Creates a new financial account with zero balance and specified currency.
     */
    public DomainEntityFinancialAccount createNewAccount(UUID accountId, String currency) {
        DomainCurrencyValue currencyValue = new DomainCurrencyValue(currency, getCurrencySymbol(currency));
        DomainMoneyValue zeroBalance = new DomainMoneyValue(BigDecimal.ZERO, currencyValue);
        
        LocalDateTime now = LocalDateTime.now();
        
        return new DomainEntityFinancialAccount(
            UUID.randomUUID(), // financialAccountId
            accountId,
            zeroBalance,
            DomainAccountStatusEnum.ACTIVE,
            0L, // initial version
            now, // createdAt
            now  // updatedAt
        );
    }

    /**
     * Creates a financial account with zero balance using a DomainCurrencyValue.
     */
    public DomainEntityFinancialAccount createWithZeroBalance(UUID accountId, DomainCurrencyValue currency) {
        DomainMoneyValue zeroBalance = new DomainMoneyValue(BigDecimal.ZERO, currency);
        LocalDateTime now = LocalDateTime.now();
        
        return new DomainEntityFinancialAccount(
            UUID.randomUUID(), // financialAccountId
            accountId,
            zeroBalance,
            DomainAccountStatusEnum.ACTIVE,
            0L, // initial version
            now, // createdAt
            now  // updatedAt
        );
    }

    /**
     * Helper method to get currency symbol from code.
     */
    private String getCurrencySymbol(String currencyCode) {
        switch (currencyCode.toUpperCase()) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "CAD": return "C$";
            case "AUD": return "A$";
            default: return currencyCode; // fallback to currency code as symbol
        }
    }
}