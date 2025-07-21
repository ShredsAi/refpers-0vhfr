package ai.shreds.adapter.primary;

import ai.shreds.adapter.exceptions.AdapterInvalidRequestException;
import ai.shreds.application.ports.ApplicationOAuth2InputPort;
import ai.shreds.shared.value_objects.SharedOAuth2AuthorizeRequestParams;
import ai.shreds.shared.value_objects.SharedOAuth2TokenRequestParams;
import ai.shreds.shared.value_objects.SharedOAuth2RefreshRequestParams;
import ai.shreds.shared.dtos.SharedOAuth2TokenResponseDTO;
import ai.shreds.shared.dtos.SharedErrorResponseDTO;
import ai.shreds.application.dtos.ApplicationAuthorizationCodeDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller for OAuth 2.0 Authorization Server endpoints.
 * Handles authorization flow initiation, token exchange, and token refresh.
 */
@RestController
@RequestMapping("/oauth")
@Validated
public class AdapterOAuth2Controller {
    
    private static final Logger logger = LoggerFactory.getLogger(AdapterOAuth2Controller.class);
    private final ApplicationOAuth2InputPort applicationOAuth2Service;
    
    @Autowired
    public AdapterOAuth2Controller(ApplicationOAuth2InputPort applicationOAuth2Service) {
        this.applicationOAuth2Service = applicationOAuth2Service;
    }
    
    /**
     * Initiates the OAuth 2.0 Authorization Code Flow with PKCE.
     */
    @GetMapping("/authorize")
    public ResponseEntity<Void> initiateAuthorization(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("response_type") String responseType,
            @RequestParam("scope") String scope,
            @RequestParam("state") String state,
            @RequestParam("code_challenge") String codeChallenge,
            @RequestParam("code_challenge_method") String codeChallengeMethod) {
        
        try {
            logger.info("Starting OAuth2 authorization request for clientId: {}, redirectUri: {}", clientId, redirectUri);
            
            // Validate required parameters
            validateAuthorizationRequest(clientId, redirectUri, responseType, codeChallenge, codeChallengeMethod);
            
            // Create request parameters object
            SharedOAuth2AuthorizeRequestParams params = new SharedOAuth2AuthorizeRequestParams(
                clientId, redirectUri, responseType, scope, state, codeChallenge, codeChallengeMethod
            );
            
            logger.debug("Calling application service to initiate authorization flow");
            
            // Process authorization request
            ApplicationAuthorizationCodeDTO authorizationCode = applicationOAuth2Service.initiateAuthorizationFlow(params);
            
            logger.info("Successfully generated authorization code for clientId: {}", clientId);
            
            // Build redirect URL with authorization code
            String redirectUrl = buildRedirectUrl(redirectUri, authorizationCode.getCode(), state);
            
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
                    
        } catch (Exception e) {
            logger.error("OAuth2 authorization failed for clientId: {}, error: {}", clientId, e.getMessage(), e);
            
            // Log error and redirect to error page or return error response
            String errorRedirectUrl = buildErrorRedirectUrl(redirectUri, "server_error", 
                    "An error occurred during authorization: " + e.getMessage(), state);
            
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", errorRedirectUrl)
                    .build();
        }
    }
    
