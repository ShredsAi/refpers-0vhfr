package ai.shreds.infrastructure.exceptions;

/**
 * Exception thrown for Redis operation failures.
 */
public class InfrastructureRedisException extends RuntimeException {
    private final String operation;
    private final String key;

    public InfrastructureRedisException(String message, String operation, String key) {
        super(message);
        this.operation = operation;
        this.key = key;
    }

    public String getOperation() {
        return operation;
    }

    public String getKey() {
        return key;
    }
}
