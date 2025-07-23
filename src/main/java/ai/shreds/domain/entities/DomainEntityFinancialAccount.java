package ai.shreds.domain.entities;

import ai.shreds.domain.exceptions.DomainInsufficientFundsException;
import ai.shreds.domain.exceptions.DomainAccountFrozenException;
import ai.shreds.domain.exceptions.DomainInvalidCurrencyException;
import ai.shreds.domain.enums.DomainAccountStatusEnum;
import ai.shreds.domain.enums.DomainTransactionTypeEnum;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import java.time.LocalDateTime;
import java.util.UUID;

public class DomainEntityFinancialAccount {
    private UUID financialAccountId;
    private UUID accountId;
    private DomainMoneyValue balance;
    private DomainAccountStatusEnum status;
    private long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DomainEntityFinancialAccount(UUID financialAccountId,
                                        UUID accountId,
                                        DomainMoneyValue balance,
                                        DomainAccountStatusEnum status,
                                        long version,
                                        LocalDateTime createdAt,
                                        LocalDateTime updatedAt) {
        this.financialAccountId = financialAccountId;
        this.accountId = accountId;
        this.balance = balance;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getFinancialAccountId() {
        return financialAccountId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public DomainMoneyValue getBalance() {
        return balance;
    }

    public DomainAccountStatusEnum getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public DomainEntityTransaction credit(DomainMoneyValue amount, String description, String reference) {
        if (status != DomainAccountStatusEnum.ACTIVE) {
            throw new DomainAccountFrozenException(accountId.toString(), "Account is not active");
        }
        if (!balance.getCurrency().equals(amount.getCurrency())) {
            throw new DomainInvalidCurrencyException(balance.getCurrency().getCode(), amount.getCurrency().getCode());
        }
        DomainMoneyValue newBalance = balance.add(amount);
        this.balance = newBalance;
        this.updatedAt = LocalDateTime.now();
        return new DomainEntityTransaction(
                UUID.randomUUID(),
                financialAccountId,
                amount.getAmount(),
                amount.getCurrency().getCode(),
                DomainTransactionTypeEnum.CREDIT,
                description,
                reference,
                this.updatedAt
        );
    }

    public DomainEntityTransaction debit(DomainMoneyValue amount, String description, String reference) {
        if (status != DomainAccountStatusEnum.ACTIVE) {
            throw new DomainAccountFrozenException(accountId.toString(), "Account is not active");
        }
        if (!balance.getCurrency().equals(amount.getCurrency())) {
            throw new DomainInvalidCurrencyException(balance.getCurrency().getCode(), amount.getCurrency().getCode());
        }
        if (balance.subtract(amount).getAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new DomainInsufficientFundsException(accountId.toString(), amount.getAmount(), balance.getAmount());
        }
        DomainMoneyValue newBalance = balance.subtract(amount);
        this.balance = newBalance;
        this.updatedAt = LocalDateTime.now();
        return new DomainEntityTransaction(
                UUID.randomUUID(),
                financialAccountId,
                amount.getAmount(),
                amount.getCurrency().getCode(),
                DomainTransactionTypeEnum.DEBIT,
                description,
                reference,
                this.updatedAt
        );
    }

    public boolean canDebit(DomainMoneyValue amount) {
        return balance.subtract(amount).getAmount().compareTo(java.math.BigDecimal.ZERO) >= 0;
    }

    public void freeze() {
        this.status = DomainAccountStatusEnum.FROZEN;
        this.updatedAt = LocalDateTime.now();
    }

    public void close() {
        this.status = DomainAccountStatusEnum.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }
}