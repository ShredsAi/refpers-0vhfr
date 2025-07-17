package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityAuthenticationContext;
import ai.shreds.domain.entities.DomainEntityRequestContext;
import ai.shreds.domain.value_objects.DomainValueRequestMetadata;
import ai.shreds.domain.value_objects.DomainValueSecurityHeaders;

/**
 * Port interface for request context creation operations in the domain layer.
 * This port defines how request contexts are generated and security headers produced.
 */
public interface DomainInputPortRequestContext {

    /**
     * Creates a complete request context entity from request metadata and authentication context.
     * The request context combines information from the HTTP request with the authentication state.
     *
     * @param request the metadata extracted from the incoming request
     * @param authContext the authentication context for the request
     * @return a fully populated request context entity
     */
    DomainEntityRequestContext createRequestContext(
        DomainValueRequestMetadata request,
        DomainEntityAuthenticationContext authContext
    );

    /**
     * Generates security headers from an authentication context for downstream service propagation.
     * These headers will contain user identification and role information in a format suitable
     * for transmission to backend services.
     *
     * @param authContext the authentication context to extract user information from
     * @return security headers value object with user ID and roles
     */
    DomainValueSecurityHeaders generateSecurityHeaders(DomainEntityAuthenticationContext authContext);
}