package ai.shreds.domain.ports;

/**
 * Outbound port for cryptographic operations in the domain layer.
 */
public interface DomainOutputPortCryptoService {

    /**
     * Hashes the given password using a secure algorithm (e.g., BCrypt).
     *
     * @param password the raw password
     * @return the hashed password
     */
    String hashPassword(String password);

    /**
     * Verifies the provided password against the stored hash.
     *
     * @param password the raw password
     * @param hash the stored hash to verify against
     * @return true if the password matches the hash, false otherwise
     */
    boolean verifyPassword(String password, String hash);

    /**
     * Generates a cryptographically secure random token.
     *
     * @return a secure random token string
     */
    String generateSecureToken();

    /**
     * Hashes a token for storage and comparison (e.g., SHA-256).
     *
     * @param token the raw token
     * @return the hashed token
     */
    String hashToken(String token);
}
