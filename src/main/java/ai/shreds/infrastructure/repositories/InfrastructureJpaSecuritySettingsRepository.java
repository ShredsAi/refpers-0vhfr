package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainSecuritySettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InfrastructureJpaSecuritySettingsRepository extends JpaRepository<DomainSecuritySettingsEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DomainSecuritySettingsEntity s WHERE s.accountId = :accountId")
    Optional<DomainSecuritySettingsEntity> findByAccountIdWithLock(@Param("accountId") UUID accountId);

    Optional<DomainSecuritySettingsEntity> findByAccountId(UUID accountId);

    @Modifying
    @Transactional
    @Query("UPDATE DomainSecuritySettingsEntity s SET s.loginAttempts = :attempts, s.lockedUntil = :lockedUntil WHERE s.accountId = :accountId")
    void updateLoginAttemptsAndLockStatus(@Param("accountId") UUID accountId, @Param("attempts") Integer attempts, @Param("lockedUntil") Instant lockedUntil);

    @Modifying
    @Transactional
    @Query("UPDATE DomainSecuritySettingsEntity s SET s.loginAttempts = 0, s.lockedUntil = null WHERE s.accountId = :accountId")
    void resetLoginAttempts(@Param("accountId") UUID accountId);

    boolean existsByAccountId(UUID accountId);
}