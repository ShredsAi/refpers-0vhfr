package ai.shreds.application.dtos;

import ai.shreds.shared.dtos.SharedOAuth2TokenResponseDTO;

/**
 * DTO representing an authentication session with tokens.
 */
public class ApplicationSessionDTO {
    private String sessionId;
    private String accountId;
    private String accessToken;
    private String refreshToken;
    private long expiresAt;

    public ApplicationSessionDTO() {}

    public ApplicationSessionDTO(String sessionId, String accountId, String accessToken, String refreshToken, long expiresAt) {
        this.sessionId = sessionId;
        this.accountId = accountId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Convert to OAuth2 token response format.
     * @return SharedOAuth2TokenResponseDTO
     */
    public SharedOAuth2TokenResponseDTO toTokenResponse() {
        SharedOAuth2TokenResponseDTO response = new SharedOAuth2TokenResponseDTO();
        response.setAccessToken(this.accessToken);
        response.setRefreshToken(this.refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn((this.expiresAt - System.currentTimeMillis()) / 1000);
        return response;
    }

    @Override
    public String toString() {
        return "ApplicationSessionDTO{" +
                "sessionId='" + sessionId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", accessToken='[PROTECTED]'" +
                ", refreshToken='[PROTECTED]'" +
                ", expiresAt=" + expiresAt +
                '}';
    }
}