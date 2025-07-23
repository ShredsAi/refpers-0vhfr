package ai.shreds.application.dtos;

import ai.shreds.domain.value_objects.DomainMoneyValue;
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
        return DomainMoneyValue.builder()
            .amount(new BigDecimal(amount))
            .currency(currency)
            .build();
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
}