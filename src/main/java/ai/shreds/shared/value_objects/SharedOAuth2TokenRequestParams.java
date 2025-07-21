package ai.shreds.shared.value_objects;

import jakarta.validation.constraints.NotBlank;

/**
 * Value object for OAuth 2.0 token request parameters.
 * Used for exchanging authorization codes for access tokens with PKCE support.
 */
public class SharedOAuth2TokenRequestParams {
    
    @NotBlank(message = "Grant type is required")
    private String grantType;
    
    @NotBlank(message = "Authorization code is required")
    private String code;
    
    @NotBlank(message = "Redirect URI is required")
    private String redirectUri;
    
    @NotBlank(message = "Client ID is required")
    private String clientId;
    
    @NotBlank(message = "Code verifier is required for PKCE")
    private String codeVerifier;

    public SharedOAuth2TokenRequestParams() {}

    public SharedOAuth2TokenRequestParams(
            String grantType,
            String code,
            String redirectUri,
            String clientId,
            String codeVerifier) {
        this.grantType = grantType;
        this.code = code;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.codeVerifier = codeVerifier;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public void setCodeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

    @Override
    public String toString() {
        return "SharedOAuth2TokenRequestParams{" +
                "grantType='" + grantType + '\'' +
                ", code='[PROTECTED]'" +
                ", redirectUri='" + redirectUri + '\'' +
                ", clientId='" + clientId + '\'' +
                ", codeVerifier='[PROTECTED]'" +
                '}';
    }
}