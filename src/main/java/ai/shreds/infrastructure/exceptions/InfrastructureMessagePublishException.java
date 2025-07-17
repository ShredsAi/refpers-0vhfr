package ai.shreds.infrastructure.exceptions;

/**
 * Exception thrown when message publishing fails.
 */
public class InfrastructureMessagePublishException extends RuntimeException {
    private final String exchange;
    private final String routingKey;

    public InfrastructureMessagePublishException(String message, String exchange, String routingKey) {
        super(message);
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }
}
