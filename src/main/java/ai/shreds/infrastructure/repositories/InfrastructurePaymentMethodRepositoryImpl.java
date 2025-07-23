package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityPaymentMethod;
import ai.shreds.domain.ports.DomainOutputPortPaymentMethodRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureRepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class InfrastructurePaymentMethodRepositoryImpl implements DomainOutputPortPaymentMethodRepository {

    private final InfrastructurePaymentMethodJpaRepository jpaRepository;
    private final InfrastructurePaymentMethodMapper mapper;

    @Autowired
    public InfrastructurePaymentMethodRepositoryImpl(
            InfrastructurePaymentMethodJpaRepository jpaRepository,
            InfrastructurePaymentMethodMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DomainEntityPaymentMethod save(DomainEntityPaymentMethod paymentMethod) {
        try {
            InfrastructurePaymentMethodJpaEntity jpaEntity = mapper.toJpaEntity(paymentMethod);
            InfrastructurePaymentMethodJpaEntity savedEntity = jpaRepository.save(jpaEntity);
            return mapper.toDomainEntity(savedEntity);
        } catch (Exception e) {
            throw new InfrastructureRepositoryException(
                "Failed to save payment method: " + paymentMethod.getPaymentMethodId(), e);
        }
    }

    @Override
    public DomainEntityPaymentMethod findById(UUID paymentMethodId) {
        try {
            Optional<InfrastructurePaymentMethodJpaEntity> jpaEntity = jpaRepository.findById(paymentMethodId);
            return jpaEntity.map(mapper::toDomainEntity)
                    .orElse(null);
        } catch (Exception e) {
            throw new InfrastructureRepositoryException(
                "Failed to find payment method by ID: " + paymentMethodId, e);
        }
    }

    @Override
    public List<DomainEntityPaymentMethod> findByAccountId(UUID accountId) {
        try {
            List<InfrastructurePaymentMethodJpaEntity> jpaEntities = jpaRepository.findByAccountId(accountId);
            return jpaEntities.stream()
                    .map(mapper::toDomainEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new InfrastructureRepositoryException(
                "Failed to find payment methods by account ID: " + accountId, e);
        }
    }

    @Override
    public DomainEntityPaymentMethod findDefaultByAccountId(UUID accountId) {
        try {
            Optional<InfrastructurePaymentMethodJpaEntity> jpaEntity = jpaRepository.findByAccountIdAndIsDefaultTrue(accountId);
            return jpaEntity.map(mapper::toDomainEntity)
                    .orElse(null);
        } catch (Exception e) {
            throw new InfrastructureRepositoryException(
                "Failed to find default payment method by account ID: " + accountId, e);
        }
    }
}