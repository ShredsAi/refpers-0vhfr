package ai.shreds.application.services;

import ai.shreds.application.dtos.ApplicationAuthorizationCodeDTO;
import ai.shreds.application.exceptions.ApplicationInvalidAuthCodeException;
import ai.shreds.application.exceptions.ApplicationTokenExpiredException;
import ai.shreds.application.ports.ApplicationCacheOutputPort;
import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import ai.shreds.application.ports.ApplicationOAuth2InputPort;
import ai.shreds.domain.ports.DomainInputPortOAuth2Authorization;
import ai.shreds.domain.ports.DomainInputPortTokenService;
import ai.shreds.shared.dtos.SharedOAuth2TokenResponseDTO;
import ai.shreds.shared.value_objects.SharedOAuth2AuthorizeRequestParams;
import ai.shreds.shared.value_objects.SharedOAuth2RefreshRequestParams;
import ai.shreds.shared.value_objects.SharedOAuth2TokenRequestParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Application service implementing OAuth 2.0 authorization server operations.
 */
@Service
public class ApplicationOAuth2Service implements ApplicationOAuth2InputPort {

    private final DomainInputPortOAuth2Authorization domainOAuth2AuthorizationService;
    private final DomainInputPortTokenService domainTokenService;
    private final ApplicationCacheOutputPort cachePort;
    private final ApplicationEventPublisherOutputPort eventPublisher;

    @Autowired
    public ApplicationOAuth2Service(
            DomainInputPortOAuth2Authorization domainOAuth2AuthorizationService,
            DomainInputPortTokenService domainTokenService,
            ApplicationCacheOutputPort cachePort,
            ApplicationEventPublisherOutputPort eventPublisher) {
        this.domainOAuth2AuthorizationService = domainOAuth2AuthorizationService;
        this.domainTokenService = domainTokenService;
        this.cachePort = cachePort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ApplicationAuthorizationCodeDTO initiateAuthorizationFlow(SharedOAuth2AuthorizeRequestParams params) {
        try {
            // Validate request parameters
            if (params.getClientId() == null || params.getClientId().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Client ID cannot be empty", params.getClientId());
            }

            if (params.getRedirectUri() == null || params.getRedirectUri().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Redirect URI cannot be empty", params.getClientId());
            }

            if (params.getCodeChallenge() == null || params.getCodeChallenge().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Code challenge cannot be empty", params.getClientId());
            }

            // Extract accountId from state parameter or use a placeholder for pre-authenticated flows
            String accountId = params.getState() != null ? params.getState() : "authenticated-user";

            // Generate authorization code through domain service
            String authCode = domainOAuth2AuthorizationService.generateAuthorizationCode(
                    accountId, // Fixed: using accountId instead of clientId
                    params.getClientId(),
                    params.getRedirectUri(),
                    params.getCodeChallenge()
            );

            // Create authorization code DTO with expiration
            long expiresAt = System.currentTimeMillis() + (10 * 60 * 1000); // 10 minutes
            ApplicationAuthorizationCodeDTO authCodeDTO = new ApplicationAuthorizationCodeDTO(
                    authCode,
                    params.getClientId(),
                    params.getRedirectUri(),
                    params.getCodeChallenge(),
                    accountId, // Using the correct accountId
                    expiresAt
            );

            // Cache the authorization code
            cachePort.put("auth_code:" + authCode, authCodeDTO, 10 * 60 * 1000L);
            
            return authCodeDTO;

        } catch (ApplicationInvalidAuthCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationInvalidAuthCodeException("Failed to initiate authorization flow: " + e.getMessage(), 
                    params != null ? params.getClientId() : "unknown");
        }
    }

    @Override
    public SharedOAuth2TokenResponseDTO exchangeCodeForTokens(SharedOAuth2TokenRequestParams params) {
        try {
            // Validate request parameters
            if (params.getCode() == null || params.getCode().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Authorization code cannot be empty", params.getCode());
            }

            if (params.getClientId() == null || params.getClientId().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Client ID cannot be empty", params.getCode());
            }

            if (params.getCodeVerifier() == null || params.getCodeVerifier().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Code verifier cannot be empty", params.getCode());
            }

            // Validate and exchange code through domain service
            var session = domainOAuth2AuthorizationService.validateAndExchangeCode(
                    params.getCode(),
                    params.getClientId(),
                    params.getCodeVerifier()
            );

            // Remove cached authorization code as it's now used
            cachePort.delete("auth_code:" + params.getCode());

            // Create token response
            SharedOAuth2TokenResponseDTO response = new SharedOAuth2TokenResponseDTO();
            response.setAccessToken(session.getAccessTokenHash());
            response.setRefreshToken(session.getRefreshTokenHash());
            response.setTokenType("Bearer");
            response.setExpiresIn((session.getExpiresAt().toEpochMilli() - System.currentTimeMillis()) / 1000);

            return response;

        } catch (ApplicationInvalidAuthCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationInvalidAuthCodeException("Invalid authorization code: " + e.getMessage(), 
                    params != null ? params.getCode() : "unknown");
        }
    }

    @Override
    public SharedOAuth2TokenResponseDTO refreshAccessToken(SharedOAuth2RefreshRequestParams params) {
        try {
            // Validate request parameters
            if (params.getRefreshToken() == null || params.getRefreshToken().trim().isEmpty()) {
                throw new ApplicationTokenExpiredException("Refresh token cannot be empty", "refresh_token");
            }

            // Validate refresh token first
            if (!domainTokenService.validateToken(params.getRefreshToken())) {
                throw new ApplicationTokenExpiredException("Refresh token is invalid or expired", "refresh_token");
            }

            // Refresh token through domain service
            var session = domainOAuth2AuthorizationService.refreshAccessToken(params.getRefreshToken());

            // Create token response with new tokens
            SharedOAuth2TokenResponseDTO response = new SharedOAuth2TokenResponseDTO();
            response.setAccessToken(session.getAccessTokenHash());
            response.setRefreshToken(session.getRefreshTokenHash());
            response.setTokenType("Bearer");
            response.setExpiresIn((session.getExpiresAt().toEpochMilli() - System.currentTimeMillis()) / 1000);

            return response;

        } catch (ApplicationTokenExpiredException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationTokenExpiredException("Failed to refresh token: " + e.getMessage(), "refresh_token");
        }
    }
}