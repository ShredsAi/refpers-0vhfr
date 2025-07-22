package ai.shreds.application.services;

import ai.shreds.application.dtos.ApplicationMfaChallengeDTO;
import ai.shreds.application.ports.ApplicationMfaInputPort;
import ai.shreds.application.ports.ApplicationNotificationOutputPort;
import ai.shreds.application.ports.ApplicationCacheOutputPort;
import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import ai.shreds.domain.ports.DomainInputPortMfa;
import ai.shreds.shared.dtos.SharedMfaVerifyRequestDTO;
import ai.shreds.shared.dtos.SharedOAuth2TokenResponseDTO;
import ai.shreds.shared.dtos.SharedSensitiveOperationRequestEventDTO;
import ai.shreds.shared.dtos.SharedAdditionalAuthenticationRequiredEventDTO;
import ai.shreds.shared.enums.SharedMfaMethodEnum;
import ai.shreds.domain.entities.DomainMfaChallengeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.Instant;
import java.security.SecureRandom;

/**
 * Application service implementing multi-factor authentication operations.
 */
@Service
@Transactional
public class ApplicationMfaService implements ApplicationMfaInputPort {

    private final DomainInputPortMfa domainMfaService;
    private final ApplicationNotificationOutputPort notificationPort;
    private final ApplicationCacheOutputPort cachePort;
    private final ApplicationEventPublisherOutputPort eventPublisher;
    private final ApplicationJwtService jwtService;
    private final SecureRandom secureRandom;

    @Autowired
    public ApplicationMfaService(
            DomainInputPortMfa domainMfaService,
            ApplicationNotificationOutputPort notificationPort,
            ApplicationCacheOutputPort cachePort,
            ApplicationEventPublisherOutputPort eventPublisher,
            ApplicationJwtService jwtService) {
        this.domainMfaService = domainMfaService;
        this.notificationPort = notificationPort;
        this.cachePort = cachePort;
        this.eventPublisher = eventPublisher;
        this.jwtService = jwtService;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public ApplicationMfaChallengeDTO generateMfaChallenge(String accountId, String method) {
        try {
            // Validate method
            SharedMfaMethodEnum mfaMethod = SharedMfaMethodEnum.valueOf(method.toUpperCase());
            
            // Generate MFA challenge through domain service
            DomainMfaChallengeEntity challenge = domainMfaService.generateMfaChallenge(accountId, mfaMethod);
            
            // Generate verification code (6 digits)
            String verificationCode = String.format("%06d", secureRandom.nextInt(1000000));
            
            // Send verification code based on method
            switch (mfaMethod) {
                case SMS:
                    // Get phone number from account settings (placeholder implementation)
                    String phoneNumber = "+1234567890"; // This should come from account settings
                    notificationPort.sendSms(phoneNumber, "Your verification code is: " + verificationCode);
                    break;
                case EMAIL:
                    // Get email from account settings (placeholder implementation)
                    String email = "user@example.com"; // This should come from account settings
                    notificationPort.sendEmail(email, "MFA Verification Code", 
                        "Your verification code is: " + verificationCode + ". This code expires in 5 minutes.");
                    break;
                case TOTP:
                    // For TOTP, no code needs to be sent as user generates it from their authenticator app
                    break;
            }
            
            // Cache the verification code for validation
            cachePort.put("mfa_code:" + challenge.getChallengeId().toString(), verificationCode, 5 * 60 * 1000); // 5 minutes
            
            // Create challenge DTO
            ApplicationMfaChallengeDTO challengeDTO = new ApplicationMfaChallengeDTO(
                    challenge.getChallengeId().toString(),
                    accountId,
                    method,
                    challenge.getExpiresAt().toEpochMilli()
            );
            
            return challengeDTO;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate MFA challenge: " + e.getMessage(), e);
        }
    }

    @Override
    public SharedOAuth2TokenResponseDTO verifyMfaCode(SharedMfaVerifyRequestDTO request) {
        try {
            String challengeId = request.getChallengeId();
            String providedCode = request.getCode();
            
            // Get cached verification code
            String cachedCode = (String) cachePort.get("mfa_code:" + challengeId);
            if (cachedCode == null) {
                throw new RuntimeException("MFA challenge expired or not found");
            }
            
            // Verify the code through domain service
            boolean isValid = domainMfaService.verifyMfaCode(challengeId, providedCode);
            
            if (!isValid && !providedCode.equals(cachedCode)) {
                throw new RuntimeException("Invalid MFA code");
            }
            
            // Get challenge details to extract account ID
            // This is a placeholder - in real implementation, we'd get this from the challenge
            String accountId = "placeholder-account-id";
            
            // Generate tokens after successful MFA verification
            String accessToken = jwtService.generateAccessToken(accountId, 
                java.util.Map.of("account_id", accountId, "mfa_verified", true));
            String refreshToken = jwtService.generateRefreshToken(accountId);
            
            // Clean up used code
            cachePort.delete("mfa_code:" + challengeId);
            
            // Create token response
            SharedOAuth2TokenResponseDTO response = new SharedOAuth2TokenResponseDTO();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(3600L); // 1 hour
            
            return response;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify MFA code: " + e.getMessage(), e);
        }
    }

    @Override
    public void initiateStepUpAuthentication(SharedSensitiveOperationRequestEventDTO event) {
        try {
            String accountId = event.getAccountId();
            String operationType = event.getOperationType();
            
            // Check if MFA is required for this account
            if (!domainMfaService.isMfaRequired(accountId)) {
                return; // MFA not required for this account
            }
            
            // Generate step-up MFA challenge
            // Default to SMS for sensitive operations
            ApplicationMfaChallengeDTO challenge = generateMfaChallenge(accountId, "SMS");
            
            // Publish additional authentication required event
            SharedAdditionalAuthenticationRequiredEventDTO authEvent = new SharedAdditionalAuthenticationRequiredEventDTO();
            authEvent.setOperationId(UUID.randomUUID().toString());
            authEvent.setChallengeId(challenge.getChallengeId());
            authEvent.setMethod("SMS");
            
            eventPublisher.publishEvent(authEvent);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate step-up authentication: " + e.getMessage(), e);
        }
    }
}