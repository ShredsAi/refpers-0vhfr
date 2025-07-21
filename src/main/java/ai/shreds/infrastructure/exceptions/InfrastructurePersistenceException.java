package ai.shreds.infrastructure.exceptions;

/**
 * Exception thrown when persistence operations fail in the infrastructure layer.
 * This exception provides context about the entity type and operation that failed.
 */
public class InfrastructurePersistenceException extends RuntimeException {

    private final String entityType;
    private final String operation;

    public InfrastructurePersistenceException(String message, String entityType, String operation) {
        super(message);
        this.entityType = entityType;
        this.operation = operation;
    }

    public InfrastructurePersistenceException(String message, String entityType, String operation, Throwable cause) {
        super(message, cause);
        this.entityType = entityType;
        this.operation = operation;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return String.format("InfrastructurePersistenceException{entityType='%s', operation='%s', message='%s'}",
                entityType, operation, getMessage());
    }
}