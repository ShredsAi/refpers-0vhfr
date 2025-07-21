package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InfrastructureJpaAccountRepository extends JpaRepository<DomainAccountEntity, UUID> {

    Optional<DomainAccountEntity> findByUsername(String username);

    Optional<DomainAccountEntity> findByEmail(String email);

    Optional<DomainAccountEntity> findByAccountId(UUID accountId);
}