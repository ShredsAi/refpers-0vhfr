package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationMfaInputPort;
import ai.shreds.shared.dtos.SharedSensitiveOperationRequestEventDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event listener for sensitive operation request events.
 * This component listens for events from other shreds (e.g., Financial & Payment Management)
 * that require step-up authentication for sensitive operations.
 */
@Component
public class AdapterSensitiveOperationRequestEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(AdapterSensitiveOperationRequestEventListener.class);
    
    private final ApplicationMfaInputPort applicationMfaService;
    
    @Autowired
    public AdapterSensitiveOperationRequestEventListener(
            ApplicationMfaInputPort applicationMfaService) {
        this.applicationMfaService = applicationMfaService;
    }
    
    /**
     * Handles sensitive operation request events from other shreds.
     * This method is called when other shreds need additional authentication
     * for sensitive operations like high-value transactions, account changes, etc.
     * 
     * @param event The sensitive operation request event containing operation details
     */
    @EventListener
    public void handleSensitiveOperationRequest(SharedSensitiveOperationRequestEventDTO event) {
        try {
            logger.info("Received sensitive operation request event for operation: {} by account: {}", 
                event.getOperationType(), event.getAccountId());
            
            // Validate the event data
            if (!isValidEvent(event)) {
                logger.error("Invalid sensitive operation request event received");
                return;
            }
            
            // Log event details for debugging
            logEventDetails(event);
            
            // Check if the operation requires MFA based on the required auth level
            if (requiresMfaAuthentication(event.getRequiredAuthLevel())) {
                logger.info("Initiating step-up MFA authentication for operation: {} by account: {}", 
                    event.getOperationType(), event.getAccountId());
                
                // Delegate to the application MFA service to initiate step-up authentication
                applicationMfaService.initiateStepUpAuthentication(event);
                
                logger.debug("Successfully initiated step-up authentication for operation: {} by account: {}", 
                    event.getOperationType(), event.getAccountId());
                    
            } else {
                logger.info("Operation: {} by account: {} does not require additional MFA", 
                    event.getOperationType(), event.getAccountId());
                
                // For operations that don't require MFA, we could still log the attempt
                // or perform other security checks as needed
                handleNonMfaOperation(event);
            }
            
        } catch (Exception e) {
            logger.error("Error processing sensitive operation request event for operation: {} by account: {}", 
                event.getOperationType(), event.getAccountId(), e);
            
            // In case of error, we should notify the requesting shred that
            // the additional authentication could not be initiated
            handleAuthenticationInitiationError(event, e);
        }
    }
    
    /**
     * Determines if the required authentication level necessitates MFA.
     * 
     * @param requiredAuthLevel The required authentication level from the event
     * @return true if MFA is required, false otherwise
     */
    private boolean requiresMfaAuthentication(String requiredAuthLevel) {
        if (requiredAuthLevel == null || requiredAuthLevel.trim().isEmpty()) {
            logger.warn("No required authentication level specified, defaulting to no MFA required");
            return false;
        }
        
        return switch (requiredAuthLevel.toUpperCase()) {
            case "MFA_REQUIRED", "HIGH_SECURITY", "STEP_UP_AUTH" -> {
                logger.debug("Authentication level '{}' requires MFA", requiredAuthLevel);
                yield true;
            }
            case "BASIC_AUTH", "NO_ADDITIONAL_AUTH", "STANDARD" -> {
                logger.debug("Authentication level '{}' does not require MFA", requiredAuthLevel);
                yield false;
            }
            default -> {
                logger.warn("Unknown authentication level '{}', defaulting to MFA required for security", 
                    requiredAuthLevel);
                yield true; // Default to requiring MFA for unknown levels for security
            }
        };
    }
    
    /**
     * Handles operations that don't require additional MFA but may need other processing.
     * 
     * @param event The sensitive operation request event
     */
    private void handleNonMfaOperation(SharedSensitiveOperationRequestEventDTO event) {
        // Log the operation for audit purposes
        logger.info("Non-MFA sensitive operation logged: {} by account: {} with auth level: {}", 
            event.getOperationType(), event.getAccountId(), event.getRequiredAuthLevel());
        
        // Could add additional security checks here, such as:
        // - Rate limiting checks
        // - Geolocation verification
        // - Device fingerprinting
        // - Time-based access controls
        
        // For now, we just log it for audit trail
    }
    
    /**
     * Handles errors that occur during authentication initiation.
     * 
     * @param event The original event that caused the error
     * @param error The exception that occurred
     */
    private void handleAuthenticationInitiationError(SharedSensitiveOperationRequestEventDTO event, Exception error) {
        logger.error("Failed to initiate additional authentication for operation: {} by account: {}", 
            event.getOperationType(), event.getAccountId());
        
        // Here we could publish an event back to the requesting shred to notify
        // that additional authentication could not be initiated
        // For now, we log the error
        
        // Future enhancement: Publish an AuthenticationInitiationFailedEvent
        // that the requesting shred can listen to and handle appropriately
    }
    
    /**
     * Validates the event data to ensure it contains required fields.
     * 
     * @param event The event to validate
     * @return true if the event is valid, false otherwise
     */
    private boolean isValidEvent(SharedSensitiveOperationRequestEventDTO event) {
        if (event == null) {
            logger.error("Received null sensitive operation request event");
            return false;
        }
        
        if (event.getAccountId() == null || event.getAccountId().trim().isEmpty()) {
            logger.error("Sensitive operation request event missing account ID");
            return false;
        }
        
        if (event.getOperationType() == null || event.getOperationType().trim().isEmpty()) {
            logger.error("Sensitive operation request event missing operation type for account: {}", 
                event.getAccountId());
            return false;
        }
        
        if (event.getRequiredAuthLevel() == null || event.getRequiredAuthLevel().trim().isEmpty()) {
            logger.warn("Sensitive operation request event missing required auth level for account: {} and operation: {}", 
                event.getAccountId(), event.getOperationType());
            // This is not necessarily invalid, just log a warning and default to MFA required
        }
        
        return true;
    }
    
    /**
     * Logs detailed event information for debugging purposes.
     * 
     * @param event The event to log
     */
    private void logEventDetails(SharedSensitiveOperationRequestEventDTO event) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sensitive Operation Request Event Details: " +
                "operationType={}, accountId={}, requiredAuthLevel={}",
                event.getOperationType(),
                event.getAccountId(),
                event.getRequiredAuthLevel());
        }
    }
    
    /**
     * Gets security configuration for specific operation types.
     * This can be used to customize MFA requirements based on operation type.
     * 
     * @param operationType The type of operation being performed
     * @return Security configuration for the operation
     */
    private String getSecurityConfigurationForOperation(String operationType) {
        // This could be enhanced to read from a configuration service
        // or database to allow dynamic security policy configuration
        return switch (operationType.toUpperCase()) {
            case "HIGH_VALUE_TRANSACTION", "WIRE_TRANSFER" -> "MFA_REQUIRED";
            case "PASSWORD_CHANGE", "EMAIL_CHANGE" -> "MFA_REQUIRED";
            case "PROFILE_UPDATE", "SETTINGS_CHANGE" -> "BASIC_AUTH";
            default -> "MFA_REQUIRED"; // Default to highest security for unknown operations
        };
    }
}