package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityTransaction;
import ai.shreds.domain.enums.DomainTransactionTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfrastructureTransactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "financial_account_id", nullable = false)
    private UUID financialAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "type", nullable = false, length = 10)
    private String type;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference", length = 255)
    private String reference;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public DomainEntityTransaction toDomainEntity() {
        DomainTransactionTypeEnum typeEnum = DomainTransactionTypeEnum.valueOf(type);
        
        return new DomainEntityTransaction(
                transactionId,
                financialAccountId,
                amount,
                currency,
                typeEnum,
                description,
                reference,
                timestamp
        );
    }

    public static InfrastructureTransactionJpaEntity fromDomainEntity(DomainEntityTransaction domain) {
        InfrastructureTransactionJpaEntity entity = new InfrastructureTransactionJpaEntity();
        entity.setTransactionId(domain.getTransactionId());
        entity.setFinancialAccountId(domain.getFinancialAccountId());
        entity.setAmount(domain.getAmount());
        entity.setCurrency(domain.getCurrency());
        entity.setType(domain.getType().name());
        entity.setDescription(domain.getDescription());
        entity.setReference(domain.getReference());
        entity.setTimestamp(domain.getTimestamp());
        return entity;
    }
}