package ai.shreds.shared.dtos;

import ai.shreds.shared.value_objects.SharedValueRouteId;
import ai.shreds.shared.value_objects.SharedValuePathPattern;
import ai.shreds.shared.value_objects.SharedValueServiceName;
import ai.shreds.shared.enums.SharedEnumHttpMethod;
import ai.shreds.domain.entities.DomainEntityRoute;

public class SharedRouteDTO {
    private SharedValueRouteId routeId;
    private SharedValuePathPattern pathPattern;
    private SharedEnumHttpMethod httpMethod;
    private SharedValueServiceName targetService;
    private boolean authenticationRequired;
    private boolean isActive;

    public SharedRouteDTO() {
    }
    
    public SharedRouteDTO(SharedValueRouteId routeId, SharedValuePathPattern pathPattern, 
                         SharedEnumHttpMethod httpMethod, SharedValueServiceName targetService, 
                         boolean authenticationRequired, boolean isActive) {
        this.routeId = routeId;
        this.pathPattern = pathPattern;
        this.httpMethod = httpMethod;
        this.targetService = targetService;
        this.authenticationRequired = authenticationRequired;
        this.isActive = isActive;
    }

    /**
     * Converts this DTO to a domain entity.
     * 
     * @return DomainEntityRoute
     */
    public DomainEntityRoute toEntity() {
        if (routeId == null) {
            throw new IllegalStateException("RouteId cannot be null when converting to entity");
        }
        if (pathPattern == null) {
            throw new IllegalStateException("PathPattern cannot be null when converting to entity");
        }
        if (httpMethod == null) {
            throw new IllegalStateException("HttpMethod cannot be null when converting to entity");
        }
        if (targetService == null) {
            throw new IllegalStateException("TargetService cannot be null when converting to entity");
        }
        
        return DomainEntityRoute.fromDTO(this);
    }

    /**
     * Creates a DTO from a domain entity.
     * 
     * @param entity the domain entity
     * @return SharedRouteDTO
     */
    public static SharedRouteDTO fromEntity(DomainEntityRoute entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        
        return entity.toDTO();
    }

    public SharedValueRouteId getRouteId() {
        return routeId;
    }

    public void setRouteId(SharedValueRouteId routeId) {
        this.routeId = routeId;
    }

    public SharedValuePathPattern getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(SharedValuePathPattern pathPattern) {
        this.pathPattern = pathPattern;
    }

    public SharedEnumHttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(SharedEnumHttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public SharedValueServiceName getTargetService() {
        return targetService;
    }

    public void setTargetService(SharedValueServiceName targetService) {
        this.targetService = targetService;
    }

    public boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    public void setAuthenticationRequired(boolean authenticationRequired) {
        this.authenticationRequired = authenticationRequired;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "SharedRouteDTO{" +
                "routeId=" + routeId +
                ", pathPattern=" + pathPattern +
                ", httpMethod=" + httpMethod +
                ", targetService=" + targetService +
                ", authenticationRequired=" + authenticationRequired +
                ", isActive=" + isActive +
                '}';
    }
}