    /**
     * Exchanges an authorization code for access and refresh tokens.
     */
    @PostMapping("/token")
    public ResponseEntity<SharedOAuth2TokenResponseDTO> exchangeToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("code") String code,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("client_id") String clientId,
            @RequestParam("code_verifier") String codeVerifier) {
        
        try {
            // Validate token request
            validateTokenRequest(grantType, code, clientId, codeVerifier);
            
            // Create request parameters object
            SharedOAuth2TokenRequestParams params = new SharedOAuth2TokenRequestParams(
                grantType, code, redirectUri, clientId, codeVerifier
            );
            
            // Exchange code for tokens
            SharedOAuth2TokenResponseDTO tokenResponse = applicationOAuth2Service.exchangeCodeForTokens(params);
            
            return ResponseEntity.ok(tokenResponse);
            
        } catch (AdapterInvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new AdapterInvalidRequestException(
                "Token exchange failed: " + e.getMessage(), 
                "invalid_grant"
            );
        }
    }
    
    /**
     * Refreshes an access token using a refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<SharedOAuth2TokenResponseDTO> refreshToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("refresh_token") String refreshToken) {
        
        try {
            // Validate refresh request
            validateRefreshRequest(grantType, refreshToken);
            
            // Create request parameters object
            SharedOAuth2RefreshRequestParams params = new SharedOAuth2RefreshRequestParams(
                grantType, refreshToken
            );
            
            // Refresh access token
            SharedOAuth2TokenResponseDTO tokenResponse = applicationOAuth2Service.refreshAccessToken(params);
            
            return ResponseEntity.ok(tokenResponse);
            
        } catch (AdapterInvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new AdapterInvalidRequestException(
                "Token refresh failed: " + e.getMessage(), 
                "invalid_grant"
            );
        }
    }
    
    /**
     * Global exception handler for OAuth2 related errors.
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
     * Validates the authorization request parameters.
     */
    private void validateAuthorizationRequest(String clientId, String redirectUri, 
            String responseType, String codeChallenge, String codeChallengeMethod) {
        
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Client ID is required", "invalid_request");
        }
        
        if (redirectUri == null || redirectUri.trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Redirect URI is required", "invalid_request");
        }
        
        if (!"code".equals(responseType)) {
            throw new AdapterInvalidRequestException("Unsupported response type", "unsupported_response_type");
        }
        
        if (codeChallenge == null || codeChallenge.trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Code challenge is required for PKCE", "invalid_request");
        }
        
        if (!"S256".equals(codeChallengeMethod)) {
            throw new AdapterInvalidRequestException("Only S256 code challenge method is supported", "invalid_request");
        }
    }
    
    /**
     * Validates the token request parameters.
     */
    private void validateTokenRequest(String grantType, String code, String clientId, String codeVerifier) {
        if (!"authorization_code".equals(grantType)) {
            throw new AdapterInvalidRequestException("Unsupported grant type", "unsupported_grant_type");
        }
        
        if (code == null || code.trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Authorization code is required", "invalid_request");
        }
        
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Client ID is required", "invalid_request");
        }
        
        if (codeVerifier == null || codeVerifier.trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Code verifier is required for PKCE", "invalid_request");
        }
    }
    
    /**
     * Validates the refresh token request parameters.
     */
    private void validateRefreshRequest(String grantType, String refreshToken) {
        if (!"refresh_token".equals(grantType)) {
            throw new AdapterInvalidRequestException("Unsupported grant type", "unsupported_grant_type");
        }
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new AdapterInvalidRequestException("Refresh token is required", "invalid_request");
        }
    }
    
    /**
     * Builds the redirect URL with authorization code and state.
     */
    private String buildRedirectUrl(String redirectUri, String authorizationCode, String state) {
        StringBuilder url = new StringBuilder(redirectUri);
        url.append(redirectUri.contains("?") ? "&" : "?");
        url.append("code=").append(authorizationCode);
        
        if (state != null && !state.trim().isEmpty()) {
            url.append("&state=").append(state);
        }
        
        return url.toString();
    }
    
    /**
     * Builds the error redirect URL.
     */
    private String buildErrorRedirectUrl(String redirectUri, String error, String errorDescription, String state) {
        StringBuilder url = new StringBuilder(redirectUri);
        url.append(redirectUri.contains("?") ? "&" : "?");
        url.append("error=").append(error);
        url.append("&error_description=").append(errorDescription);
        
        if (state != null && !state.trim().isEmpty()) {
            url.append("&state=").append(state);
        }
        
        return url.toString();
    }
    
    /**
     * Determines the appropriate HTTP status code based on the error code.
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "unauthorized_client", "access_denied" -> HttpStatus.UNAUTHORIZED;
            case "unsupported_response_type", "unsupported_grant_type" -> HttpStatus.BAD_REQUEST;
            case "invalid_scope" -> HttpStatus.BAD_REQUEST;
            case "server_error" -> HttpStatus.INTERNAL_SERVER_ERROR;
            case "temporarily_unavailable" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}