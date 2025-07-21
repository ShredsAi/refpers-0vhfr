package ai.shreds.application.dtos;

public class ApplicationAuthorizationCodeDTO {
    private String code;
    private String clientId;
    private String redirectUri;
    private String codeChallenge;
    private String accountId;
    private long expiresAt;

    public ApplicationAuthorizationCodeDTO() {}

    public ApplicationAuthorizationCodeDTO(String code, String clientId, String redirectUri, String codeChallenge, String accountId, long expiresAt) {
        this.code = code;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.codeChallenge = codeChallenge;
        this.accountId = accountId;
        this.expiresAt = expiresAt;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}