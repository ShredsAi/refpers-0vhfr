package ai.shreds.shared.dtos;

import ai.shreds.domain.value_objects.DomainValueTokenClaims;

import java.time.Instant;
import java.util.Set;

public class SharedTokenClaimsDTO {
    private String subject;
    private String issuer;
    private String audience;
    private Instant issuedAt;
    private Instant expiresAt;
    private Set<String> roles;

    public SharedTokenClaimsDTO() {
    }

    public SharedTokenClaimsDTO(String subject, String issuer, String audience, Instant issuedAt, Instant expiresAt, Set<String> roles) {
        this.subject = subject;
        this.issuer = issuer;
        this.audience = audience;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.roles = roles;
    }

    /**
     * Converts this DTO to a domain value object.
     * 
     * @return DomainValueTokenClaims
     */
    public DomainValueTokenClaims toValueObject() {
        return new DomainValueTokenClaims(
            subject,
            issuer,
            audience,
            issuedAt,
            expiresAt,
            roles
        );
    }

    /**
     * Creates a DTO from a domain value object.
     * 
     * @param vo the domain value object
     * @return SharedTokenClaimsDTO
     */
    public static SharedTokenClaimsDTO fromValueObject(DomainValueTokenClaims vo) {
        if (vo == null) {
            throw new IllegalArgumentException("Value object cannot be null");
        }
        
        return new SharedTokenClaimsDTO(
            vo.getSubject(),
            vo.getIssuer(),
            vo.getAudience(),
            vo.getIssuedAt(),
            vo.getExpiresAt(),
            vo.getRoles()
        );
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "SharedTokenClaimsDTO{" +
                "subject='" + subject + '\'' +
                ", issuer='" + issuer + '\'' +
                ", audience='" + audience + '\'' +
                ", issuedAt=" + issuedAt +
                ", expiresAt=" + expiresAt +
                ", roles=" + roles +
                '}';
    }
}