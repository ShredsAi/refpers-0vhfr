package ai.shreds.infrastructure.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface InfrastructureTransactionJpaRepository extends JpaRepository<InfrastructureTransactionJpaEntity, UUID> {
    
    Page<InfrastructureTransactionJpaEntity> findByFinancialAccountIdAndTimestampBetween(
            UUID financialAccountId, 
            LocalDateTime start, 
            LocalDateTime end, 
            Pageable pageable
    );
    
    Long countByFinancialAccountIdAndTimestampBetween(
            UUID financialAccountId, 
            LocalDateTime start, 
            LocalDateTime end
    );
    
    Page<InfrastructureTransactionJpaEntity> findByFinancialAccountId(UUID financialAccountId, Pageable pageable);
    
    Long countByFinancialAccountId(UUID financialAccountId);
}