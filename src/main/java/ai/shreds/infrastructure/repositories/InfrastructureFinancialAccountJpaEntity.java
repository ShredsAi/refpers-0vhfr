package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityFinancialAccount;
import ai.shreds.domain.enums.DomainAccountStatusEnum;
import ai.shreds.domain.value_objects.DomainCurrencyValue;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "financial_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfrastructureFinancialAccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "financial_account_id")
    private UUID financialAccountId;

    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;

    @Column(name = "balance_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAmount;

    @Column(name = "balance_currency", nullable = false, length = 3)
    private String balanceCurrency;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (version == null) {
            version = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public DomainEntityFinancialAccount toDomainEntity() {
        DomainCurrencyValue currency = new DomainCurrencyValue(balanceCurrency, getCurrencySymbol(balanceCurrency));
        DomainMoneyValue balance = new DomainMoneyValue(balanceAmount, currency);
        DomainAccountStatusEnum statusEnum = DomainAccountStatusEnum.valueOf(status);
        
        return new DomainEntityFinancialAccount(
                financialAccountId,
                accountId,
                balance,
                statusEnum,
                version,
                createdAt,
                updatedAt
        );
    }

    public static InfrastructureFinancialAccountJpaEntity fromDomainEntity(DomainEntityFinancialAccount domain) {
        InfrastructureFinancialAccountJpaEntity entity = new InfrastructureFinancialAccountJpaEntity();
        entity.setFinancialAccountId(domain.getFinancialAccountId());
        entity.setAccountId(domain.getAccountId());
        entity.setBalanceAmount(domain.getBalance().getAmount());
        entity.setBalanceCurrency(domain.getBalance().getCurrency().getCode());
        entity.setStatus(domain.getStatus().name());
        entity.setVersion(domain.getVersion());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private String getCurrencySymbol(String currencyCode) {
        return switch (currencyCode) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "JPY" -> "¥";
            default -> currencyCode;
        };
    }
}