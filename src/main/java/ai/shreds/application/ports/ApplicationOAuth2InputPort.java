package ai.shreds.application.ports;

import ai.shreds.application.dtos.ApplicationAuthorizationCodeDTO;
import ai.shreds.shared.dtos.SharedOAuth2TokenResponseDTO;
import ai.shreds.shared.value_objects.SharedOAuth2AuthorizeRequestParams;
import ai.shreds.shared.value_objects.SharedOAuth2TokenRequestParams;
import ai.shreds.shared.value_objects.SharedOAuth2RefreshRequestParams;

/**
 * Input port for OAuth 2.0 authorization server operations.
 */
public interface ApplicationOAuth2InputPort {

    /**
     * Initiate OAuth 2.0 authorization flow with PKCE.
     * @param params authorization request parameters
     * @return authorization code DTO
     */
    ApplicationAuthorizationCodeDTO initiateAuthorizationFlow(SharedOAuth2AuthorizeRequestParams params);

    /**
     * Exchange authorization code for access and refresh tokens.
     * @param params token request parameters
     * @return token response with access and refresh tokens
     */
    SharedOAuth2TokenResponseDTO exchangeCodeForTokens(SharedOAuth2TokenRequestParams params);

    /**
     * Refresh access token using refresh token.
     * @param params refresh token request parameters
     * @return new token response with refreshed tokens
     */
    SharedOAuth2TokenResponseDTO refreshAccessToken(SharedOAuth2RefreshRequestParams params);
}