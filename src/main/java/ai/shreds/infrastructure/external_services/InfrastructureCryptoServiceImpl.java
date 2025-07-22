package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.ports.DomainOutputPortCryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class InfrastructureCryptoServiceImpl implements DomainOutputPortCryptoService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    @Autowired
    public InfrastructureCryptoServiceImpl(BCryptPasswordEncoder passwordEncoder, SecureRandom secureRandom) {
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = secureRandom;
    }

    @Override
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean verifyPassword(String password, String hash) {
        return passwordEncoder.matches(password, hash);
    }

    @Override
    public String generateSecureToken() {
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    /**
     * Hashes a token using a deterministic algorithm (SHA-256) suitable for lookups.
     * This is different from password hashing, which must be non-deterministic (salted).
     * @param token The raw token to hash.
     * @return The SHA-256 hashed, Base64 URL-encoded string.
     */
    @Override
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // This should never happen in a standard Java environment
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
