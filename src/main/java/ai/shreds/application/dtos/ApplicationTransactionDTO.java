package ai.shreds.application.dtos;

import ai.shreds.domain.dtos.DomainTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTransactionDTO {
    private String transactionId;
    private ApplicationMoneyValue amount;
    private String type;
    private String description;
    private String reference;
    private String timestamp;

    public static ApplicationTransactionDTO fromDomainTransaction(DomainTransaction transaction) {
        if (Objects.isNull(transaction)) {
            return null;
        }
        return ApplicationTransactionDTO.builder()
            .transactionId(transaction.getTransactionId())
            .amount(ApplicationMoneyValue.fromDomainMoneyValue(transaction.getAmount()))
            .type(transaction.getType().name())
            .description(transaction.getDescription())
            .reference(transaction.getReference())
            .timestamp(transaction.getTimestamp().toString())
            .build();
    }
}