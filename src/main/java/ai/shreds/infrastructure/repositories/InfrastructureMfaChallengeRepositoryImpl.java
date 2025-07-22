package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainMfaChallengeEntity;
import ai.shreds.domain.ports.DomainOutputPortMfaChallengeRepository;
import ai.shreds.infrastructure.exceptions.InfrastructurePersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class InfrastructureMfaChallengeRepositoryImpl implements DomainOutputPortMfaChallengeRepository {

    private final InfrastructureJpaMfaChallengeRepository jpaRepository;

    @Autowired
    public InfrastructureMfaChallengeRepositoryImpl(InfrastructureJpaMfaChallengeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DomainMfaChallengeEntity save(DomainMfaChallengeEntity challenge) {
        return jpaRepository.save(challenge);
    }

    @Override
    public DomainMfaChallengeEntity findById(String challengeId) {
        try {
            UUID id = UUID.fromString(challengeId);
            return jpaRepository.findByChallengeId(id)
                    .orElseThrow(() -> new InfrastructurePersistenceException(
                            "Failed to find MFA challenge by id: " + challengeId,
                            DomainMfaChallengeEntity.class.getSimpleName(),
                            "findById"));
        } catch (IllegalArgumentException e) {
            throw new InfrastructurePersistenceException(
                    "Invalid UUID for challenge id: " + challengeId,
                    DomainMfaChallengeEntity.class.getSimpleName(),
                    "findById");
        }
    }

    @Override
    public void markAsUsed(String challengeId) {
        try {
            UUID id = UUID.fromString(challengeId);
            jpaRepository.updateIsUsedByChallengeId(id, true);
        } catch (IllegalArgumentException e) {
            throw new InfrastructurePersistenceException(
                    "Invalid UUID for challenge id: " + challengeId,
                    DomainMfaChallengeEntity.class.getSimpleName(),
                    "markAsUsed");
        }
    }
}
