package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationOutputPortJwksClient;
import ai.shreds.shared.dtos.SharedJwksDTO;
import ai.shreds.shared.dtos.SharedPublicKeyDTO;
import ai.shreds.infrastructure.config.InfrastructureSecurityConfig;
import ai.shreds.shared.utils.SharedUtilJsonSerializer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Component
public class InfrastructureJwksClientImpl implements ApplicationOutputPortJwksClient {

    private final InfrastructureHttpClient httpClient;
    private final InfrastructureRedisClient redisClient;
    private final InfrastructureSecurityConfig securityConfig;
    private final SharedUtilJsonSerializer jsonSerializer;
    private static final String JWKS_CACHE_KEY = "JWKS_CACHE_KEY";

    public InfrastructureJwksClientImpl(InfrastructureHttpClient httpClient,
                                        InfrastructureRedisClient redisClient,
                                        InfrastructureSecurityConfig securityConfig,
                                        SharedUtilJsonSerializer jsonSerializer) {
        this.httpClient = httpClient;
        this.redisClient = redisClient;
        this.securityConfig = securityConfig;
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public SharedJwksDTO fetchPublicKeys() {
        String cacheKey = getJwksCacheKey();
        String cachedJwksJson = redisClient.get(cacheKey);
        if (cachedJwksJson != null) {
            SharedJwksDTO cachedJwks = jsonSerializer.deserialize(cachedJwksJson, SharedJwksDTO.class);
            return cachedJwks;
        }
        
        // If no cached value, fetch from account service
        return fetchFromAccountService();
    }

    @Override
    public List<SharedPublicKeyDTO> getCachedPublicKeys() {
        SharedJwksDTO jwks = fetchPublicKeys();
        return jwks.getKeys();
    }
    
    private SharedJwksDTO fetchFromAccountService() {
        String url = securityConfig.getJwksEndpoint();
        try {
            // Use reactive approach with subscribeOn to avoid blocking in event loop
            String jwksJson = Mono.fromCallable(() -> {
                // This will run on a different thread pool
                return httpClient.get(url, String.class).block();
            })
            .subscribeOn(Schedulers.boundedElastic())
            .block();
            
            SharedJwksDTO jwksDTO = jsonSerializer.deserialize(jwksJson, SharedJwksDTO.class);
            cacheJwks(jwksDTO, jwksJson);
            return jwksDTO;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch JWKS from " + url + ": " + e.getMessage(), e);
        }
    }
    
    private void cacheJwks(SharedJwksDTO jwks, String jwksJson) {
        String cacheKey = getJwksCacheKey();
        redisClient.set(cacheKey, jwksJson, securityConfig.getJwksCacheTtl().toMillis());
    }

    private String getJwksCacheKey() {
        return JWKS_CACHE_KEY;
    }
}