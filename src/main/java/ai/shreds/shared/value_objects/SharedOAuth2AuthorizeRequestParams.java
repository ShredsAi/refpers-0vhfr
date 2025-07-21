package ai.shreds.shared.value_objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Value object for OAuth 2.0 authorization request parameters with PKCE support.
 * Used for initiating the OAuth 2.0 Authorization Code Flow.
 */
public class SharedOAuth2AuthorizeRequestParams {
    
    @NotBlank(message = "Client ID is required")
    private String clientId;
    
    @NotBlank(message = "Redirect URI is required")
    private String redirectUri;
    
    @NotBlank(message = "Response type is required")
    private String responseType;
    
    private String scope;
    
    private String state;
    
    @NotBlank(message = "Code challenge is required for PKCE")
    private String codeChallenge;
    
    @NotBlank(message = "Code challenge method is required for PKCE")
    private String codeChallengeMethod;

    public SharedOAuth2AuthorizeRequestParams() {}

    public SharedOAuth2AuthorizeRequestParams(
            String clientId,
            String redirectUri,
            String responseType,
            String scope,
            String state,
            String codeChallenge,
            String codeChallengeMethod) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.responseType = responseType;
        this.scope = scope;
        this.state = state;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

    @Override
    public String toString() {
        return "SharedOAuth2AuthorizeRequestParams{" +
                "clientId='" + clientId + '\'' +
                ", redirectUri='" + redirectUri + '\'' +
                ", responseType='" + responseType + '\'' +
                ", scope='" + scope + '\'' +
                ", state='" + state + '\'' +
                ", codeChallenge='[PROTECTED]'" +
                ", codeChallengeMethod='" + codeChallengeMethod + '\'' +
                '}';
    }
}