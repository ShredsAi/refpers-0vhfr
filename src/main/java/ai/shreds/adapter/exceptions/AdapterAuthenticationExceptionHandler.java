package ai.shreds.adapter.exceptions;

import ai.shreds.application.exceptions.ApplicationAuthenticationException;
import ai.shreds.application.exceptions.ApplicationAuthorizationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import org.springframework.core.io.buffer.DataBuffer;

/**
 * Global exception handler for authentication and authorization errors in the gateway filter chain.
 * It captures exceptions thrown during the authentication process and transforms them into
 * structured error responses.
 */
@Slf4j
@Component
@Order(-2) // High precedence to catch security exceptions before other handlers
@RequiredArgsConstructor
public class AdapterAuthenticationExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status;
        AdapterAuthenticationErrorResponse errorResponse;
        String path = exchange.getRequest().getPath().value();
        Long timestamp = System.currentTimeMillis();

        if (ex instanceof ApplicationAuthenticationException authEx) {
            status = HttpStatus.UNAUTHORIZED;
            errorResponse = new AdapterAuthenticationErrorResponse(
                    authEx.getErrorCode(), 
                    authEx.getMessage(), 
                    path, 
                    timestamp);
            log.warn("Authentication failed for path {}: {}", path, authEx.getMessage());
        } else if (ex instanceof ApplicationAuthorizationException authzEx) {
            status = HttpStatus.FORBIDDEN;
            errorResponse = new AdapterAuthenticationErrorResponse(
                    "insufficient_permissions", 
                    "User does not have the required permissions", 
                    path, 
                    timestamp);
            log.warn("Authorization failed for path {}: {}", path, authzEx.getMessage());
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorResponse = new AdapterAuthenticationErrorResponse(
                    "server_error", 
                    "An unexpected error occurred", 
                    path, 
                    timestamp);
            log.error("Unexpected error during authentication processing: {}", ex.getMessage(), ex);
        }

        response.setStatusCode(status);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response: {}", e.getMessage());
            bytes = ("{\"error\":\"serialization_error\"}").getBytes();
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Flux.just(buffer));
    }
}