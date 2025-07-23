package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityTransaction;
import ai.shreds.domain.ports.DomainOutputPortTransactionRepository;
import ai.shreds.domain.value_objects.DomainPaginationParams;
import ai.shreds.infrastructure.exceptions.InfrastructureRepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class InfrastructureTransactionRepositoryImpl implements DomainOutputPortTransactionRepository {

    private final InfrastructureTransactionJpaRepository jpaRepository;
    private final InfrastructureTransactionMapper mapper;

    @Autowired
    public InfrastructureTransactionRepositoryImpl(
            InfrastructureTransactionJpaRepository jpaRepository,
            InfrastructureTransactionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DomainEntityTransaction save(DomainEntityTransaction transaction) {
        try {
            InfrastructureTransactionJpaEntity jpaEntity = mapper.toJpaEntity(transaction);
            InfrastructureTransactionJpaEntity savedEntity = jpaRepository.save(jpaEntity);
            return mapper.toDomainEntity(savedEntity);
        } catch (Exception e) {
            throw new InfrastructureRepositoryException(
                "Failed to save transaction: " + transaction.getTransactionId(), e);
        }
    }

    @Override
    public List<DomainEntityTransaction> findByFinancialAccountId(UUID financialAccountId, DomainPaginationParams params) {
        try {
            Pageable pageable = PageRequest.of(
                params.getPage(),
                params.getSize(),
                Sort.by(Sort.Direction.DESC, "timestamp")
            );

            Page<InfrastructureTransactionJpaEntity> jpaEntities;
            
            if (params.getFromDate() != null && params.getToDate() != null) {
                jpaEntities = jpaRepository.findByFinancialAccountIdAndTimestampBetween(
                    financialAccountId,
                    params.getFromDate(),
                    params.getToDate(),
                    pageable
                );
            } else {
                jpaEntities = jpaRepository.findByFinancialAccountId(financialAccountId, pageable);
            }

            return jpaEntities.getContent().stream()
                    .map(mapper::toDomainEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new InfrastructureRepositoryException(
                "Failed to find transactions by financial account ID: " + financialAccountId, e);
        }
    }

    @Override
    public Integer countByFinancialAccountId(UUID financialAccountId, DomainPaginationParams params) {
        try {
            Long count;
            
            if (params.getFromDate() != null && params.getToDate() != null) {
                count = jpaRepository.countByFinancialAccountIdAndTimestampBetween(
                    financialAccountId,
                    params.getFromDate(),
                    params.getToDate()
                );
            } else {
                count = jpaRepository.countByFinancialAccountId(financialAccountId);
            }

            return count.intValue();
        } catch (Exception e) {
            throw new InfrastructureRepositoryException(
                "Failed to count transactions by financial account ID: " + financialAccountId, e);
        }
    }
}