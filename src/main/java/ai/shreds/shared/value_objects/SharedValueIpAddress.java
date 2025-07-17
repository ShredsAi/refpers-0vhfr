package ai.shreds.shared.value_objects;

import java.util.Objects;

public class SharedValueIpAddress {
    private final String value;

    public SharedValueIpAddress(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("IP address cannot be null or empty");
        }
        this.value = value;
        validate();
    }

    public String getValue() {
        return value;
    }

    public void validate() {
        // Enhanced IPv4/IPv6 format validation
        if (!isValidIPv4(value) && !isValidIPv6(value)) {
            throw new IllegalArgumentException("Invalid IP address format: " + value);
        }
    }
    
    private boolean isValidIPv4(String ip) {
        if (!ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            return false;
        }
        
        String[] parts = ip.split("\\.");
        for (String part : parts) {
            int num = Integer.parseInt(part);
            if (num < 0 || num > 255) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isValidIPv6(String ip) {
        // Basic check for IPv6 format (simplified)
        return ip.contains(":") && ip.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::$|^([0-9a-fA-F]{1,4}:){0,6}::([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedValueIpAddress that = (SharedValueIpAddress) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
