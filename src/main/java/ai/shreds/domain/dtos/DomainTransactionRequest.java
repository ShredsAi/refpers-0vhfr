package ai.shreds.domain.dtos;

import ai.shreds.domain.enums.DomainTransactionTypeEnum;
import ai.shreds.domain.value_objects.DomainMoneyValue;

public class DomainTransactionRequest {
    private final DomainMoneyValue amount;
    private final DomainTransactionTypeEnum type;
    private final String description;
    private final String reference;

    public DomainTransactionRequest(DomainMoneyValue amount,
                                   DomainTransactionTypeEnum type,
                                   String description,
                                   String reference) {
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.reference = reference;
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

    public boolean validate() {
        if (amount == null) {
            return false;
        }
        if (type == null) {
            return false;
        }
        if (description == null || description.trim().isEmpty()) {
            return false;
        }
        return amount.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0;
    }
}