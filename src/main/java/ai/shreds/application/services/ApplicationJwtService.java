package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationCacheOutputPort;
import ai.shreds.domain.ports.DomainInputPortTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Application service for JWT token management.
 */
@Service
public class ApplicationJwtService {

    private final DomainInputPortTokenService domainTokenService;
    private final ApplicationCacheOutputPort cachePort;

    @Autowired
    public ApplicationJwtService(
            DomainInputPortTokenService domainTokenService,
            ApplicationCacheOutputPort cachePort) {
        this.domainTokenService = domainTokenService;
        this.cachePort = cachePort;
    }

    /**
     * Generate JWT access token for the given account with claims.
     * @param accountId the account identifier
     * @param claims additional claims to include in the token
     * @return JWT access token string
     */
    public String generateAccessToken(String accountId, Map<String, Object> claims) {
        try {
            return domainTokenService.generateAccessToken(accountId, claims);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate access token: " + e.getMessage(), e);
        }
    }

    /**
     * Generate JWT refresh token for the given account.
     * @param accountId the account identifier
     * @return JWT refresh token string
     */
    public String generateRefreshToken(String accountId) {
        try {
            return domainTokenService.generateRefreshToken(accountId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate refresh token: " + e.getMessage(), e);
        }
    }

    /**
     * Validate JWT token (access or refresh).
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Check if token is blacklisted first
            if (cachePort.exists("jwt:blacklist:" + token)) {
                return false;
            }
            
            return domainTokenService.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Revoke a JWT token by adding it to the blacklist.
     * @param token the JWT token to revoke
     */
    public void revokeToken(String token) {
        try {
            // Extract claims to get expiration time for blacklist TTL
            Map<String, Object> claims = domainTokenService.extractClaims(token);
            Long exp = (Long) claims.get("exp");
            
            if (exp != null) {
                long currentTime = System.currentTimeMillis() / 1000;
                long ttl = (exp - currentTime) * 1000; // Convert to milliseconds
                
                if (ttl > 0) {
                    // Add token to blacklist with TTL matching token expiration
                    cachePort.put("jwt:blacklist:" + token, "revoked", ttl);
                }
            }
            
            // Also revoke through domain service
            domainTokenService.revokeToken(token);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to revoke token: " + e.getMessage(), e);
        }
    }

    /**
     * Extract account ID from JWT token.
     * @param token the JWT token
     * @return account ID
     */
    public String extractAccountId(String token) {
        try {
            Map<String, Object> claims = domainTokenService.extractClaims(token);
            Object accountId = claims.get("account_id");
            if (accountId == null) {
                accountId = claims.get("sub"); // Fallback to 'sub' claim
            }
            return accountId != null ? accountId.toString() : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract account ID from token: " + e.getMessage(), e);
        }
    }

    /**
     * Extract all claims from JWT token.
     * @param token the JWT token
     * @return map of claims
     */
    public Map<String, Object> extractClaims(String token) {
        try {
            return domainTokenService.extractClaims(token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract claims from token: " + e.getMessage(), e);
        }
    }

    /**
     * Check if JWT token is expired.
     * @param token the JWT token
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Map<String, Object> claims = domainTokenService.extractClaims(token);
            Long exp = (Long) claims.get("exp");
            if (exp == null) {
                return true;
            }
            long currentTime = System.currentTimeMillis() / 1000;
            return exp < currentTime;
        } catch (Exception e) {
            return true;
        }
    }
}