package ai.shreds.domain.services;

import java.time.Instant;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ai.shreds.domain.ports.DomainInputPortRequestContext;
import ai.shreds.domain.entities.DomainEntityAuthenticationContext;
import ai.shreds.domain.entities.DomainEntityRequestContext;
import ai.shreds.domain.value_objects.DomainValueRequestMetadata;
import ai.shreds.domain.value_objects.DomainValueSecurityHeaders;
import ai.shreds.domain.value_objects.DomainValueUserAgent;
import ai.shreds.domain.value_objects.DomainValueRequestPath;
import ai.shreds.domain.value_objects.DomainValueRequestHeaders;
import ai.shreds.shared.value_objects.SharedValueRequestId;
import ai.shreds.shared.value_objects.SharedValueIpAddress;
import ai.shreds.shared.enums.SharedEnumUserRole;

/**
 * Service implementing request context creation logic.
 */
@Service
public class DomainServiceRequestContext implements DomainInputPortRequestContext {
    
    @Override
    public DomainEntityRequestContext createRequestContext(
            DomainValueRequestMetadata request,
            DomainEntityAuthenticationContext authContext) {
        
        if (request == null) {
            throw new IllegalArgumentException("Request metadata cannot be null");
        }
        
        if (authContext == null) {
            throw new IllegalArgumentException("Authentication context cannot be null");
        }
        
        // Generate request ID
        SharedValueRequestId requestId = generateRequestId();
        
        // Extract IP address
        SharedValueIpAddress clientIp = new SharedValueIpAddress(request.getRemoteAddress());
        
        // Extract User-Agent header, fallback to Unknown if not present
        List<String> userAgentHeaders = request.getHeaders().get("User-Agent");
        String userAgentValue = (userAgentHeaders != null && !userAgentHeaders.isEmpty())
                ? userAgentHeaders.get(0)
                : "Unknown";
        DomainValueUserAgent userAgent = new DomainValueUserAgent(userAgentValue);

        DomainValueRequestPath requestPath = new DomainValueRequestPath(request.getPath());
        
        DomainValueRequestHeaders headers = new DomainValueRequestHeaders(
            request.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey(),
                    entry -> String.join(",", entry.getValue())
                ))
        );
        
        return new DomainEntityRequestContext(
            requestId,
            clientIp,
            userAgent,
            requestPath,
            request.getMethod(),
            headers,
            Instant.now(),
            authContext
        );
    }
    
    @Override
    public DomainValueSecurityHeaders generateSecurityHeaders(DomainEntityAuthenticationContext authContext) {
        if (authContext == null) {
            throw new IllegalArgumentException("Authentication context cannot be null");
        }

        String userId = authContext.isAuthenticated() && authContext.getUserId() != null
            ? authContext.getUserId().getValue()
            : "anonymous";

        String userRoles = buildSecurityHeaderString(authContext.getUserRoles());

        return new DomainValueSecurityHeaders(userId, userRoles);
    }
    
    private String buildSecurityHeaderString(Set<SharedEnumUserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return "GUEST";
        }

        return roles.stream()
            .map(Enum::name)
            .collect(Collectors.joining(","));
    }
    
    private SharedValueRequestId generateRequestId() {
        return SharedValueRequestId.generate();
    }
}