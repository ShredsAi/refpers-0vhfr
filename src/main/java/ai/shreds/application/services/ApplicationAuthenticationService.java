package ai.shreds.application.services;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.shreds.application.ports.ApplicationAuthenticationPort;
import ai.shreds.application.ports.ApplicationOutputPortTokenCache;
import ai.shreds.application.ports.ApplicationOutputPortJwksClient;
import ai.shreds.application.exceptions.ApplicationAuthenticationException;
import ai.shreds.domain.ports.DomainInputPortAuthentication;
import ai.shreds.domain.value_objects.DomainValueTokenClaims;
import ai.shreds.domain.entities.DomainEntityAuthenticationContext;
import ai.shreds.domain.exceptions.DomainExceptionInvalidToken;
import ai.shreds.domain.exceptions.DomainExceptionTokenExpired;
import ai.shreds.domain.exceptions.DomainExceptionInvalidIssuer;
import ai.shreds.domain.exceptions.DomainExceptionInvalidAudience;
import ai.shreds.shared.dtos.SharedAuthenticationContextDTO;
import ai.shreds.shared.dtos.SharedRouteDTO;
import ai.shreds.shared.dtos.SharedTokenClaimsDTO;
import ai.shreds.shared.dtos.SharedPublicKeyDTO;
import ai.shreds.shared.value_objects.SharedPublicKey;
import ai.shreds.shared.value_objects.SharedValueSessionId;

@Service
public class ApplicationAuthenticationService implements ApplicationAuthenticationPort {

    private final DomainInputPortAuthentication domainAuthenticationUseCase;
    private final ApplicationOutputPortTokenCache tokenCachePort;
    private final ApplicationOutputPortJwksClient jwksClientPort;

    @Autowired
    public ApplicationAuthenticationService(DomainInputPortAuthentication domainAuthenticationUseCase,
                                            ApplicationOutputPortTokenCache tokenCachePort,
                                            ApplicationOutputPortJwksClient jwksClientPort) {
        this.domainAuthenticationUseCase = domainAuthenticationUseCase;
        this.tokenCachePort = tokenCachePort;
        this.jwksClientPort = jwksClientPort;
    }

    @Override
    public SharedAuthenticationContextDTO authenticateRequest(String jwt, SharedRouteDTO route) {
        try {
            String jwtHash = tokenCachePort.generateTokenHash(jwt);
            SharedAuthenticationContextDTO cachedContext = tokenCachePort.getCachedAuthContext(jwtHash);
            if (cachedContext != null) {
                return cachedContext;
            }

            // Retrieve and convert public keys
            List<SharedPublicKey> publicKeys = jwksClientPort.getCachedPublicKeys().stream()
                .map(dto -> new SharedPublicKey(dto.getKid(), dto.getAlg(), dto.toPublicKey()))
                .collect(Collectors.toList());

            if (publicKeys.isEmpty()) {
                // Fallback to fetch fresh keys if cache is empty
                publicKeys = jwksClientPort.fetchPublicKeys().getKeys().stream()
                    .map(dto -> new SharedPublicKey(dto.getKid(), dto.getAlg(), dto.toPublicKey()))
                    .collect(Collectors.toList());
            }

            // Validate JWT and extract claims
            DomainValueTokenClaims domainClaims = domainAuthenticationUseCase.validateToken(jwt, publicKeys);
            SharedTokenClaimsDTO claimsDTO = domainClaims.toDTO();

            // Build AuthenticationContext DTO
            SharedAuthenticationContextDTO authContextDTO = buildAuthenticationContext(claimsDTO);

            // Cache the authentication context with TTL based on token expiration
            long ttl = Duration.between(Instant.now(), claimsDTO.getExpiresAt()).toMillis();
            if (ttl > 0) {
                tokenCachePort.cacheAuthContext(jwtHash, authContextDTO, ttl);
            }

            return authContextDTO;
        } catch (DomainExceptionInvalidToken e) {
            throw ApplicationAuthenticationException.invalidToken(e.getMessage());
        } catch (DomainExceptionTokenExpired e) {
            throw ApplicationAuthenticationException.tokenExpired(e.getMessage());
        } catch (DomainExceptionInvalidIssuer e) {
            throw ApplicationAuthenticationException.invalidIssuer(e.getMessage());
        } catch (DomainExceptionInvalidAudience e) {
            throw ApplicationAuthenticationException.invalidAudience(e.getMessage());
        } catch (Exception e) {
            throw ApplicationAuthenticationException.invalidToken("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public SharedAuthenticationContextDTO createAnonymousContext(String requestId) {
        try {
            SharedValueSessionId sessionId = new SharedValueSessionId(requestId);
            DomainEntityAuthenticationContext domainContext = domainAuthenticationUseCase.createAnonymousContext(sessionId);
            return domainContext.toDTO();
        } catch (Exception e) {
            throw new ApplicationAuthenticationException("Failed to create anonymous context: " + e.getMessage(), "ANONYMOUS_CONTEXT_ERROR");
        }
    }

    private SharedAuthenticationContextDTO buildAuthenticationContext(SharedTokenClaimsDTO claimsDTO) {
        try {
            // Convert DTO back to domain value and build context
            DomainValueTokenClaims domainClaims = DomainValueTokenClaims.fromDTO(claimsDTO);
            SharedValueSessionId sessionId = new SharedValueSessionId(UUID.randomUUID().toString());
            DomainEntityAuthenticationContext domainContext = domainAuthenticationUseCase.createAuthenticationContext(domainClaims, sessionId);
            return domainContext.toDTO();
        } catch (Exception e) {
            throw new ApplicationAuthenticationException("Failed to build authentication context: " + e.getMessage(), "CONTEXT_BUILD_ERROR");
        }
    }
}