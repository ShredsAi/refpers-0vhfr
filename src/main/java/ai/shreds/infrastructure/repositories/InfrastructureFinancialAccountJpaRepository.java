package ai.shreds.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InfrastructureFinancialAccountJpaRepository extends JpaRepository<InfrastructureFinancialAccountJpaEntity, UUID> {
    
    Optional<InfrastructureFinancialAccountJpaEntity> findByAccountId(UUID accountId);
    
    Boolean existsByAccountId(UUID accountId);
}