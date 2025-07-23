package ai.shreds.domain.entities;

import ai.shreds.domain.dtos.DomainTransaction;
import ai.shreds.domain.enums.DomainTransactionTypeEnum;
import ai.shreds.domain.value_objects.DomainCurrencyValue;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DomainEntityTransaction {
    private final UUID transactionId;
    private final UUID financialAccountId;
    private final BigDecimal amount;
    private final String currency;
    private final DomainTransactionTypeEnum type;
    private final String description;
    private final String reference;
    private final LocalDateTime timestamp;

    public DomainEntityTransaction(UUID transactionId,
                                   UUID financialAccountId,
                                   BigDecimal amount,
                                   String currency,
                                   DomainTransactionTypeEnum type,
                                   String description,
                                   String reference,
                                   LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.financialAccountId = financialAccountId;
        this.amount = amount;
        this.currency = currency;
        this.type = type;
        this.description = description;
        this.reference = reference;
        this.timestamp = timestamp;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getFinancialAccountId() {
        return financialAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
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

    public boolean isCredit() {
        return DomainTransactionTypeEnum.CREDIT.equals(type);
    }

    public boolean isDebit() {
        return DomainTransactionTypeEnum.DEBIT.equals(type);
    }

    public BigDecimal getSignedAmount() {
        return isDebit() ? amount.negate() : amount;
    }

    /**
     * Converts this entity to the domain DTO representation.
     */
    public DomainTransaction toDomainModel() {
        DomainMoneyValue mv = new DomainMoneyValue(amount, new DomainCurrencyValue(currency, currency));
        return new DomainTransaction(
            transactionId.toString(),
            mv,
            type,
            description,
            reference,
            timestamp
        );
    }
}