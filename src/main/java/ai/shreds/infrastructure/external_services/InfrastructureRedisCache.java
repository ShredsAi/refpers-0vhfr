package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationOutputPortCache;
import ai.shreds.application.dtos.ApplicationMoneyValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class InfrastructureRedisCache implements ApplicationOutputPortCache {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Long ttlSeconds;
    
    private static final String BALANCE_KEY_PREFIX = "financial_account:balance:";

    public InfrastructureRedisCache(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            @Value("${financial.cache.ttl-seconds}") Long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public ApplicationMoneyValue getBalance(String accountId) {
        try {
            String cacheKey = generateCacheKey(accountId);
            String cachedValue = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedValue != null) {
                return objectMapper.readValue(cachedValue, ApplicationMoneyValue.class);
            }
            
            return null;
        } catch (JsonProcessingException e) {
            // Log error and return null to fall back to database
            return null;
        }
    }

    @Override
    public void putBalance(String accountId, ApplicationMoneyValue balance) {
        try {
            String cacheKey = generateCacheKey(accountId);
            String balanceJson = objectMapper.writeValueAsString(balance);
            
            redisTemplate.opsForValue().set(cacheKey, balanceJson, ttlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            // Log error but don't throw - caching failure shouldn't break the application
        }
    }

    @Override
    public void invalidateBalance(String accountId) {
        String cacheKey = generateCacheKey(accountId);
        redisTemplate.delete(cacheKey);
    }

    private String generateCacheKey(String accountId) {
        return BALANCE_KEY_PREFIX + accountId;
    }
}