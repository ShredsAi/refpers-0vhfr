package ai.shreds.domain.ports;

import java.util.Set;

import ai.shreds.domain.entities.DomainEntityRoute;
import ai.shreds.shared.enums.SharedEnumUserRole;
import ai.shreds.shared.value_objects.SharedValuePathPattern;

/**
 * Port interface for route validation and matching operations in the domain layer.
 * This port defines how routes are validated, matched against request paths, and
 * checked for proper authorization.
 */
public interface DomainInputPortRouteValidation {

    /**
     * Validates if a route is correctly configured and active.
     * A valid route must have a properly formatted path pattern, valid target service,
     * and be marked as active.
     *
     * @param route the route to validate
     * @return true if the route is valid and active, false otherwise
     */
    boolean validateRoute(DomainEntityRoute route);

    /**
     * Checks if a path matches a path pattern.
     * This method supports wildcard matching and path parameters to determine
     * if a request path should be routed according to a specific pattern.
     *
     * @param path the request path to match
     * @param pattern the route pattern to match against
     * @return true if the path matches the pattern, false otherwise
     */
    boolean matchPathPattern(String path, SharedValuePathPattern pattern);

    /**
     * Checks if a set of user roles has the required authorization for a route.
     * This method evaluates whether the provided roles meet any authorization
     * requirements specified by the route.
     *
     * @param route the route to check authorization against
     * @param roles the set of user roles to validate
     * @return true if the roles have sufficient authorization, false otherwise
     */
    boolean checkRouteAuthorization(DomainEntityRoute route, Set<SharedEnumUserRole> roles);
}