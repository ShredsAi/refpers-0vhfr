package ai.shreds.domain.value_objects;

import ai.shreds.shared.value_objects.SharedEmailAddressValue;

import java.util.Objects;

public class DomainCredentialsValue {

    private final String username;
    private final String passwordHash;
    private final SharedEmailAddressValue email;

    public DomainCredentialsValue(String username, String passwordHash, SharedEmailAddressValue email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        validate();
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public SharedEmailAddressValue getEmail() {
        return email;
    }

    public void validate() {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        email.validate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainCredentialsValue that = (DomainCredentialsValue) o;
        return Objects.equals(username, that.username) &&
               Objects.equals(passwordHash, that.passwordHash) &&
               Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, passwordHash, email);
    }

    @Override
    public String toString() {
        return "DomainCredentialsValue{" +
                "username='" + username + '\'' +
                ", email=" + email +
                '}';
    }
}
