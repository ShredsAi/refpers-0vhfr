package ai.shreds.domain.value_objects;

import java.time.Instant;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import ai.shreds.shared.dtos.SharedTokenClaimsDTO;

/**
 * Value object representing JWT token claims.
 */
public class DomainValueTokenClaims {
    private final String subject;
    private final String issuer;
    private final String audience;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final Set<String> roles;
    
    public DomainValueTokenClaims(
            String subject,
            String issuer,
            String audience,
            Instant issuedAt,
            Instant expiresAt,
            Set<String> roles) {
        this.subject = subject;
        this.issuer = issuer;
        this.audience = audience;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
        validate();
    }
    
    private void validate() {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        if (issuer == null || issuer.trim().isEmpty()) {
            throw new IllegalArgumentException("Issuer cannot be null or empty");
        }
        if (audience == null || audience.trim().isEmpty()) {
            throw new IllegalArgumentException("Audience cannot be null or empty");
        }
        if (issuedAt == null) {
            throw new IllegalArgumentException("Issued at cannot be null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("Expires at cannot be null");
        }
        if (expiresAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("Expires at must be after issued at");
        }
    }
    
    public String getSubject() {
        return subject;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public String getAudience() {
        return audience;
    }
    
    public Instant getIssuedAt() {
        return issuedAt;
    }
    
    public Instant getExpiresAt() {
        return expiresAt;
    }
    
    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
    
    /**
     * Convert to shared DTO.
     */
    public SharedTokenClaimsDTO toDTO() {
        SharedTokenClaimsDTO dto = new SharedTokenClaimsDTO();
        dto.setSubject(subject);
        dto.setIssuer(issuer);
        dto.setAudience(audience);
        dto.setIssuedAt(issuedAt);
        dto.setExpiresAt(expiresAt);
        return dto;
    }
    
    /**
     * Create from shared DTO.
     */
    public static DomainValueTokenClaims fromDTO(SharedTokenClaimsDTO dto) {
        return new DomainValueTokenClaims(
            dto.getSubject(),
            dto.getIssuer(),
            dto.getAudience(),
            dto.getIssuedAt(),
            dto.getExpiresAt(),
            Set.of() // Roles are not part of SharedTokenClaimsDTO
        );
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainValueTokenClaims that = (DomainValueTokenClaims) o;
        return subject.equals(that.subject) &&
               issuer.equals(that.issuer) &&
               audience.equals(that.audience) &&
               issuedAt.equals(that.issuedAt) &&
               expiresAt.equals(that.expiresAt) &&
               roles.equals(that.roles);
    }
    
    @Override
    public int hashCode() {
        int result = subject.hashCode();
        result = 31 * result + issuer.hashCode();
        result = 31 * result + audience.hashCode();
        result = 31 * result + issuedAt.hashCode();
        result = 31 * result + expiresAt.hashCode();
        result = 31 * result + roles.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return String.format("TokenClaims{subject='%s', issuer='%s', audience='%s', issuedAt=%s, expiresAt=%s, roles=%s}",
            subject, issuer, audience, issuedAt, expiresAt, roles);
    }
}