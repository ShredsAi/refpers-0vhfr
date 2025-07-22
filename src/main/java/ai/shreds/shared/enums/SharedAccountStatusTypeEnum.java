package ai.shreds.shared.enums;

/**
 * Enumeration of possible account status types.
 * Used to determine if an account is in a valid state for authentication.
 */
public enum SharedAccountStatusTypeEnum {
    
    /**
     * Account is newly created but not yet verified/activated
     */
    PENDING,
    
    /**
     * Account is active and can authenticate
     */
    ACTIVE,
    
    /**
     * Account is temporarily suspended, cannot authenticate
     */
    SUSPENDED,
    
    /**
     * Account is permanently closed, cannot authenticate
     */
    CLOSED;
    
    /**
     * Check if this status allows authentication
     * @return true if authentication is allowed for this status
     */
    public boolean canAuthenticate() {
        return this == ACTIVE;
    }
    
    /**
     * Check if this status represents an inactive account
     * @return true if the account is inactive
     */
    public boolean isInactive() {
        return this != ACTIVE;
    }
}