package ai.shreds.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InfrastructurePaymentMethodJpaRepository extends JpaRepository<InfrastructurePaymentMethodJpaEntity, UUID> {
    
    List<InfrastructurePaymentMethodJpaEntity> findByAccountId(UUID accountId);
    
    Optional<InfrastructurePaymentMethodJpaEntity> findByAccountIdAndIsDefaultTrue(UUID accountId);
}