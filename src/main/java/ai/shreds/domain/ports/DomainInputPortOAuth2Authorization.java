package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainAuthenticationSessionEntity;

/**
 * Inbound port for OAuth 2.0 authorization operations in the domain layer.
 */
public interface DomainInputPortOAuth2Authorization {

    /**
     * Generate an authorization code for the given account and client parameters.
     */
    String generateAuthorizationCode(String accountId, String clientId, String redirectUri, String codeChallenge);

    /**
     * Validate and exchange authorization code for tokens, verifying PKCE.
     */
    DomainAuthenticationSessionEntity validateAndExchangeCode(String code, String clientId, String codeVerifier);

    /**
     * Refresh access token using a valid refresh token.
     */
    DomainAuthenticationSessionEntity refreshAccessToken(String refreshToken);
}
