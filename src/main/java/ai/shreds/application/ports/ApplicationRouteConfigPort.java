package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedRouteDTO;
import ai.shreds.shared.enums.SharedEnumHttpMethod;

public interface ApplicationRouteConfigPort {

    /**
     * Retrieve the route configuration for a given request path and HTTP method.
     */
    SharedRouteDTO getRouteForRequest(String path, SharedEnumHttpMethod method);

    /**
     * Check if authentication is required for the specified route.
     */
    boolean isAuthenticationRequired(SharedRouteDTO route);
}