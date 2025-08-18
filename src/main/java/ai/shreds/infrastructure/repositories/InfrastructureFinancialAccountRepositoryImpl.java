package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityFinancialAccount;
import ai.shreds.domain.ports.DomainOutputPortFinancialAccountRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureRepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class InfrastructureFinancialAccountRepositoryImpl implements DomainOutputPortFinancialAccountRepository {

    private final InfrastructureFinancialAccountJpaRepository jpaRepository;
    private final InfrastructureFinancialAccountMapper mapper;

    @Autowired
    public InfrastructureFinancialAccountRepositoryImpl(
            InfrastructureFinancialAccountJpaRepository jpaRepository,
            InfrastructureFinancialAccountMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DomainEntityFinancialAccount save(DomainEntityFinancialAccount account) {
        try {
            InfrastructureFinancialAccountJpaEntity jpaEntity = mapper.toJpaEntity(account);
            InfrastructureFinancialAccountJpaEntity savedEntity = jpaRepository.save(jpaEntity);
            return mapper.toDomainEntity(savedEntity);
        } catch (Exception e) {
            throw new InfrastructureRepositoryException(
                "Failed to save financial account: " + account.getFinancialAccountId(), e);
        }
    }

    @Override
    public DomainEntityFinancialAccount findByAccountId(UUID accountId) {
        try {
            Optional<InfrastructureFinancialAccountJpaEntity> jpaEntity = jpaRepository.findByAccountId(accountId);
            return jpaEntity.map(mapper::toDomainEntity)
                    .orElse(null);
        } catch (Exception e) {
            throw new InfrastructureRepositoryException(
                "Failed to find financial account by account ID: " + accountId, e);
        }
    }

    @Override
    public boolean existsByAccountId(UUID accountId) {
        try {
            Boolean result = jpaRepository.existsByAccountId(accountId);
            return result != null ? result.booleanValue() : false;
        } catch (Exception e) {
            throw new InfrastructureRepositoryException(
                "Failed to check existence of financial account by account ID: " + accountId, e);
        }
    }
}