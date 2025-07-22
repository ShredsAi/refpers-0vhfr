package ai.shreds;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Simple helper to generate BCrypt hash for testing
 */
public class BCryptTestHelper {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "password";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        
        // Test verification
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + matches);
        
        // Test with the existing hash
        String existingHash = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewreZNtY7BgQr9T6";
        boolean matchesExisting = encoder.matches(password, existingHash);
        System.out.println("Matches existing hash: " + matchesExisting);
        
        // Try different common passwords
        String[] commonPasswords = {"password", "test", "admin", "123456", "testuser2", "testpassword"};
        for (String pwd : commonPasswords) {
            boolean match = encoder.matches(pwd, existingHash);
            if (match) {
                System.out.println("Found matching password: " + pwd);
            }
        }
    }
}
