package ai.shreds.infrastructure.repositories;

import ai.shreds.application.ports.ApplicationOutputPortTokenCache;
import ai.shreds.shared.dtos.SharedAuthenticationContextDTO;
import ai.shreds.shared.utils.SharedUtilJsonSerializer;
import ai.shreds.infrastructure.external_services.InfrastructureRedisClient;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Repository
public class InfrastructureTokenCacheImpl implements ApplicationOutputPortTokenCache {

    private final InfrastructureRedisClient redisClient;
    private final SharedUtilJsonSerializer jsonSerializer;

    public InfrastructureTokenCacheImpl(InfrastructureRedisClient redisClient, SharedUtilJsonSerializer jsonSerializer) {
        this.redisClient = redisClient;
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public SharedAuthenticationContextDTO getCachedAuthContext(String jwtHash) {
        String cacheKey = getCacheKey(jwtHash);
        String json = redisClient.get(cacheKey);
        if (json != null) {
            return jsonSerializer.deserialize(json, SharedAuthenticationContextDTO.class);
        }
        return null;
    }

    @Override
    public void cacheAuthContext(String jwtHash, SharedAuthenticationContextDTO context, long ttl) {
        String cacheKey = getCacheKey(jwtHash);
        String json = jsonSerializer.serialize(context);
        redisClient.set(cacheKey, json, ttl);
    }

    @Override
    public String generateTokenHash(String jwt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(jwt.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String getCacheKey(String jwtHash) {
        return "TOKEN_CACHE:" + jwtHash;
    }
}