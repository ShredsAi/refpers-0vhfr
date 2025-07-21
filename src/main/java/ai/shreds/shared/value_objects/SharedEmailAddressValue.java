package ai.shreds.shared.value_objects;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Value object representing a validated email address.
 * Ensures email format validation and immutability.
 */
public class SharedEmailAddressValue {
    
    @NotBlank(message = "Email address cannot be blank")
    @Email(message = "Email address must be valid")
    private final String email;

    public SharedEmailAddressValue(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be null or empty");
        }
        this.email = email.toLowerCase().trim();
        validate();
    }

    public void validate() {
        // Additional custom validation can be added here
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        
        // Check for basic email structure
        String[] parts = email.split("@");
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        
        // Check domain has at least one dot
        String domain = parts[1];
        if (!domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
            throw new IllegalArgumentException("Invalid email domain: " + domain);
        }
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return email;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SharedEmailAddressValue that = (SharedEmailAddressValue) obj;
        return email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }
}
