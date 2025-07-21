package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainAuthenticationSessionEntity;
import ai.shreds.domain.ports.DomainOutputPortSessionRepository;
import ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional
public class InfrastructureSessionRepositoryImpl implements DomainOutputPortSessionRepository {

    private final InfrastructureJpaSessionRepository jpaRepository;

    @Autowired
    public InfrastructureSessionRepositoryImpl(InfrastructureJpaSessionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DomainAuthenticationSessionEntity save(DomainAuthenticationSessionEntity session) {
        try {
            return jpaRepository.save(session);
        } catch (Exception e) {
            throw new InfrastructurePersistenceException(
                    "Failed to save authentication session: " + e.getMessage(),
                    DomainAuthenticationSessionEntity.class.getSimpleName(),
                    "save"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DomainAuthenticationSessionEntity findByRefreshTokenHash(String tokenHash) {
        try {
            return jpaRepository.findByRefreshTokenHash(tokenHash)
                    .orElseThrow(() -> new InfrastructurePersistenceException(
                            "Authentication session not found for refresh token hash",
                            DomainAuthenticationSessionEntity.class.getSimpleName(),
                            "findByRefreshTokenHash"
                    ));
        } catch (Exception e) {
            throw new InfrastructurePersistenceException(
                    "Failed to find session by refresh token hash: " + e.getMessage(),
                    DomainAuthenticationSessionEntity.class.getSimpleName(),
                    "findByRefreshTokenHash"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DomainAuthenticationSessionEntity findByAccessTokenHash(String tokenHash) {
        try {
            return jpaRepository.findByAccessTokenHash(tokenHash)
                    .orElseThrow(() -> new InfrastructurePersistenceException(
                            "Authentication session not found for access token hash",
                            DomainAuthenticationSessionEntity.class.getSimpleName(),
                            "findByAccessTokenHash"
                    ));
        } catch (Exception e) {
            throw new InfrastructurePersistenceException(
                    "Failed to find session by access token hash: " + e.getMessage(),
                    DomainAuthenticationSessionEntity.class.getSimpleName(),
                    "findByAccessTokenHash"
            );
        }
    }

    @Override
    public void revokeSession(String sessionId) {
        try {
            UUID sessionUuid = UUID.fromString(sessionId);
            if (!jpaRepository.existsBySessionId(sessionUuid)) {
                throw new InfrastructurePersistenceException(
                        "Session not found for sessionId: " + sessionId,
                        DomainAuthenticationSessionEntity.class.getSimpleName(),
                        "revokeSession"
                );
            }
            jpaRepository.updateIsRevokedBySessionId(sessionUuid, true);
        } catch (IllegalArgumentException e) {
            throw new InfrastructurePersistenceException(
                    "Invalid session ID format: " + sessionId,
                    DomainAuthenticationSessionEntity.class.getSimpleName(),
                    "revokeSession"
            );
        } catch (Exception e) {
            throw new InfrastructurePersistenceException(
                    "Failed to revoke session: " + e.getMessage(),
                    DomainAuthenticationSessionEntity.class.getSimpleName(),
                    "revokeSession"
            );
        }
    }

    @Override
    public void revokeAllSessionsForAccount(String accountId) {
        try {
            UUID accountUuid = UUID.fromString(accountId);
            jpaRepository.updateIsRevokedByAccountId(accountUuid, true);
        } catch (IllegalArgumentException e) {
            throw new InfrastructurePersistenceException(
                    "Invalid account ID format: " + accountId,
                    DomainAuthenticationSessionEntity.class.getSimpleName(),
                    "revokeAllSessionsForAccount"
            );
        } catch (Exception e) {
            throw new InfrastructurePersistenceException(
                    "Failed to revoke all sessions for account: " + e.getMessage(),
                    DomainAuthenticationSessionEntity.class.getSimpleName(),
                    "revokeAllSessionsForAccount"
            );
        }
    }
}