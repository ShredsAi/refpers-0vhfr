package ai.shreds.application.dtos;

import ai.shreds.domain.dtos.DomainTransaction;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTransactionResponseDTO {
    private String transactionId;
    private ApplicationMoneyValue newBalance;
    private String processedAt;
    
    public static ApplicationTransactionResponseDTO fromDomainTransaction(
            DomainTransaction transaction, 
            DomainMoneyValue newBalance) {
        if (Objects.isNull(transaction)) {
            return null;
        }
        return ApplicationTransactionResponseDTO.builder()
            .transactionId(transaction.getTransactionId())
            .newBalance(ApplicationMoneyValue.fromDomainMoneyValue(newBalance))
            .processedAt(transaction.getTimestamp().toString())
            .build();
    }
}