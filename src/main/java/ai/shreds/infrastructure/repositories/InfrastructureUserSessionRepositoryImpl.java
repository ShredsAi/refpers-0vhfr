package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityUserSession;
import ai.shreds.domain.ports.DomainOutputPortUserSessionRepository;
import ai.shreds.infrastructure.external_services.InfrastructureRedisClient;
import ai.shreds.shared.utils.SharedUtilJsonSerializer;
import ai.shreds.shared.value_objects.SharedValueSessionId;

import org.springframework.stereotype.Repository;

@Repository
public class InfrastructureUserSessionRepositoryImpl implements DomainOutputPortUserSessionRepository {

    private final InfrastructureRedisClient redisClient;
    private final SharedUtilJsonSerializer jsonSerializer;
    private static final String SESSION_KEY_PREFIX = "USER_SESSION:";
    private static final long SESSION_TTL = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    public InfrastructureUserSessionRepositoryImpl(InfrastructureRedisClient redisClient,
                                                  SharedUtilJsonSerializer jsonSerializer) {
        this.redisClient = redisClient;
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public void save(DomainEntityUserSession session) {
        String key = getSessionKey(session.getSessionId());
        String sessionJson = jsonSerializer.serialize(session);
        long ttl = session.getExpiresAt().toEpochMilli() - System.currentTimeMillis();
        ttl = Math.max(ttl, 0); // Ensure non-negative TTL
        
        redisClient.set(key, sessionJson, ttl);
    }

    @Override
    public DomainEntityUserSession findById(SharedValueSessionId sessionId) {
        String key = getSessionKey(sessionId);
        String sessionJson = redisClient.get(key);
        
        if (sessionJson == null) {
            return null;
        }
        
        DomainEntityUserSession session = jsonSerializer.deserialize(sessionJson, DomainEntityUserSession.class);
        
        if (session.isExpired()) {
            redisClient.delete(key);
            return null;
        }
        
        return session;
    }

    private String getSessionKey(SharedValueSessionId sessionId) {
        return SESSION_KEY_PREFIX + sessionId.getValue();
    }
}
