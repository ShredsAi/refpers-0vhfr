package ai.shreds.domain.ports;

import ai.shreds.domain.value_objects.DomainMoneyValue;
import java.math.BigDecimal;

public interface DomainOutputPortCurrencyService {
    DomainMoneyValue convertCurrency(DomainMoneyValue amount, String targetCurrency);
    BigDecimal getExchangeRate(String fromCurrency, String toCurrency);
}