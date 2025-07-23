package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityTransaction;
import ai.shreds.domain.enums.DomainTransactionTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class InfrastructureTransactionMapper {

    public DomainEntityTransaction toDomainEntity(InfrastructureTransactionJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        DomainTransactionTypeEnum typeEnum = toDomainTransactionType(jpa.getType());
        
        return new DomainEntityTransaction(
                jpa.getTransactionId(),
                jpa.getFinancialAccountId(),
                jpa.getAmount(),
                jpa.getCurrency(),
                typeEnum,
                jpa.getDescription(),
                jpa.getReference(),
                jpa.getTimestamp()
        );
    }

    public InfrastructureTransactionJpaEntity toJpaEntity(DomainEntityTransaction domain) {
        if (domain == null) {
            return null;
        }

        InfrastructureTransactionJpaEntity jpa = new InfrastructureTransactionJpaEntity();
        jpa.setTransactionId(domain.getTransactionId());
        jpa.setFinancialAccountId(domain.getFinancialAccountId());
        jpa.setAmount(domain.getAmount());
        jpa.setCurrency(domain.getCurrency());
        jpa.setType(domain.getType().name());
        jpa.setDescription(domain.getDescription());
        jpa.setReference(domain.getReference());
        jpa.setTimestamp(domain.getTimestamp());

        return jpa;
    }

    public DomainTransactionTypeEnum toDomainTransactionType(String type) {
        if (type == null) {
            return null;
        }
        
        try {
            return DomainTransactionTypeEnum.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type: " + type, e);
        }
    }
}