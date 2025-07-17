package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationAuthenticationPort;
import ai.shreds.application.ports.ApplicationRequestContextPort;
import ai.shreds.application.ports.ApplicationRouteConfigPort;
import ai.shreds.shared.dtos.SharedAuthenticationContextDTO;
import ai.shreds.shared.dtos.SharedRequestContextDTO;
import ai.shreds.shared.dtos.SharedRouteDTO;
import ai.shreds.shared.dtos.SharedSecurityHeadersDTO;
import ai.shreds.shared.dtos.SharedServerHttpRequestDTO;
import ai.shreds.shared.enums.SharedEnumHttpMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Primary adapter that implements a Global Gateway Filter to process authentication
 * for all incoming requests. It extracts JWTs, validates them, creates authentication contexts,
 * and adds security headers for downstream services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdapterGatewaySecurityFilter implements GlobalFilter, Ordered {

    private final ApplicationAuthenticationPort applicationAuthenticationPort;
    private final ApplicationRouteConfigPort applicationRouteConfigPort;
    private final ApplicationRequestContextPort applicationRequestContextPort;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_USER_ID_HEADER = "X-User-Id";
    private static final String X_USER_ROLES_HEADER = "X-User-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().pathWithinApplication().value();
        SharedEnumHttpMethod method = SharedEnumHttpMethod.valueOf(request.getMethod().name());
        
        log.debug("Processing request: {} {}", method, path);
        
        // Get route configuration for the current request
        SharedRouteDTO route = applicationRouteConfigPort.getRouteForRequest(path, method);
        if (route == null) {
            log.warn("No route configuration found for: {} {}", method, path);
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return response.setComplete();
        }
        
        try {
            // Create authentication context based on route requirements
            SharedAuthenticationContextDTO authContext;
            
            if (applicationRouteConfigPort.isAuthenticationRequired(route)) {
                String jwt = extractBearerToken(request);
                if (jwt == null || jwt.isEmpty()) {
                    log.warn("Authentication required but no JWT token provided for: {} {}", method, path);
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
                
                authContext = applicationAuthenticationPort.authenticateRequest(jwt, route);
                log.debug("Authenticated request for user: {}, roles: {}", 
                        authContext.getUserId().getValue(),
                        authContext.getUserRoles());
            } else {
                // Create anonymous context for non-protected routes
                String requestId = exchange.getRequest().getId();
                authContext = applicationAuthenticationPort.createAnonymousContext(requestId);
                log.debug("Created anonymous context for public endpoint: {} {}", method, path);
            }
            
            // Add security headers to the forwarded request
            SharedSecurityHeadersDTO securityHeaders = createSecurityHeaders(authContext);
            ServerWebExchange modifiedExchange = addSecurityHeaders(exchange, securityHeaders);
            
            // Build and publish request context for downstream processing
            SharedServerHttpRequestDTO requestDTO = SharedServerHttpRequestDTO.fromServerRequest(request);
            SharedRequestContextDTO requestContext = applicationRequestContextPort.buildRequestContext(
                    requestDTO, authContext);
            
            applicationRequestContextPort.publishRequestContext(requestContext);
            log.debug("Published request context with ID: {}", requestContext.getRequestId().getValue());
            
            // Continue the filter chain with the modified exchange
            return chain.filter(modifiedExchange);
            
        } catch (Exception e) {
            log.error("Error during authentication processing: {}", e.getMessage(), e);
            return handleAuthenticationError(exchange, e);
        }
    }

    @Override
    public int getOrder() {
        // Ensure this filter runs early in the chain
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    /**
     * Extracts the JWT token from the Authorization header.
     * 
     * @param request the incoming HTTP request
     * @return the extracted JWT token or null if not present
     */
    private String extractBearerToken(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().getFirst(AUTHORIZATION_HEADER))
                .filter(header -> header.startsWith(BEARER_PREFIX))
                .map(header -> header.substring(BEARER_PREFIX.length()))
                .orElse(null);
    }

    /**
     * Creates security headers DTO from the authentication context.
     *
     * @param authContext the authenticated user context
     * @return security headers DTO containing user ID and roles
     */
    private SharedSecurityHeadersDTO createSecurityHeaders(SharedAuthenticationContextDTO authContext) {
        SharedSecurityHeadersDTO headers = new SharedSecurityHeadersDTO();
        
        if (authContext.isAuthenticated()) {
            headers.setUserId(authContext.getUserId().getValue());
            
            // Convert roles set to comma-separated string
            String roles = String.join(",", 
                    authContext.getUserRoles().stream()
                            .map(Enum::name)
                            .toList());
            
            headers.setUserRoles(roles);
        } else {
            headers.setUserId("anonymous");
            headers.setUserRoles("GUEST");
        }
        
        return headers;
    }

    /**
     * Adds security headers to the exchange for downstream services.
     *
     * @param exchange the server web exchange
     * @param headers the security headers to add
     * @return modified ServerWebExchange with security headers
     */
    private ServerWebExchange addSecurityHeaders(ServerWebExchange exchange, SharedSecurityHeadersDTO headers) {
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(X_USER_ID_HEADER, headers.getUserId())
                .header(X_USER_ROLES_HEADER, headers.getUserRoles())
                .build();
        
        return exchange.mutate().request(mutatedRequest).build();
    }

    /**
     * Handles authentication errors by setting appropriate status codes and passing control
     * to the global exception handler.
     *
     * @param exchange the server web exchange
     * @param error the authentication error
     * @return Mono completing the response
     */
    private Mono<Void> handleAuthenticationError(ServerWebExchange exchange, Throwable error) {
        // The specific error handling is delegated to AdapterAuthenticationExceptionHandler
        // This just sets the initial response status
        ServerHttpResponse response = exchange.getResponse();
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        
        // We can add headers or modify the response as needed
        response.getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setStatusCode(status);
        
        // Let the exception bubble up to the global handler
        return Mono.error(error);
    }
}