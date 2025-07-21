package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationPasswordInputPort;
import ai.shreds.application.ports.ApplicationNotificationOutputPort;
import ai.shreds.application.ports.ApplicationCacheOutputPort;
import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import ai.shreds.domain.ports.DomainInputPortPasswordManagement;
import ai.shreds.shared.dtos.SharedPasswordResetRequestDTO;
import ai.shreds.shared.dtos.SharedPasswordChangeRequestDTO;
import ai.shreds.shared.dtos.SharedPasswordChangedEventDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Application service implementing password management operations.
 */
@Service
@Transactional
public class ApplicationPasswordService implements ApplicationPasswordInputPort {

    private final DomainInputPortPasswordManagement domainPasswordService;
    private final ApplicationNotificationOutputPort notificationPort;
    private final ApplicationCacheOutputPort cachePort;
    private final ApplicationEventPublisherOutputPort eventPublisher;
    private final SecureRandom secureRandom;

    @Autowired
    public ApplicationPasswordService(
            DomainInputPortPasswordManagement domainPasswordService,
            ApplicationNotificationOutputPort notificationPort,
            ApplicationCacheOutputPort cachePort,
            ApplicationEventPublisherOutputPort eventPublisher) {
        this.domainPasswordService = domainPasswordService;
        this.notificationPort = notificationPort;
        this.cachePort = cachePort;
        this.eventPublisher = eventPublisher;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public void initiatePasswordReset(SharedPasswordResetRequestDTO request) {
        try {
            String email = request.getEmail();
            
            // Check rate limiting for password reset requests
            String rateLimitKey = "password_reset_rate:" + email;
            if (cachePort.exists(rateLimitKey)) {
                throw new RuntimeException("Password reset request rate limit exceeded. Please try again later.");
            }
            
            // Generate password reset token through domain service
            String resetToken = domainPasswordService.generatePasswordResetToken(email);
            
            if (resetToken == null) {
                // Email not found, but don't reveal this for security reasons
                // Just log and pretend success
                System.out.println("Password reset requested for non-existent email: " + email);
                return;
            }
            
            // Cache the reset token with 24-hour expiration
            long tokenExpiry = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
            cachePort.put(
                "password_reset_token:" + resetToken, 
                email, 
                tokenExpiry
            );
            
            // Generate password reset link
            String resetLink = "https://example.com/reset-password?token=" + resetToken;
            
            // Send password reset email
            String emailSubject = "Password Reset Request";
            String emailBody = String.format(
                "Hello,\n\n" +
                "You have requested a password reset for your account.\n\n" +
                "Please click the following link to reset your password:\n" +
                "%s\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Authentication Service",
                resetLink
            );
            
            notificationPort.sendEmail(email, emailSubject, emailBody);
            
            // Set rate limiting (prevent multiple requests for 15 minutes)
            cachePort.put(rateLimitKey, "limited", 15 * 60 * 1000);
            
        } catch (Exception e) {
            // Log the error but don't expose details to prevent enumeration attacks
            System.err.println("Failed to process password reset request: " + e.getMessage());
            throw new RuntimeException("Failed to process password reset request");
        }
    }

    @Override
    public void changePassword(SharedPasswordChangeRequestDTO request) {
        try {
            String accountId = request.getAccountId();
            String currentPassword = request.getCurrentPassword();
            String newPassword = request.getNewPassword();
            
            // Validate password policy through domain service
            if (!domainPasswordService.validatePasswordPolicy(newPassword)) {
                throw new RuntimeException("New password does not meet security requirements");
            }
            
            // Change password through domain service
            domainPasswordService.changePassword(accountId, currentPassword, newPassword);
            
            // Publish password changed event
            SharedPasswordChangedEventDTO passwordChangedEvent = new SharedPasswordChangedEventDTO();
            passwordChangedEvent.setAccountId(accountId);
            passwordChangedEvent.setChangedAt(Instant.now().toString());
            passwordChangedEvent.setMethod("USER_INITIATED");
            
            eventPublisher.publishEvent(passwordChangedEvent);
            
            // Send confirmation email (get email from account service in real implementation)
            String email = "user@example.com"; // Placeholder - should get from account service
            String emailSubject = "Password Changed Successfully";
            String emailBody = String.format(
                "Hello,\n\n" +
                "Your password has been successfully changed on %s.\n\n" +
                "If you did not make this change, please contact support immediately.\n\n" +
                "Best regards,\n" +
                "Authentication Service",
                Instant.now().toString()
            );
            
            notificationPort.sendEmail(email, emailSubject, emailBody);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to change password: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process password reset with token (this would be called from a separate endpoint).
     * @param resetToken the password reset token
     * @param newPassword the new password to set
     */
    public void processPasswordReset(String resetToken, String newPassword) {
        try {
            // Get email from cached token
            String email = (String) cachePort.get("password_reset_token:" + resetToken);
            if (email == null) {
                throw new RuntimeException("Invalid or expired password reset token");
            }
            
            // Validate password policy
            if (!domainPasswordService.validatePasswordPolicy(newPassword)) {
                throw new RuntimeException("New password does not meet security requirements");
            }
            
            // Reset password through domain service
            domainPasswordService.resetPassword(resetToken, newPassword);
            
            // Clear used token
            cachePort.delete("password_reset_token:" + resetToken);
            
            // Send confirmation email
            String emailSubject = "Password Reset Successful";
            String emailBody = String.format(
                "Hello,\n\n" +
                "Your password has been successfully reset on %s.\n\n" +
                "If you did not perform this action, please contact support immediately.\n\n" +
                "Best regards,\n" +
                "Authentication Service",
                Instant.now().toString()
            );
            
            notificationPort.sendEmail(email, emailSubject, emailBody);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to process password reset: " + e.getMessage(), e);
        }
    }
}