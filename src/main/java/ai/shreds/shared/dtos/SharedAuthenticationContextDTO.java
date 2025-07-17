package ai.shreds.shared.dtos;

import ai.shreds.shared.value_objects.SharedValueUserId;
import ai.shreds.shared.value_objects.SharedValueSessionId;
import ai.shreds.shared.enums.SharedEnumUserRole;
import ai.shreds.domain.entities.DomainEntityAuthenticationContext;
import ai.shreds.domain.value_objects.DomainValueTokenClaims;

import java.time.Instant;
import java.util.Set;

public class SharedAuthenticationContextDTO {
    private SharedValueUserId userId;
    private Set<SharedEnumUserRole> userRoles;
    private SharedTokenClaimsDTO tokenClaims;
    private SharedValueSessionId sessionId;
    private boolean isAuthenticated;
    private Instant expiresAt;

    public SharedAuthenticationContextDTO() {
    }
    
    public SharedAuthenticationContextDTO(SharedValueUserId userId, Set<SharedEnumUserRole> userRoles,
                                         SharedTokenClaimsDTO tokenClaims, SharedValueSessionId sessionId,
                                         boolean isAuthenticated, Instant expiresAt) {
        this.userId = userId;
        this.userRoles = userRoles;
        this.tokenClaims = tokenClaims;
        this.sessionId = sessionId;
        this.isAuthenticated = isAuthenticated;
        this.expiresAt = expiresAt;
    }

    /**
     * Converts this DTO to a domain entity.
     * 
     * @return DomainEntityAuthenticationContext
     */
    public DomainEntityAuthenticationContext toEntity() {
        if (sessionId == null) {
            throw new IllegalStateException("SessionId cannot be null when converting to entity");
        }
        if (expiresAt == null) {
            throw new IllegalStateException("ExpiresAt cannot be null when converting to entity");
        }
        
        DomainValueTokenClaims domainTokenClaims = null;
        if (tokenClaims != null) {
            domainTokenClaims = tokenClaims.toValueObject();
        }
        
        return new DomainEntityAuthenticationContext(
            userId,
            userRoles,
            domainTokenClaims,
            sessionId,
            isAuthenticated,
            expiresAt
        );
    }

    /**
     * Creates a DTO from a domain entity.
     * 
     * @param entity the domain entity
     * @return SharedAuthenticationContextDTO
     */
    public static SharedAuthenticationContextDTO fromEntity(DomainEntityAuthenticationContext entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        
        return entity.toDTO();
    }

    public SharedValueUserId getUserId() {
        return userId;
    }

    public void setUserId(SharedValueUserId userId) {
        this.userId = userId;
    }

    public Set<SharedEnumUserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<SharedEnumUserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public SharedTokenClaimsDTO getTokenClaims() {
        return tokenClaims;
    }

    public void setTokenClaims(SharedTokenClaimsDTO tokenClaims) {
        this.tokenClaims = tokenClaims;
    }

    public SharedValueSessionId getSessionId() {
        return sessionId;
    }

    public void setSessionId(SharedValueSessionId sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "SharedAuthenticationContextDTO{" +
                "userId=" + userId +
                ", userRoles=" + userRoles +
                ", tokenClaims=" + tokenClaims +
                ", sessionId=" + sessionId +
                ", isAuthenticated=" + isAuthenticated +
                ", expiresAt=" + expiresAt +
                '}';
    }
}