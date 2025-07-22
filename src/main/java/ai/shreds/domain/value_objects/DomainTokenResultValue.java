package ai.shreds.domain.value_objects;

import ai.shreds.domain.entities.DomainAuthenticationSessionEntity;

public class DomainTokenResultValue {

    private final DomainAuthenticationSessionEntity session;
    private final String rawAccessToken;
    private final String rawRefreshToken;

    public DomainTokenResultValue(DomainAuthenticationSessionEntity session, String rawAccessToken, String rawRefreshToken) {
        this.session = session;
        this.rawAccessToken = rawAccessToken;
        this.rawRefreshToken = rawRefreshToken;
    }

    public DomainAuthenticationSessionEntity getSession() {
        return session;
    }

    public String getRawAccessToken() {
        return rawAccessToken;
    }

    public String getRawRefreshToken() {
        return rawRefreshToken;
    }
}