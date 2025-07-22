package ai.shreds.domain.value_objects;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class DomainPkceChallengeValidator {

    private static final String PKCE_METHOD_PLAIN = "plain";
    private static final String PKCE_METHOD_S256 = "S256";
    private static final int MIN_VERIFIER_LENGTH = 43;
    private static final int MAX_VERIFIER_LENGTH = 128;

    public boolean validateCodeChallenge(String codeChallenge, String codeVerifier, String method) {
        if (codeChallenge == null || codeVerifier == null || method == null) {
            return false;
        }

        if (codeVerifier.length() < MIN_VERIFIER_LENGTH || codeVerifier.length() > MAX_VERIFIER_LENGTH) {
            return false;
        }

        // Validate code verifier contains only allowed characters (A-Z, a-z, 0-9, -, ., _, ~)
        if (!codeVerifier.matches("^[A-Za-z0-9\\-\\._~]+$")) {
            return false;
        }

        String expectedChallenge;
        switch (method.toUpperCase()) {
            case PKCE_METHOD_PLAIN:
                expectedChallenge = codeVerifier;
                break;
            case PKCE_METHOD_S256:
                expectedChallenge = generateCodeChallenge(codeVerifier, PKCE_METHOD_S256);
                break;
            default:
                return false;
        }

        return codeChallenge.equals(expectedChallenge);
    }

    public String generateCodeChallenge(String codeVerifier, String method) {
        if (codeVerifier == null || method == null) {
            throw new IllegalArgumentException("Code verifier and method cannot be null");
        }

        switch (method.toUpperCase()) {
            case PKCE_METHOD_PLAIN:
                return codeVerifier;
            case PKCE_METHOD_S256:
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
                    return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("SHA-256 algorithm not available", e);
                }
            default:
                throw new IllegalArgumentException("Unsupported PKCE method: " + method);
        }
    }
}
