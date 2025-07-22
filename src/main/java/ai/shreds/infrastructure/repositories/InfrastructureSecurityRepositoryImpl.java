package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainSecuritySettingsEntity;
import ai.shreds.domain.ports.DomainOutputPortSecurityRepository;
import ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional
public class InfrastructureSecurityRepositoryImpl implements DomainOutputPortSecurityRepository {

    private final InfrastructureJpaSecuritySettingsRepository jpaRepository;

    @Autowired
    public InfrastructureSecurityRepositoryImpl(InfrastructureJpaSecuritySettingsRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DomainSecuritySettingsEntity findByAccountId(String accountId) {
        try {
            UUID accountUuid = UUID.fromString(accountId);
            return jpaRepository.findByAccountId(accountUuid).orElse(null);
        } catch (IllegalArgumentException e) {
            throw new InfrastructurePersistenceException(
                    "Invalid account ID format: " + accountId,
                    DomainSecuritySettingsEntity.class.getSimpleName(),
                    "findByAccountId"
            );
        } catch (Exception e) {
            throw new InfrastructurePersistenceException(
                    "Failed to find security settings for account: " + accountId,
                    DomainSecuritySettingsEntity.class.getSimpleName(),
                    "findByAccountId"
            );
        }
    }

    @Override
    public DomainSecuritySettingsEntity save(DomainSecuritySettingsEntity settings) {
        try {
            return jpaRepository.save(settings);
        } catch (Exception e) {
            throw new InfrastructurePersistenceException(
                    "Failed to save security settings: " + e.getMessage(),
                    DomainSecuritySettingsEntity.class.getSimpleName(),
                    "save"
            );
        }
    }

    @Override
    public void update(DomainSecuritySettingsEntity settings) {
        try {
            if (!jpaRepository.existsByAccountId(settings.getAccountId())) {
                throw new InfrastructurePersistenceException(
                        "Security settings not found for account: " + settings.getAccountId(),
                        DomainSecuritySettingsEntity.class.getSimpleName(),
                        "update"
                );
            }
            jpaRepository.save(settings);
        } catch (InfrastructurePersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new InfrastructurePersistenceException(
                    "Failed to update security settings: " + e.getMessage(),
                    DomainSecuritySettingsEntity.class.getSimpleName(),
                    "update"
            );
        }
    }

    @Transactional(readOnly = true)
    public DomainSecuritySettingsEntity findByAccountIdWithPessimisticLock(String accountId) {
        try {
            UUID accountUuid = UUID.fromString(accountId);
            return jpaRepository.findByAccountIdWithLock(accountUuid)
                    .orElseThrow(() -> new InfrastructurePersistenceException(
                            "Security settings not found for account (with lock): " + accountId,
                            DomainSecuritySettingsEntity.class.getSimpleName(),
                            "findByAccountIdWithLock"
                    ));
        } catch (IllegalArgumentException e) {
            throw new InfrastructurePersistenceException(
                    "Invalid account ID format: " + accountId,
                    DomainSecuritySettingsEntity.class.getSimpleName(),
                    "findByAccountIdWithLock"
            );
        } catch (Exception e) {
            throw new InfrastructurePersistenceException(
                    "Failed to find security settings with lock for account: " + accountId,
                    DomainSecuritySettingsEntity.class.getSimpleName(),
                    "findByAccountIdWithLock"
            );
        }
    }

    public void resetLoginAttempts(String accountId) {
        try {
            UUID accountUuid = UUID.fromString(accountId);
            jpaRepository.resetLoginAttempts(accountUuid);
        } catch (IllegalArgumentException e) {
            throw new InfrastructurePersistenceException(
                    "Invalid account ID format: " + accountId,
                    DomainSecuritySettingsEntity.class.getSimpleName(),
                    "resetLoginAttempts"
            );
        } catch (Exception e) {
            throw new InfrastructurePersistenceException(
                    "Failed to reset login attempts for account: " + accountId,
                    DomainSecuritySettingsEntity.class.getSimpleName(),
                    "resetLoginAttempts"
            );
        }
    }
}