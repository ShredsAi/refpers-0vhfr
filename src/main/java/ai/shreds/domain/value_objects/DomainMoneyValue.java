package ai.shreds.domain.value_objects;

import java.math.BigDecimal;
import java.util.Objects;
import ai.shreds.application.dtos.ApplicationMoneyValue;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DomainMoneyValue {
    private final BigDecimal amount;
    private final DomainCurrencyValue currency;

    public DomainMoneyValue(BigDecimal amount, DomainCurrencyValue currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null || !currency.validate()) {
            throw new IllegalArgumentException("Currency is invalid");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public DomainMoneyValue add(DomainMoneyValue other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency + " vs " + other.currency);
        }
        return new DomainMoneyValue(amount.add(other.amount), currency);
    }

    public DomainMoneyValue subtract(DomainMoneyValue other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency + " vs " + other.currency);
        }
        return new DomainMoneyValue(amount.subtract(other.amount), currency);
    }

    public boolean isGreaterThan(DomainMoneyValue other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency + " vs " + other.currency);
        }
        return amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(DomainMoneyValue other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency + " vs " + other.currency);
        }
        return amount.compareTo(other.amount) < 0;
    }

    public boolean equals(DomainMoneyValue other) {
        if (other == null) {
            return false;
        }
        return amount.compareTo(other.amount) == 0 && currency.equals(other.currency);
    }

    public ApplicationMoneyValue toApplicationMoneyValue() {
        return ApplicationMoneyValue.fromDomainMoneyValue(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainMoneyValue)) return false;
        DomainMoneyValue that = (DomainMoneyValue) o;
        return amount.compareTo(that.amount) == 0 && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}