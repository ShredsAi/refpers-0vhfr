package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationAuthenticationInputPort;
import ai.shreds.shared.dtos.SharedAccountStatusValidatedEventDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event listener for account status validation events.
 * This component listens for events published by the Account Management Shred
 * to validate account status during authentication flows.
 */
@Component
public class AdapterAccountStatusValidatedEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(AdapterAccountStatusValidatedEventListener.class);
    
    private final ApplicationAuthenticationInputPort applicationAuthenticationService;
    
    @Autowired
    public AdapterAccountStatusValidatedEventListener(
            ApplicationAuthenticationInputPort applicationAuthenticationService) {
        this.applicationAuthenticationService = applicationAuthenticationService;
    }
    
    /**
     * Handles account status validation events from the Account Management Shred.
     * This method is called when the Account Management Shred responds to an
     * authentication attempt event with the account's validation status.
     * 
     * @param event The account status validation event containing account validation results
     */
    @EventListener
    public void handleAccountStatusValidated(SharedAccountStatusValidatedEventDTO event) {
        try {
            logger.info("Received account status validation event for account: {}", event.getAccountId());
            
            // Validate the event data
            if (event.getAccountId() == null || event.getAccountId().trim().isEmpty()) {
                logger.error("Received account status validation event with null or empty account ID");
                return;
            }
            
            // Log the validation result
            if (event.getIsValid()) {
                logger.info("Account {} is valid with status: {}", event.getAccountId(), event.getAccountStatus());
            } else {
                logger.warn("Account {} is invalid with status: {}", event.getAccountId(), event.getAccountStatus());
            }
            
            // Delegate to the application service to handle the validation result
            if (event.getIsValid()) {
                // Account is valid, proceed with authentication
                applicationAuthenticationService.validateAccountStatus(event.getAccountId());
                logger.debug("Successfully validated account status for account: {}", event.getAccountId());
            } else {
                // Account is not valid, handle the failed authentication
                String reason = String.format("Account status is invalid: %s", event.getAccountStatus());
                applicationAuthenticationService.handleFailedAuthentication(event.getAccountId(), reason);
                logger.debug("Handled failed authentication for invalid account: {}", event.getAccountId());
            }
            
        } catch (Exception e) {
            logger.error("Error processing account status validation event for account: {}", 
                event.getAccountId(), e);
            
            // In case of error, treat as authentication failure for security
            try {
                applicationAuthenticationService.handleFailedAuthentication(
                    event.getAccountId(), 
                    "Error during account status validation: " + e.getMessage()
                );
            } catch (Exception innerException) {
                logger.error("Failed to handle authentication failure after validation error for account: {}", 
                    event.getAccountId(), innerException);
            }
        }
    }
    
    /**
     * Handles account status validation timeout events.
     * This method can be extended to handle cases where account validation
     * takes too long or times out.
     * 
     * @param accountId The account ID that timed out during validation
     */
    public void handleAccountValidationTimeout(String accountId) {
        try {
            logger.warn("Account status validation timed out for account: {}", accountId);
            
            // Handle timeout as authentication failure for security
            applicationAuthenticationService.handleFailedAuthentication(
                accountId, 
                "Account status validation timed out"
            );
            
        } catch (Exception e) {
            logger.error("Error handling account validation timeout for account: {}", accountId, e);
        }
    }
    
    /**
     * Validates the event data to ensure it contains required fields.
     * 
     * @param event The event to validate
     * @return true if the event is valid, false otherwise
     */
    private boolean isValidEvent(SharedAccountStatusValidatedEventDTO event) {
        if (event == null) {
            logger.error("Received null account status validation event");
            return false;
        }
        
        if (event.getAccountId() == null || event.getAccountId().trim().isEmpty()) {
            logger.error("Account status validation event missing account ID");
            return false;
        }
        
        if (event.getAccountStatus() == null || event.getAccountStatus().trim().isEmpty()) {
            logger.warn("Account status validation event missing account status for account: {}", 
                event.getAccountId());
            // This is not necessarily invalid, just log a warning
        }
        
        return true;
    }
    
    /**
     * Logs detailed event information for debugging purposes.
     * 
     * @param event The event to log
     */
    private void logEventDetails(SharedAccountStatusValidatedEventDTO event) {
        if (logger.isDebugEnabled()) {
            logger.debug("Account Status Validation Event Details: " +
                "accountId={}, isValid={}, accountStatus={}",
                event.getAccountId(),
                event.getIsValid(),
                event.getAccountStatus());
        }
    }
}