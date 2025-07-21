package ai.shreds.adapter.primary;

import ai.shreds.adapter.exceptions.AdapterInvalidRequestException;
import ai.shreds.application.ports.ApplicationAuthenticationInputPort;
import ai.shreds.application.ports.ApplicationMfaInputPort;
import ai.shreds.application.ports.ApplicationPasswordInputPort;
import ai.shreds.shared.dtos.*;
import ai.shreds.shared.exceptions.SharedAuthorizationException;
import ai.shreds.shared.exceptions.SharedAccountLockedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

/**
 * REST Controller for authentication-related endpoints.
 * Handles login, MFA verification, password reset, and logout operations.
 */
@RestController
@RequestMapping("/auth")
@Validated
public class AdapterAuthenticationController {
    
    private final ApplicationAuthenticationInputPort applicationAuthenticationService;
    private final ApplicationMfaInputPort applicationMfaService;
    private final ApplicationPasswordInputPort applicationPasswordService;
    
    @Autowired
    public AdapterAuthenticationController(
            ApplicationAuthenticationInputPort applicationAuthenticationService,
            ApplicationMfaInputPort applicationMfaService,
            ApplicationPasswordInputPort applicationPasswordService) {
        this.applicationAuthenticationService = applicationAuthenticationService;
        this.applicationMfaService = applicationMfaService;
        this.applicationPasswordService = applicationPasswordService;
    }
    
    /**
     * Authenticates a user with username and password.
     * Returns tokens if MFA is not required, or MFA challenge if required.
     * 
     * @param request The login request containing username and password
     * @return Login response with tokens or MFA challenge
     */
    @PostMapping("/login")
    public ResponseEntity<SharedLoginResponseDTO> login(@Valid @RequestBody SharedLoginRequestDTO request) {
        try {
            // Validate input
            validateLoginRequest(request);
            
            // Perform authentication
            SharedLoginResponseDTO response = applicationAuthenticationService.authenticateUser(request);
            
            // Return appropriate status based on MFA requirement
            HttpStatus status = response.getMfaRequired() ? HttpStatus.ACCEPTED : HttpStatus.OK;
            return ResponseEntity.status(status).body(response);
            
        } catch (SharedAccountLockedException e) {
            throw new AdapterInvalidRequestException(
                String.format("Account is locked until %s after %d failed attempts", 
                    e.getLockedUntil(), e.getAttempts()),
                "account_locked"
            );
        } catch (SharedAuthorizationException e) {
            throw new AdapterInvalidRequestException(
                "Invalid credentials", 
                "invalid_credentials"
            );
        } catch (Exception e) {
            throw new AdapterInvalidRequestException(
                "Authentication failed: " + e.getMessage(), 
                "authentication_error"
            );
        }
    }
    
    /**
     * Verifies an MFA code and completes the authentication process.
     * 
     * @param request The MFA verification request containing challenge ID and code
     * @return Token response with access and refresh tokens
     */
    @PostMapping("/mfa/verify")
    public ResponseEntity<SharedOAuth2TokenResponseDTO> verifyMfa(@Valid @RequestBody SharedMfaVerifyRequestDTO request) {
        try {
            // Validate input
            validateMfaVerifyRequest(request);
            
            // Verify MFA code and complete authentication
            SharedOAuth2TokenResponseDTO response = applicationMfaService.verifyMfaCode(request);
            
            return ResponseEntity.ok(response);
            
        } catch (SharedAuthorizationException e) {
            throw new AdapterInvalidRequestException(
                "Invalid MFA code or expired challenge", 
                "invalid_mfa_code"
            );
        } catch (Exception e) {
            throw new AdapterInvalidRequestException(
                "MFA verification failed: " + e.getMessage(), 
                "mfa_verification_error"
            );
        }
    }
    
    /**
     * Initiates the password reset process for a given email address.
     * 
     * @param request The password reset request containing the email address
     * @return Confirmation response
     */
    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody SharedPasswordResetRequestDTO request) {
        try {
            // Validate input
            validatePasswordResetRequest(request);
            
            // Initiate password reset
            applicationPasswordService.initiatePasswordReset(request);
            
            // Return confirmation (don't reveal if email exists for security)
            Map<String, String> response = new HashMap<>();
            response.put("message", "If the email address exists in our system, you will receive password reset instructions.");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Don't reveal specific error details for security reasons
            Map<String, String> response = new HashMap<>();
            response.put("message", "An error occurred while processing your request. Please try again later.");
            response.put("status", "error");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Logs out a user by revoking their access token.
     * 
     * @param authHeader The Authorization header containing the Bearer token
     * @return Confirmation response
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from Authorization header
            String token = extractTokenFromHeader(authHeader);
            
            // Perform logout
            applicationAuthenticationService.logout(token);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully logged out");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            throw new AdapterInvalidRequestException(
                "Logout failed: " + e.getMessage(), 
                "logout_error"
            );
        }
    }
    
    /**
     * Global exception handler for authentication related errors.
     */
    @ExceptionHandler(AdapterInvalidRequestException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleInvalidRequest(
            AdapterInvalidRequestException e, HttpServletRequest request) {
        
        SharedErrorResponseDTO errorResponse = new SharedErrorResponseDTO(
            e.getErrorCode(),
            e.getMessage(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            request.getRequestURI()
        );
        
        HttpStatus status = determineHttpStatus(e.getErrorCode());
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Exception handler for shared authorization exceptions.
     */
    @ExceptionHandler(SharedAuthorizationException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleAuthorizationException(
            SharedAuthorizationException e, HttpServletRequest request) {
        
        SharedErrorResponseDTO errorResponse = new SharedErrorResponseDTO(
            e.getErrorCode(),
            e.getMessage(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Exception handler for account locked exceptions.
     */
    @ExceptionHandler(SharedAccountLockedException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleAccountLockedException(
            SharedAccountLockedException e, HttpServletRequest request) {
        
        SharedErrorResponseDTO errorResponse = new SharedErrorResponseDTO(
            "account_locked",
            String.format("Account is locked until %s after %d failed attempts", 
                e.getLockedUntil(), e.getAttempts()),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.LOCKED).body(errorResponse);
    }
    
    /**
     * Validates the login request.
     */
    private void validateLoginRequest(SharedLoginRequestDTO request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Username is required", "invalid_request");
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Password is required", "invalid_request");
        }
    }
    
    /**
     * Validates the MFA verification request.
     */
    private void validateMfaVerifyRequest(SharedMfaVerifyRequestDTO request) {
        if (request.getChallengeId() == null || request.getChallengeId().trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Challenge ID is required", "invalid_request");
        }
        
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Verification code is required", "invalid_request");
        }
    }
    
    /**
     * Validates the password reset request.
     */
    private void validatePasswordResetRequest(SharedPasswordResetRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Email address is required", "invalid_request");
        }
        
        // Basic email format validation
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new AdapterInvalidRequestException("Invalid email address format", "invalid_request");
        }
    }
    
    /**
     * Extracts the token from the Authorization header.
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AdapterInvalidRequestException("Invalid Authorization header format", "invalid_request");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        if (token.trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Token is required", "invalid_request");
        }
        
        return token;
    }
    
    /**
     * Determines the appropriate HTTP status code based on the error code.
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "invalid_credentials", "invalid_mfa_code" -> HttpStatus.UNAUTHORIZED;
            case "account_locked" -> HttpStatus.LOCKED;
            case "invalid_request" -> HttpStatus.BAD_REQUEST;
            case "authentication_error", "mfa_verification_error", "logout_error" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}