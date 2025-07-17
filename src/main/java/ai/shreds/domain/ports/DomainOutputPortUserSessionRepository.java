package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityUserSession;
import ai.shreds.shared.value_objects.SharedValueSessionId;

/**
 * Port interface for persisting and retrieving user session entities.
 */
public interface DomainOutputPortUserSessionRepository {

    /**
     * Persist a user session.
     *
     * @param session the session entity to save
     */
    void save(DomainEntityUserSession session);

    /**
     * Retrieve a user session by its identifier.
     *
     * @param sessionId the session identifier
     * @return the corresponding user session entity or null if not found
     */
    DomainEntityUserSession findById(SharedValueSessionId sessionId);
}