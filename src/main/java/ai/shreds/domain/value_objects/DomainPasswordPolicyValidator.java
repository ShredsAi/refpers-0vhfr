package ai.shreds.domain.value_objects;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class DomainPasswordPolicyValidator {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':,.<>?/~`|\\\\]");

    public boolean validatePasswordComplexity(String password) {
        if (password == null) {
            return false;
        }

        // Check length
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }

        // Check for at least one lowercase letter
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            return false;
        }

        // Check for at least one uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            return false;
        }

        // Check for at least one digit
        if (!DIGIT_PATTERN.matcher(password).find()) {
            return false;
        }

        // Check for at least one special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            return false;
        }

        // Check for common weak patterns
        String lowerPassword = password.toLowerCase();
        String[] commonPatterns = {"password", "123456", "qwerty", "admin", "user"};
        for (String pattern : commonPatterns) {
            if (lowerPassword.contains(pattern)) {
                return false;
            }
        }

        return true;
    }

    public String getPasswordRequirements() {
        return "Password must be " + MIN_PASSWORD_LENGTH + "-" + MAX_PASSWORD_LENGTH + " characters long and contain at least: " +
               "one lowercase letter, one uppercase letter, one digit, and one special character. " +
               "Common weak patterns are not allowed.";
    }
}
