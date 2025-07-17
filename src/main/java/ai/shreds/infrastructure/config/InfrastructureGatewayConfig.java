package ai.shreds.infrastructure.config;

import ai.shreds.shared.value_objects.SharedValueDuration;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class InfrastructureGatewayConfig {

    @Value("${gateway.default-timeout:30000}")
    private Long defaultTimeoutMillis;

    @Value("${gateway.retry-attempts:3}")
    private Integer retryAttempts;

    @Value("${gateway.circuit-breaker-threshold:50}")
    private Integer circuitBreakerThreshold;

    @Value("${gateway.routes-refresh-interval:300}")
    private Long routesRefreshIntervalSeconds;

    public SharedValueDuration getDefaultTimeout() {
        return SharedValueDuration.fromMillis(defaultTimeoutMillis);
    }

    public SharedValueDuration getRoutesRefreshInterval() {
        return SharedValueDuration.fromSeconds(routesRefreshIntervalSeconds);
    }
}
