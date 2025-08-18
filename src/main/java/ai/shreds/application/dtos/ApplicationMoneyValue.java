package ai.shreds.application.dtos;

import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.domain.value_objects.DomainCurrencyValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationMoneyValue {
    private String amount;
    private String currency;
    
    public DomainMoneyValue toDomainMoneyValue() {
        // Get currency symbol - for simplicity, using common symbols
        String symbol = getCurrencySymbol(currency);
        DomainCurrencyValue domainCurrency = new DomainCurrencyValue(currency, symbol);
        return new DomainMoneyValue(new BigDecimal(amount), domainCurrency);
    }
    
    public static ApplicationMoneyValue fromDomainMoneyValue(DomainMoneyValue value) {
        if (value == null) {
            return null;
        }
        return ApplicationMoneyValue.builder()
            .amount(value.getAmount().toString())
            .currency(value.getCurrency().getCode())
            .build();
    }
    
    private String getCurrencySymbol(String currencyCode) {
        switch (currencyCode) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            default: return currencyCode;
        }
    }
}