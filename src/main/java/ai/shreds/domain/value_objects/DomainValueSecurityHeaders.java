package ai.shreds.domain.value_objects;

import ai.shreds.shared.dtos.SharedSecurityHeadersDTO;

/**
 * Value object representing security headers for downstream service propagation.
 */
public class DomainValueSecurityHeaders {
    private final String userId;
    private final String userRoles;
    private static final int MAX_HEADER_LENGTH = 1024;

    public DomainValueSecurityHeaders(String userId, String userRoles) {
        this.userId = userId;
        this.userRoles = userRoles;
        validate();
    }

    private void validate() {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId in security headers cannot be null or empty");
        }
        if (userRoles == null) {
            throw new IllegalArgumentException("UserRoles in security headers cannot be null");
        }
        if (userId.length() > MAX_HEADER_LENGTH || userRoles.length() > MAX_HEADER_LENGTH) {
            throw new IllegalArgumentException("Security header value exceeds maximum length of " + MAX_HEADER_LENGTH + " characters");
        }
        if (!userId.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("UserId contains invalid characters");
        }
        if (!userRoles.matches("^[a-zA-Z0-9_,.-]+$")) {
            throw new IllegalArgumentException("UserRoles contains invalid characters");
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getUserRoles() {
        return userRoles;
    }

    /**
     * Convert to shared DTO for adapter and application layers.
     */
    public SharedSecurityHeadersDTO toDTO() {
        SharedSecurityHeadersDTO dto = new SharedSecurityHeadersDTO();
        dto.setUserId(userId);
        dto.setUserRoles(userRoles);
        return dto;
    }

    /**
     * Create a domain value object from shared DTO.
     */
    public static DomainValueSecurityHeaders fromDTO(SharedSecurityHeadersDTO dto) {
        return new DomainValueSecurityHeaders(
            dto.getUserId(),
            dto.getUserRoles()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainValueSecurityHeaders that = (DomainValueSecurityHeaders) o;
        return userId.equals(that.userId) && userRoles.equals(that.userRoles);
    }

    @Override
    public int hashCode() {
        return 31 * userId.hashCode() + userRoles.hashCode();
    }

    @Override
    public String toString() {
        return String.format("SecurityHeaders{userId='%s', userRoles='%s'}", userId, userRoles);
    }
}