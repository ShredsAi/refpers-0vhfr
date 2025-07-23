package ai.shreds.domain.dtos;

import ai.shreds.domain.enums.DomainTransactionTypeEnum;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.application.dtos.ApplicationTransactionDTO;
import java.time.LocalDateTime;

public class DomainTransaction {
    private final String transactionId;
    private final DomainMoneyValue amount;
    private final DomainTransactionTypeEnum type;
    private final String description;
    private final String reference;
    private final LocalDateTime timestamp;

    public DomainTransaction(String transactionId,
                           DomainMoneyValue amount,
                           DomainTransactionTypeEnum type,
                           String description,
                           String reference,
                           LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.reference = reference;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public DomainMoneyValue getAmount() {
        return amount;
    }

    public DomainTransactionTypeEnum getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public ApplicationTransactionDTO toApplicationDTO() {
        return ApplicationTransactionDTO.fromDomainTransaction(this);
    }
}