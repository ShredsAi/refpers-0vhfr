package ai.shreds.shared.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Shared JWT utility service for token operations across the authentication system.
 * Handles JWT creation, validation, and claims extraction using nimbus-jose-jwt library.
 */
@Service
public class SharedJwtUtilService {

    private final String jwtSecret;
    private final Long jwtExpiration;
    private final Long refreshTokenExpiration;
    private final JWSSigner signer;
    private final JWSVerifier verifier;

    public SharedJwtUtilService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration:3600000}") Long jwtExpiration,
            @Value("${refresh.token.expiration:86400000}") Long refreshTokenExpiration) throws JOSEException {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.signer = new MACSigner(jwtSecret.getBytes());
        this.verifier = new MACVerifier(jwtSecret.getBytes());
    }

    public String generateAccessToken(String accountId, Map<String, Object> claims) {
        return generateToken(accountId, claims, jwtExpiration);
    }

    public String generateRefreshToken(String accountId, Map<String, Object> claims) {
        return generateToken(accountId, claims, refreshTokenExpiration);
    }

    private String generateToken(String accountId, Map<String, Object> claims, Long expirationMillis) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plus(expirationMillis, ChronoUnit.MILLIS);

            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(accountId)
                    .issuer("authentication-shred")
                    .audience("api")
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiration))
                    .notBeforeTime(Date.from(now));

            if (claims != null) {
                claims.forEach(claimsBuilder::claim);
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(verifier)) {
                return false;
            }
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime != null && expirationTime.after(new Date());
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    public Map<String, Object> extractClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Invalid JWT signature");
            }
            return signedJWT.getJWTClaimsSet().getClaims();
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException("Failed to extract JWT claims", e);
        }
    }

    public String extractAccountId(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Failed to extract account ID from JWT", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expirationTime = SignedJWT.parse(token).getJWTClaimsSet().getExpirationTime();
            return expirationTime == null || expirationTime.before(new Date());
        } catch (ParseException e) {
            return true; // Consider invalid tokens as expired
        }
    }
}