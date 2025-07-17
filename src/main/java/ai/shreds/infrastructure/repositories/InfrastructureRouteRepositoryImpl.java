package ai.shreds.infrastructure.repositories;

import ai.shreds.application.ports.ApplicationOutputPortRouteRepository;
import ai.shreds.infrastructure.config.InfrastructureGatewayConfig;
import ai.shreds.shared.dtos.SharedRouteDTO;
import ai.shreds.shared.enums.SharedEnumHttpMethod;
import ai.shreds.shared.value_objects.SharedValuePathPattern;
import ai.shreds.shared.value_objects.SharedValueRouteId;
import ai.shreds.shared.value_objects.SharedValueServiceName;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InfrastructureRouteRepositoryImpl implements ApplicationOutputPortRouteRepository {

    private final ConcurrentHashMap<String, SharedRouteDTO> routes;
    private final InfrastructureGatewayConfig gatewayConfig;
    
    @Value("${gateway.routes.default-private-patterns:/api/**,/admin/**}")
    private String defaultPrivatePatterns;
    
    @Value("${gateway.routes.default-public-patterns:/health,/info,/metrics}")
    private String defaultPublicPatterns;

    public InfrastructureRouteRepositoryImpl(InfrastructureGatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
        this.routes = new ConcurrentHashMap<>();
        // Remove initializeRoutes() from constructor
    }

    @PostConstruct
    private void init() {
        initializeRoutes();
    }

    @Override
    public SharedRouteDTO findRouteByPathAndMethod(String path, SharedEnumHttpMethod method) {
        return routes.values().stream()
                .filter(route -> matchRoute(path, method, route))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<SharedRouteDTO> getAllActiveRoutes() {
        return new ArrayList<>(routes.values()).stream()
                .filter(SharedRouteDTO::isActive)
                .collect(Collectors.toList());
    }

    private boolean matchRoute(String path, SharedEnumHttpMethod method, SharedRouteDTO route) {
        return route.isActive() &&
               route.getHttpMethod() == method &&
               matchPathPattern(path, route.getPathPattern());
    }

    private boolean matchPathPattern(String path, SharedValuePathPattern pattern) {
        String patternStr = pattern.getPattern();
        
        // Handle wildcard patterns
        if (patternStr.endsWith("/**")) {
            String basePattern = patternStr.substring(0, patternStr.length() - 3);
            return path.startsWith(basePattern);
        }
        
        String[] patternParts = patternStr.split("/");
        String[] pathParts = path.split("/");

        if (patternParts.length != pathParts.length) {
            return false;
        }

        for (int i = 0; i < patternParts.length; i++) {
            String patternPart = patternParts[i];
            String pathPart = pathParts[i];

            if (patternPart.equals("**")) {
                return true;
            } else if (patternPart.startsWith("{") && patternPart.endsWith("}")) {
                // Path variable - matches any value
                continue;
            } else if (!patternPart.equals(pathPart)) {
                return false;
            }
        }

        return true;
    }

    private void initializeRoutes() {
        // Initialize default private routes (require authentication)
        initializePrivateRoutes();
        
        // Initialize default public routes (no authentication required)
        initializePublicRoutes();
    }
    
    private void initializePrivateRoutes() {
        // Add null check to prevent NullPointerException
        if (defaultPrivatePatterns == null || defaultPrivatePatterns.trim().isEmpty()) {
            defaultPrivatePatterns = "/api/**,/admin/**";
        }
        
        String[] privatePatterns = defaultPrivatePatterns.split(",");
        for (String pattern : privatePatterns) {
            pattern = pattern.trim();
            for (SharedEnumHttpMethod method : SharedEnumHttpMethod.values()) {
                SharedRouteDTO route = createRoute(
                    "private-" + pattern.replace("/", "-").replace("*", "wildcard") + "-" + method.name().toLowerCase(),
                    pattern,
                    method,
                    "api-service",
                    true,
                    true
                );
                routes.put(route.getRouteId().getValue(), route);
            }
        }
    }
    
    private void initializePublicRoutes() {
        // Add null check to prevent NullPointerException
        if (defaultPublicPatterns == null || defaultPublicPatterns.trim().isEmpty()) {
            defaultPublicPatterns = "/health,/info,/metrics";
        }
        
        String[] publicPatterns = defaultPublicPatterns.split(",");
        for (String pattern : publicPatterns) {
            pattern = pattern.trim();
            SharedRouteDTO route = createRoute(
                "public-" + pattern.replace("/", "-"),
                pattern,
                SharedEnumHttpMethod.GET,
                "actuator-service",
                false,
                true
            );
            routes.put(route.getRouteId().getValue(), route);
        }
    }
    
    private SharedRouteDTO createRoute(String routeId, String pathPattern, SharedEnumHttpMethod httpMethod, 
                                      String targetService, boolean authenticationRequired, boolean isActive) {
        SharedRouteDTO route = new SharedRouteDTO();
        route.setRouteId(new SharedValueRouteId(routeId));
        route.setPathPattern(new SharedValuePathPattern(pathPattern));
        route.setHttpMethod(httpMethod);
        route.setTargetService(new SharedValueServiceName(targetService));
        route.setAuthenticationRequired(authenticationRequired);
        route.setActive(isActive);
        return route;
    }
}