package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainMfaChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InfrastructureJpaMfaChallengeRepository extends JpaRepository<DomainMfaChallengeEntity, UUID> {

    Optional<DomainMfaChallengeEntity> findByChallengeId(UUID challengeId);

    @Modifying
    @Transactional
    @Query("UPDATE DomainMfaChallengeEntity m SET m.isUsed = :isUsed WHERE m.challengeId = :challengeId")
    void updateIsUsedByChallengeId(@Param("challengeId") UUID challengeId, @Param("isUsed") boolean isUsed);
}