package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainAuthenticationSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InfrastructureJpaSessionRepository extends JpaRepository<DomainAuthenticationSessionEntity, UUID> {

    Optional<DomainAuthenticationSessionEntity> findByRefreshTokenHash(String tokenHash);

    Optional<DomainAuthenticationSessionEntity> findByAccessTokenHash(String tokenHash);

    @Modifying
    @Transactional
    @Query("UPDATE DomainAuthenticationSessionEntity s SET s.isRevoked = :isRevoked WHERE s.sessionId = :sessionId")
    void updateIsRevokedBySessionId(@Param("sessionId") UUID sessionId, @Param("isRevoked") Boolean isRevoked);

    @Modifying
    @Transactional
    @Query("UPDATE DomainAuthenticationSessionEntity s SET s.isRevoked = :isRevoked WHERE s.accountId = :accountId")
    void updateIsRevokedByAccountId(@Param("accountId") UUID accountId, @Param("isRevoked") Boolean isRevoked);

    @Query("SELECT s FROM DomainAuthenticationSessionEntity s WHERE s.accountId = :accountId AND s.isRevoked = false")
    java.util.List<DomainAuthenticationSessionEntity> findActiveSessionsByAccountId(@Param("accountId") UUID accountId);

    boolean existsBySessionId(UUID sessionId);
}