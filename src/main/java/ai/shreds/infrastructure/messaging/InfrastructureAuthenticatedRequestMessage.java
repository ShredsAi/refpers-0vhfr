package ai.shreds.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

public class InfrastructureAuthenticatedRequestMessage {

    private final String correlationId;
    private final Instant timestamp;
    private final String requestContext;
    private final Map<String, String> headers;

    @JsonCreator
    public InfrastructureAuthenticatedRequestMessage(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("requestContext") String requestContext,
            @JsonProperty("headers") Map<String, String> headers) {
        this.correlationId = correlationId;
        this.timestamp = timestamp;
        this.requestContext = requestContext;
        this.headers = headers;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getRequestContext() {
        return requestContext;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize message", e);
        }
    }

    public static InfrastructureAuthenticatedRequestMessage fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, InfrastructureAuthenticatedRequestMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize message", e);
        }
    }
}
