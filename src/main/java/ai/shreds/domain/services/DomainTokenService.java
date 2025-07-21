package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainAuthenticationSessionEntity;
import ai.shreds.domain.exceptions.DomainInvalidTokenException;
import ai.shreds.domain.ports.DomainInputPortTokenService;
import ai.shreds.domain.ports.DomainOutputPortCryptoService;
import ai.shreds.domain.ports.DomainOutputPortSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DomainTokenService implements DomainInputPortTokenService {

    private final DomainOutputPortCryptoService cryptoService;
    private final DomainOutputPortSessionRepository sessionRepository;

    // Temporary storage for token blacklist (in real implementation, use cache)
    private final Set<String> blacklistedTokens = new HashSet<>();
    
    // Temporary storage for tokens and their claims (in real implementation, use JWT library)
    private final Map<String, TokenData> tokenStorage = new HashMap<>();

    public DomainTokenService(
            DomainOutputPortCryptoService cryptoService,
            DomainOutputPortSessionRepository sessionRepository) {
        this.cryptoService = cryptoService;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public String generateAccessToken(String accountId, Map<String, Object> claims) {
        // Generate secure token
        String token = cryptoService.generateSecureToken();
        
        // Prepare token claims
        Map<String, Object> tokenClaims = new HashMap<>(claims);
        tokenClaims.put("sub", accountId); // Subject
        tokenClaims.put("iat", Instant.now().getEpochSecond()); // Issued at
        tokenClaims.put("exp", Instant.now().plus(1, ChronoUnit.HOURS).getEpochSecond()); // Expires at
        tokenClaims.put("jti", UUID.randomUUID().toString()); // JWT ID
        tokenClaims.put("token_type", "access_token");

        // Store token data (in real implementation, JWT would be self-contained)
        TokenData tokenData = new TokenData(
            token, 
            accountId, 
            tokenClaims, 
            Instant.now().plus(1, ChronoUnit.HOURS),
            "access_token"
        );
        tokenStorage.put(token, tokenData);

        return token;
    }

    @Override
    public String generateRefreshToken(String accountId) {
        // Generate secure token
        String token = cryptoService.generateSecureToken();
        
        // Prepare token claims
        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("sub", accountId); // Subject
        tokenClaims.put("iat", Instant.now().getEpochSecond()); // Issued at
        tokenClaims.put("exp", Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond()); // Expires at (30 days)
        tokenClaims.put("jti", UUID.randomUUID().toString()); // JWT ID
        tokenClaims.put("token_type", "refresh_token");

        // Store token data
        TokenData tokenData = new TokenData(
            token, 
            accountId, 
            tokenClaims, 
            Instant.now().plus(30, ChronoUnit.DAYS),
            "refresh_token"
        );
        tokenStorage.put(token, tokenData);

        return token;
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // Check if token is blacklisted
        if (blacklistedTokens.contains(token)) {
            return false;
        }

        // Get token data
        TokenData tokenData = tokenStorage.get(token);
        if (tokenData == null) {
            return false;
        }

        // Check if token is expired
        if (tokenData.isExpired()) {
            return false;
        }

        // For access tokens, also check if associated session is still valid
        if ("access_token".equals(tokenData.getType())) {
            String tokenHash = cryptoService.hashToken(token);
            DomainAuthenticationSessionEntity session = sessionRepository.findByAccessTokenHash(tokenHash);
            return session != null && session.isValid();
        }

        return true;
    }

    @Override
    public Map<String, Object> extractClaims(String token) {
        if (!validateToken(token)) {
            throw new DomainInvalidTokenException("Invalid or expired token");
        }

        TokenData tokenData = tokenStorage.get(token);
        if (tokenData == null) {
            throw new DomainInvalidTokenException("Token not found");
        }

        return new HashMap<>(tokenData.getClaims());
    }

    @Override
    public void revokeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        // Add token to blacklist
        blacklistedTokens.add(token);

        // Remove from storage
        tokenStorage.remove(token);

        // If it's an access token, revoke the associated session
        String tokenHash = cryptoService.hashToken(token);
        DomainAuthenticationSessionEntity session = sessionRepository.findByAccessTokenHash(tokenHash);
        if (session != null) {
            sessionRepository.revokeSession(session.getSessionId().toString());
        }
    }

    // Inner class to hold token data
    private static class TokenData {
        private final String token;
        private final String accountId;
        private final Map<String, Object> claims;
        private final Instant expiresAt;
        private final String type;

        public TokenData(String token, String accountId, Map<String, Object> claims, 
                        Instant expiresAt, String type) {
            this.token = token;
            this.accountId = accountId;
            this.claims = claims;
            this.expiresAt = expiresAt;
            this.type = type;
        }

        public boolean isExpired() {
            return expiresAt.isBefore(Instant.now());
        }

        public String getAccountId() { return accountId; }
        public Map<String, Object> getClaims() { return claims; }
        public String getType() { return type; }
    }
}
