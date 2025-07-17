package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedAuthenticationContextDTO;
import ai.shreds.shared.dtos.SharedRouteDTO;

public interface ApplicationAuthenticationPort {

    /**
     * Authenticate the given JWT against the specified route requirements.
     * @param jwt the raw JWT token
     * @param route the route metadata
     * @return populated authentication context DTO
     */
    SharedAuthenticationContextDTO authenticateRequest(String jwt, SharedRouteDTO route);

    /**
     * Create an anonymous authentication context for public or unauthenticated requests.
     * @param requestId unique request identifier for tracing
     * @return anonymous authentication context DTO
     */
    SharedAuthenticationContextDTO createAnonymousContext(String requestId);
}