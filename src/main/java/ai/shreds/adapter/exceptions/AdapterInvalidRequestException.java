package ai.shreds.adapter.exceptions;

import java.util.Map;
import java.util.HashMap;

/**
 * Exception thrown when an invalid request is received by the adapter layer.
 * This exception provides detailed information about the validation error.
 */
public class AdapterInvalidRequestException extends RuntimeException {
    
    private final String errorCode;
    private final Map<String, Object> details;
    
    /**
     * Constructor with message and error code.
     * 
     * @param message The error message
     * @param errorCode The specific error code for this exception
     */
    public AdapterInvalidRequestException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }
    
    /**
     * Constructor with message, error code and additional details.
     * 
     * @param message The error message
     * @param errorCode The specific error code for this exception
     * @param details Additional details about the error
     */
    public AdapterInvalidRequestException(String message, String errorCode, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details != null ? new HashMap<>(details) : new HashMap<>();
    }
    
    /**
     * Gets the error code associated with this exception.
     * 
     * @return The error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets additional details about the error.
     * 
     * @return Map containing error details
     */
    public Map<String, Object> getDetails() {
        return new HashMap<>(details);
    }
    
    /**
     * Adds a detail to the error details map.
     * 
     * @param key The detail key
     * @param value The detail value
     */
    public void addDetail(String key, Object value) {
        this.details.put(key, value);
    }
}