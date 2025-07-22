package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainAccountEntity;
import ai.shreds.domain.entities.DomainAuthenticationSessionEntity;
import ai.shreds.domain.exceptions.DomainInvalidTokenException;
import ai.shreds.domain.ports.*;
import ai.shreds.domain.value_objects.DomainPkceChallengeValidator;
import ai.shreds.domain.value_objects.DomainTokenResultValue;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DomainOAuth2AuthorizationService implements DomainInputPortOAuth2Authorization {

    private final DomainOutputPortSessionRepository sessionRepository;
    private final DomainOutputPortAccountRepository accountRepository;
    private final DomainInputPortTokenService tokenService;
    private final DomainPkceChallengeValidator pkceChallengeValidator;
    private final DomainOutputPortCryptoService cryptoService;

    private final Map<String, AuthorizationCodeData> authorizationCodes = new HashMap<>();

    public DomainOAuth2AuthorizationService(
            DomainOutputPortSessionRepository sessionRepository,
            DomainOutputPortAccountRepository accountRepository,
            DomainInputPortTokenService tokenService,
            DomainPkceChallengeValidator pkceChallengeValidator,
            DomainOutputPortCryptoService cryptoService) {
        this.sessionRepository = sessionRepository;
        this.accountRepository = accountRepository;
        this.tokenService = tokenService;
        this.pkceChallengeValidator = pkceChallengeValidator;
        this.cryptoService = cryptoService;
    }

    @Override
    public String generateAuthorizationCode(String accountId, String clientId, String redirectUri, String codeChallenge) {
        DomainAccountEntity account = accountRepository.findById(accountId);
        if (account == null || !account.isActive()) {
            throw new DomainInvalidTokenException("Invalid or inactive account");
        }

        String authorizationCode = cryptoService.generateSecureToken();
        Instant expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES);

        AuthorizationCodeData codeData = new AuthorizationCodeData(
                authorizationCode, accountId, clientId, redirectUri, codeChallenge, expiresAt
        );
        authorizationCodes.put(authorizationCode, codeData);

        return authorizationCode;
    }

    @Override
    public DomainTokenResultValue validateAndExchangeCode(String code, String clientId, String codeVerifier) {
        AuthorizationCodeData codeData = authorizationCodes.get(code);
        if (codeData == null || codeData.isExpired() || !codeData.getClientId().equals(clientId)) {
            throw new DomainInvalidTokenException("Invalid or expired authorization code");
        }

        if (!pkceChallengeValidator.validateCodeChallenge(codeData.getCodeChallenge(), codeVerifier, "S256")) {
            throw new DomainInvalidTokenException("Invalid PKCE code verifier");
        }

        authorizationCodes.remove(code);

        Map<String, Object> claims = new HashMap<>();
        claims.put("client_id", clientId);
        claims.put("scope", "read write");

        String accessToken = tokenService.generateAccessToken(codeData.getAccountId(), claims);
        String refreshToken = tokenService.generateRefreshToken(codeData.getAccountId());

        DomainAuthenticationSessionEntity session = new DomainAuthenticationSessionEntity(
                UUID.randomUUID(),
                UUID.fromString(codeData.getAccountId()),
                cryptoService.hashToken(accessToken),
                cryptoService.hashToken(refreshToken),
                Instant.now().plus(1, ChronoUnit.HOURS),
                Instant.now(),
                false
        );

        DomainAuthenticationSessionEntity savedSession = sessionRepository.save(session);

        return new DomainTokenResultValue(savedSession, accessToken, refreshToken);
    }

    @Override
    public DomainTokenResultValue refreshAccessToken(String refreshToken) {
        String refreshTokenHash = cryptoService.hashToken(refreshToken);
        
        DomainAuthenticationSessionEntity session = sessionRepository.findByRefreshTokenHash(refreshTokenHash);
        if (session == null || !session.isValid()) {
            throw new DomainInvalidTokenException("Invalid or expired refresh token");
        }

        session.revoke();
        sessionRepository.save(session);

        Map<String, Object> claims = new HashMap<>();
        claims.put("refreshed", true);
        
        String newAccessToken = tokenService.generateAccessToken(session.getAccountId().toString(), claims);
        String newRefreshToken = tokenService.generateRefreshToken(session.getAccountId().toString());

        DomainAuthenticationSessionEntity newSession = new DomainAuthenticationSessionEntity(
                UUID.randomUUID(),
                session.getAccountId(),
                cryptoService.hashToken(newAccessToken),
                cryptoService.hashToken(newRefreshToken),
                Instant.now().plus(1, ChronoUnit.HOURS),
                Instant.now(),
                false
        );

        DomainAuthenticationSessionEntity savedNewSession = sessionRepository.save(newSession);

        return new DomainTokenResultValue(savedNewSession, newAccessToken, newRefreshToken);
    }

    private static class AuthorizationCodeData {
        private final String code;
        private final String accountId;
        private final String clientId;
        private final String redirectUri;
        private final String codeChallenge;
        private final Instant expiresAt;

        public AuthorizationCodeData(String code, String accountId, String clientId, 
                                   String redirectUri, String codeChallenge, Instant expiresAt) {
            this.code = code;
            this.accountId = accountId;
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.codeChallenge = codeChallenge;
            this.expiresAt = expiresAt;
        }

        public boolean isExpired() {
            return expiresAt.isBefore(Instant.now());
        }

        public String getAccountId() { return accountId; }
        public String getClientId() { return clientId; }
        public String getCodeChallenge() { return codeChallenge; }
    }
}