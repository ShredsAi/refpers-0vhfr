package ai.shreds.infrastructure.config;

import ai.shreds.shared.value_objects.SharedValueDuration;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class InfrastructureSecurityConfig {

    @Value("${security.account-service-url:http://account-service}")
    private String accountServiceUrl;

    @Value("${security.oauth2.resourceserver.jwt.jwk-set-uri:${security.account-service-url}/.well-known/jwks.json}")
    private String jwksEndpoint;

    @Value("${security.jwt.issuer:account-service}")
    private String expectedIssuer;

    @Value("${security.jwt.audience:gateway}")
    private String expectedAudience;

    @Value("${security.token-cache-ttl:3600}")
    private Long tokenCacheTtlSeconds;

    @Value("${security.jwks-cache-ttl:3600}")
    private Long jwksCacheTtlSeconds;

    public SharedValueDuration getTokenCacheTtl() {
        return new SharedValueDuration(tokenCacheTtlSeconds);
    }

    public SharedValueDuration getJwksCacheTtl() {
        return new SharedValueDuration(jwksCacheTtlSeconds);
    }
}
