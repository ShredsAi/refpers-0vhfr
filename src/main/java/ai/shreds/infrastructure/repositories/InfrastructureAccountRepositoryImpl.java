package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainAccountEntity;
import ai.shreds.domain.ports.DomainOutputPortAccountRepository;
import ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class InfrastructureAccountRepositoryImpl implements DomainOutputPortAccountRepository {

    private final InfrastructureJpaAccountRepository jpaRepository;

    @Autowired
    public InfrastructureAccountRepositoryImpl(InfrastructureJpaAccountRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DomainAccountEntity findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .orElseThrow(() -> new InfrastructurePersistenceException(
                        "Failed to find account by username: " + username,
                        DomainAccountEntity.class.getSimpleName(),
                        "findByUsername"));
    }

    @Override
    public DomainAccountEntity findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .orElseThrow(() -> new InfrastructurePersistenceException(
                        "Failed to find account by email: " + email,
                        DomainAccountEntity.class.getSimpleName(),
                        "findByEmail"));
    }

    @Override
    public DomainAccountEntity findById(String accountId) {
        try {
            UUID id = UUID.fromString(accountId);
            return jpaRepository.findByAccountId(id)
                    .orElseThrow(() -> new InfrastructurePersistenceException(
                            "Failed to find account by id: " + accountId,
                            DomainAccountEntity.class.getSimpleName(),
                            "findById"));
        } catch (IllegalArgumentException e) {
            throw new InfrastructurePersistenceException(
                    "Invalid UUID for account id: " + accountId,
                    DomainAccountEntity.class.getSimpleName(),
                    "findById");
        }
    }
}
