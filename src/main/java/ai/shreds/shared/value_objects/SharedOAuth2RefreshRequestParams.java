package ai.shreds.shared.value_objects;

import jakarta.validation.constraints.NotBlank;

/**
 * Value object for OAuth 2.0 refresh token request parameters.
 * Used for refreshing access tokens using valid refresh tokens.
 */
public class SharedOAuth2RefreshRequestParams {
    
    @NotBlank(message = "Grant type is required")
    private String grantType;
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public SharedOAuth2RefreshRequestParams() {}

    public SharedOAuth2RefreshRequestParams(String grantType, String refreshToken) {
        this.grantType = grantType;
        this.refreshToken = refreshToken;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "SharedOAuth2RefreshRequestParams{" +
                "grantType='" + grantType + '\'' +
                ", refreshToken='[PROTECTED]'" +
                '}';
    }
}