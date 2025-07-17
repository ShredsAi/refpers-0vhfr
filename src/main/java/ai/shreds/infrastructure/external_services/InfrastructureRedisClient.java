package ai.shreds.infrastructure.external_services;

import ai.shreds.infrastructure.exceptions.InfrastructureRedisException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class InfrastructureRedisClient {

    private final StringRedisTemplate redisTemplate;

    public InfrastructureRedisClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new InfrastructureRedisException(
                    "Failed to get value from Redis",
                    "GET",
                    key
            );
        }
    }

    public void set(String key, String value, long ttlMillis) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new InfrastructureRedisException(
                    "Failed to set value in Redis",
                    "SET",
                    key
            );
        }
    }

    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            throw new InfrastructureRedisException(
                    "Failed to delete key from Redis",
                    "DELETE",
                    key
            );
        }
    }

    public Boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            throw new InfrastructureRedisException(
                    "Failed to check key existence in Redis",
                    "EXISTS",
                    key
            );
        }
    }
}