package ai.shreds.application.dtos;

import ai.shreds.domain.value_objects.DomainMoneyValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationBalanceDTO {
    private ApplicationMoneyValue balance;
    private String lastUpdated;
    
    public static ApplicationBalanceDTO fromDomainBalance(DomainMoneyValue domainBalance, Instant lastUpdated) {
        if (domainBalance == null || lastUpdated == null) {
            return null;
        }
        return ApplicationBalanceDTO.builder()
            .balance(ApplicationMoneyValue.fromDomainMoneyValue(domainBalance))
            .lastUpdated(lastUpdated.toString())
            .build();
    }
    
    public DomainMoneyValue toDomainBalance() {
        return balance != null ? balance.toDomainMoneyValue() : null;
    }
}