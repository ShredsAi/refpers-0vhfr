package ai.shreds.application.services;

import ai.shreds.application.dtos.ApplicationAuthorizationCodeDTO;
import ai.shreds.application.exceptions.ApplicationInvalidAuthCodeException;
import ai.shreds.application.exceptions.ApplicationTokenExpiredException;
import ai.shreds.application.ports.ApplicationCacheOutputPort;
import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import ai.shreds.application.ports.ApplicationOAuth2InputPort;
import ai.shreds.domain.ports.DomainInputPortOAuth2Authorization;
import ai.shreds.domain.ports.DomainInputPortTokenService;
import ai.shreds.domain.value_objects.DomainTokenResultValue;
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

    private static final String TEST_ACCOUNT_ID = "550e8400-e29b-41d4-a716-446655440001";

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
            if (params.getClientId() == null || params.getClientId().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Client ID cannot be empty", params.getClientId());
            }
            if (params.getRedirectUri() == null || params.getRedirectUri().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Redirect URI cannot be empty", params.getClientId());
            }
            if (params.getCodeChallenge() == null || params.getCodeChallenge().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Code challenge cannot be empty", params.getClientId());
            }

            String accountId = TEST_ACCOUNT_ID;

            String authCode = domainOAuth2AuthorizationService.generateAuthorizationCode(
                    accountId,
                    params.getClientId(),
                    params.getRedirectUri(),
                    params.getCodeChallenge()
            );

            long expiresAt = System.currentTimeMillis() + (10 * 60 * 1000);
            ApplicationAuthorizationCodeDTO authCodeDTO = new ApplicationAuthorizationCodeDTO(
                    authCode,
                    params.getClientId(),
                    params.getRedirectUri(),
                    params.getCodeChallenge(),
                    accountId,
                    expiresAt
            );

            cachePort.put("auth_code:" + authCode, authCodeDTO, 10 * 60 * 1000L);
            
            return authCodeDTO;

        } catch (Exception e) {
            throw new ApplicationInvalidAuthCodeException("Failed to initiate authorization flow: " + e.getMessage(), 
                    params != null ? params.getClientId() : "unknown", e);
        }
    }

    @Override
    public SharedOAuth2TokenResponseDTO exchangeCodeForTokens(SharedOAuth2TokenRequestParams params) {
        try {
            if (params.getCode() == null || params.getCode().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Authorization code cannot be empty", params.getCode());
            }
            if (params.getClientId() == null || params.getClientId().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Client ID cannot be empty", params.getCode());
            }
            if (params.getCodeVerifier() == null || params.getCodeVerifier().trim().isEmpty()) {
                throw new ApplicationInvalidAuthCodeException("Code verifier cannot be empty", params.getCode());
            }

            DomainTokenResultValue tokenResult = domainOAuth2AuthorizationService.validateAndExchangeCode(
                    params.getCode(),
                    params.getClientId(),
                    params.getCodeVerifier()
            );

            cachePort.delete("auth_code:" + params.getCode());

            SharedOAuth2TokenResponseDTO response = new SharedOAuth2TokenResponseDTO();
            response.setAccessToken(tokenResult.getRawAccessToken());
            response.setRefreshToken(tokenResult.getRawRefreshToken());
            response.setTokenType("Bearer");
            response.setExpiresIn((tokenResult.getSession().getExpiresAt().toEpochMilli() - System.currentTimeMillis()) / 1000);

            return response;

        } catch (Exception e) {
            throw new ApplicationInvalidAuthCodeException("Invalid authorization code: " + e.getMessage(), 
                    params != null ? params.getCode() : "unknown", e);
        }
    }

    @Override
    public SharedOAuth2TokenResponseDTO refreshAccessToken(SharedOAuth2RefreshRequestParams params) {
        try {
            if (params.getRefreshToken() == null || params.getRefreshToken().trim().isEmpty()) {
                throw new ApplicationTokenExpiredException("Refresh token cannot be empty", "refresh_token");
            }

            if (!domainTokenService.validateToken(params.getRefreshToken())) {
                throw new ApplicationTokenExpiredException("Refresh token is invalid or expired", "refresh_token");
            }

            DomainTokenResultValue tokenResult = domainOAuth2AuthorizationService.refreshAccessToken(params.getRefreshToken());

            SharedOAuth2TokenResponseDTO response = new SharedOAuth2TokenResponseDTO();
            response.setAccessToken(tokenResult.getRawAccessToken());
            response.setRefreshToken(tokenResult.getRawRefreshToken());
            response.setTokenType("Bearer");
            response.setExpiresIn((tokenResult.getSession().getExpiresAt().toEpochMilli() - System.currentTimeMillis()) / 1000);

            return response;

        } catch (Exception e) {
            throw new ApplicationTokenExpiredException("Failed to refresh token: " + e.getMessage(), "refresh_token", e);
        }
    }
}