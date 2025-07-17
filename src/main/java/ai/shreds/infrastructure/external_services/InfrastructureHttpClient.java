package ai.shreds.infrastructure.external_services;

import ai.shreds.infrastructure.exceptions.InfrastructureJwksRetrievalException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class InfrastructureHttpClient {

    private final WebClient webClient;
    private final Duration timeout;

    public InfrastructureHttpClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
        this.timeout = Duration.ofSeconds(30);
    }

    public <T> Mono<T> get(String url, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(timeout)
                .onErrorMap(WebClientResponseException.class, e -> 
                    new InfrastructureJwksRetrievalException(
                            "Failed to retrieve JWKS: " + e.getMessage(),
                            url,
                            e.getStatusCode().value()))
                .onErrorMap(Exception.class, e -> 
                    new InfrastructureJwksRetrievalException(
                            "Failed to retrieve JWKS: " + e.getMessage(),
                            url,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    public <T> Mono<T> post(String url, Object body, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(timeout)
                .onErrorMap(WebClientResponseException.class, e -> 
                    new RuntimeException("HTTP request failed: " + e.getMessage(), e));
    }

    // Blocking versions for backward compatibility where absolutely necessary
    public <T> T getBlocking(String url, Class<T> responseType) {
        return get(url, responseType).block();
    }

    public <T> T postBlocking(String url, Object body, Class<T> responseType) {
        return post(url, body, responseType).block();
    }
}