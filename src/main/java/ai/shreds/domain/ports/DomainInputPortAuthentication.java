package ai.shreds.domain.ports;

import java.util.List;

import ai.shreds.shared.value_objects.SharedPublicKey;
import ai.shreds.shared.value_objects.SharedValueSessionId;
import ai.shreds.domain.value_objects.DomainValueTokenClaims;
import ai.shreds.domain.entities.DomainEntityAuthenticationContext;

/**
 * Port interface for token validation and authentication context creation.
 */
public interface DomainInputPortAuthentication {

    /**
     * Validate the JWT token against provided public keys and return token claims.
     *
     * @param jwt the raw JWT string
     * @param publicKeys the list of public keys for signature verification
     * @return extracted and validated token claims
     */
    DomainValueTokenClaims validateToken(String jwt, List<SharedPublicKey> publicKeys);

    /**
     * Create a full authentication context from validated claims and session ID.
     *
     * @param claims the validated token claims
     * @param sessionId the session identifier to attach
     * @return populated authentication context entity
     */
    DomainEntityAuthenticationContext createAuthenticationContext(
        DomainValueTokenClaims claims,
        SharedValueSessionId sessionId
    );

    /**
     * Create an anonymous authentication context for public or unauthenticated access.
     *
     * @param sessionId session identifier for anonymous context
     * @return anonymous authentication context entity
     */
    DomainEntityAuthenticationContext createAnonymousContext(SharedValueSessionId sessionId);
}