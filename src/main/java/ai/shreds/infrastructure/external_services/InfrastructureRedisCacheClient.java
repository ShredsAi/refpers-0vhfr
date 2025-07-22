package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationCacheOutputPort;
import ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class InfrastructureRedisCacheClient implements ApplicationCacheOutputPort {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public InfrastructureRedisCacheClient(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(String key, Object value, long ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofMillis(ttl));
        } catch (Exception e) {
            throw new InfrastructureExternalServiceException(
                "Failed to store value in cache: " + e.getMessage(),
                "Redis",
                0
            );
        }
    }

    @Override
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new InfrastructureExternalServiceException(
                "Failed to retrieve value from cache: " + e.getMessage(),
                "Redis",
                0
            );
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new InfrastructureExternalServiceException(
                "Failed to delete key from cache: " + e.getMessage(),
                "Redis",
                0
            );
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            throw new InfrastructureExternalServiceException(
                "Failed to check key existence in cache: " + e.getMessage(),
                "Redis",
                0
            );
        }
    }
}