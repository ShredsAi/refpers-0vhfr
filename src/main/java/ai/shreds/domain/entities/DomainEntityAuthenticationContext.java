package ai.shreds.domain.entities;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import ai.shreds.shared.dtos.SharedAuthenticationContextDTO;
import ai.shreds.shared.enums.SharedEnumUserRole;
import ai.shreds.shared.value_objects.SharedValueUserId;
import ai.shreds.shared.value_objects.SharedValueSessionId;
import ai.shreds.domain.value_objects.DomainValueTokenClaims;

/**
 * Entity representing an authentication context created after token validation.
 * Contains user identity, roles, and session information.
 */
public class DomainEntityAuthenticationContext {
    private final SharedValueUserId userId;
    private final Set<SharedEnumUserRole> userRoles;
    private final DomainValueTokenClaims tokenClaims;
    private final SharedValueSessionId sessionId;
    private final boolean isAuthenticated;
    private final Instant expiresAt;

    public DomainEntityAuthenticationContext(
            SharedValueUserId userId,
            Set<SharedEnumUserRole> userRoles,
            DomainValueTokenClaims tokenClaims,
            SharedValueSessionId sessionId,
            boolean isAuthenticated,
            Instant expiresAt) {
        this.userId = userId;
        this.userRoles = new HashSet<>(userRoles != null ? userRoles : Collections.emptySet());
        this.tokenClaims = tokenClaims;
        this.sessionId = sessionId;
        this.isAuthenticated = isAuthenticated;
        this.expiresAt = expiresAt;
        validate();
    }

    private void validate() {
        if (isAuthenticated) {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null for authenticated context");
            }
            if (userRoles.isEmpty()) {
                throw new IllegalArgumentException("User roles cannot be empty for authenticated context");
            }
            if (tokenClaims == null) {
                throw new IllegalArgumentException("Token claims cannot be null for authenticated context");
            }
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("Expiration time cannot be null");
        }
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Expiration time must be in the future");
        }
    }

    public SharedValueUserId getUserId() {
        return userId;
    }

    public Set<SharedEnumUserRole> getUserRoles() {
        return Collections.unmodifiableSet(userRoles);
    }

    public DomainValueTokenClaims getTokenClaims() {
        return tokenClaims;
    }

    public SharedValueSessionId getSessionId() {
        return sessionId;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    /**
     * Checks if the context has a specific role.
     *
     * @param role the role to check
     * @return true if the user has the role
     */
    public boolean hasRole(SharedEnumUserRole role) {
        return userRoles.contains(role);
    }

    /**
     * Checks if the context has expired.
     *
     * @return true if the context has expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Converts this domain entity to a shared DTO.
     */
    public SharedAuthenticationContextDTO toDTO() {
        SharedAuthenticationContextDTO dto = new SharedAuthenticationContextDTO();
        dto.setUserId(userId);
        dto.setUserRoles(userRoles);
        dto.setTokenClaims(tokenClaims.toDTO());
        dto.setSessionId(sessionId);
        dto.setAuthenticated(isAuthenticated);
        dto.setExpiresAt(expiresAt);
        return dto;
    }

    @Override
    public String toString() {
        return String.format("AuthenticationContext{userId=%s, roles=%s, sessionId=%s, authenticated=%s, expires=%s}",
            userId, userRoles, sessionId, isAuthenticated, expiresAt);
    }
}