package ai.shreds.shared.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public SharedJwtUtilService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration:3600000}") Long jwtExpiration) {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    /**
     * Generate a JWT access token with custom claims
     * @param accountId The account identifier
     * @param claims Additional claims to include
     * @return Signed JWT token string
     */
    public String generateToken(String accountId, Map<String, Object> claims) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plus(jwtExpiration, ChronoUnit.MILLIS);
            
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(accountId)
                    .issuer("authentication-shred")
                    .audience("api")
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiration))
                    .notBeforeTime(Date.from(now));

            // Add custom claims
            if (claims != null) {
                claims.forEach(claimsBuilder::claim);
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            
            JWSSigner signer = new MACSigner(jwtSecret.getBytes());
            signedJWT.sign(signer);
            
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Validate a JWT token's signature and expiration
     * @param token JWT token string
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());
            
            // Verify signature
            if (!signedJWT.verify(verifier)) {
                return false;
            }
            
            // Check expiration
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime != null && expirationTime.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract all claims from a JWT token
     * @param token JWT token string
     * @return Map of claims
     */
    public Map<String, Object> extractClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());
            
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Invalid JWT signature");
            }
            
            return signedJWT.getJWTClaimsSet().getClaims();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract JWT claims", e);
        }
    }

    /**
     * Extract account ID (subject) from JWT token
     * @param token JWT token string
     * @return Account ID
     */
    public String extractAccountId(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract account ID from JWT", e);
        }
    }

    /**
     * Check if JWT token is expired
     * @param token JWT token string
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime == null || expirationTime.before(new Date());
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }

    /**
     * Extract JWT ID (jti) from token for blacklist operations
     * @param token JWT token string
     * @return JWT ID
     */
    public String extractJwtId(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getJWTID();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract JWT ID", e);
        }
    }

    /**
     * Get remaining time until token expiration in seconds
     * @param token JWT token string
     * @return Remaining time in seconds, 0 if expired
     */
    public long getRemainingTimeInSeconds(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            
            if (expirationTime == null) {
                return 0;
            }
            
            long remaining = (expirationTime.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }
}