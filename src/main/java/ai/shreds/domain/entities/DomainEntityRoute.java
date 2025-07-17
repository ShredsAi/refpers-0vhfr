package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedRouteDTO;
import ai.shreds.shared.enums.SharedEnumHttpMethod;
import ai.shreds.shared.value_objects.SharedValueRouteId;
import ai.shreds.shared.value_objects.SharedValuePathPattern;
import ai.shreds.shared.value_objects.SharedValueServiceName;
import ai.shreds.domain.value_objects.DomainValueRateLimitConfig;

/**
 * Entity representing a route configuration in the gateway.
 */
public class DomainEntityRoute {
    private final SharedValueRouteId routeId;
    private final SharedValuePathPattern pathPattern;
    private final SharedEnumHttpMethod httpMethod;
    private final SharedValueServiceName targetService;
    private final boolean authenticationRequired;
    private final DomainValueRateLimitConfig rateLimitConfig;
    private final boolean isActive;
    
    public DomainEntityRoute(
            SharedValueRouteId routeId,
            SharedValuePathPattern pathPattern,
            SharedEnumHttpMethod httpMethod,
            SharedValueServiceName targetService,
            boolean authenticationRequired,
            DomainValueRateLimitConfig rateLimitConfig,
            boolean isActive) {
        this.routeId = routeId;
        this.pathPattern = pathPattern;
        this.httpMethod = httpMethod;
        this.targetService = targetService;
        this.authenticationRequired = authenticationRequired;
        this.rateLimitConfig = rateLimitConfig;
        this.isActive = isActive;
        validate();
    }
    
    private void validate() {
        if (routeId == null) {
            throw new IllegalArgumentException("Route ID cannot be null");
        }
        if (pathPattern == null) {
            throw new IllegalArgumentException("Path pattern cannot be null");
        }
        if (httpMethod == null) {
            throw new IllegalArgumentException("HTTP method cannot be null");
        }
        if (targetService == null) {
            throw new IllegalArgumentException("Target service cannot be null");
        }
        // rateLimitConfig can be null for routes without rate limiting
    }
    
    public SharedValueRouteId getRouteId() {
        return routeId;
    }
    
    public SharedValuePathPattern getPathPattern() {
        return pathPattern;
    }
    
    public SharedEnumHttpMethod getHttpMethod() {
        return httpMethod;
    }
    
    public SharedValueServiceName getTargetService() {
        return targetService;
    }
    
    public boolean isAuthenticationRequired() {
        return authenticationRequired;
    }
    
    public DomainValueRateLimitConfig getRateLimitConfig() {
        return rateLimitConfig;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Converts this domain entity to a shared DTO.
     */
    public SharedRouteDTO toDTO() {
        SharedRouteDTO dto = new SharedRouteDTO();
        dto.setRouteId(routeId);
        dto.setPathPattern(pathPattern);
        dto.setHttpMethod(httpMethod);
        dto.setTargetService(targetService);
        dto.setAuthenticationRequired(authenticationRequired);
        dto.setActive(isActive);
        return dto;
    }
    
    /**
     * Creates a domain entity from a shared DTO.
     */
    public static DomainEntityRoute fromDTO(SharedRouteDTO dto) {
        return new DomainEntityRoute(
            dto.getRouteId(),
            dto.getPathPattern(),
            dto.getHttpMethod(),
            dto.getTargetService(),
            dto.isAuthenticationRequired(),
            null, // Rate limit config not in DTO
            dto.isActive()
        );
    }
    
    @Override
    public String toString() {
        return String.format("Route{routeId=%s, pathPattern=%s, method=%s, targetService=%s, authRequired=%s, active=%s}",
            routeId, pathPattern, httpMethod, targetService, authenticationRequired, isActive);
    }
}