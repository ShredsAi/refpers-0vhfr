package ai.shreds.domain.exceptions;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import ai.shreds.shared.enums.SharedEnumUserRole;

/**
 * Exception thrown when a user's roles don't meet the required permissions for a route.
 * This exception provides detailed information about the permission mismatch,
 * including which roles were required and which roles the user actually had.
 */
public class DomainExceptionInsufficientPermissions extends RuntimeException {
    private final Set<SharedEnumUserRole> requiredRoles;
    private final Set<SharedEnumUserRole> userRoles;
    private static final String DEFAULT_MESSAGE = "Insufficient permissions for requested operation";

    /**
     * Constructs a new exception with the required and actual user roles.
     *
     * @param requiredRoles the set of roles required for access
     * @param userRoles the set of roles the user actually has
     */
    public DomainExceptionInsufficientPermissions(
            Set<SharedEnumUserRole> requiredRoles,
            Set<SharedEnumUserRole> userRoles) {
        super(formatMessage(requiredRoles, userRoles));
        this.requiredRoles = new HashSet<>(requiredRoles != null ? requiredRoles : Collections.emptySet());
        this.userRoles = new HashSet<>(userRoles != null ? userRoles : Collections.emptySet());
    }

    /**
     * Gets the set of roles required for access.
     *
     * @return an unmodifiable set of the required roles
     */
    public Set<SharedEnumUserRole> getRequiredRoles() {
        return Collections.unmodifiableSet(requiredRoles);
    }

    /**
     * Gets the set of roles the user has.
     *
     * @return an unmodifiable set of the user's roles
     */
    public Set<SharedEnumUserRole> getUserRoles() {
        return Collections.unmodifiableSet(userRoles);
    }

    /**
     * Checks which required roles are missing from the user's roles.
     *
     * @return an unmodifiable set of the missing roles
     */
    public Set<SharedEnumUserRole> getMissingRoles() {
        Set<SharedEnumUserRole> missingRoles = new HashSet<>(requiredRoles);
        missingRoles.removeAll(userRoles);
        return Collections.unmodifiableSet(missingRoles);
    }

    /**
     * Checks if the user has any of the required roles.
     *
     * @return true if the user has at least one required role, false otherwise
     */
    public boolean hasAnyRequiredRole() {
        return !Collections.disjoint(requiredRoles, userRoles);
    }

    /**
     * Gets a formatted string of missing roles.
     *
     * @return comma-separated string of missing role names
     */
    public String getMissingRolesAsString() {
        return getMissingRoles().stream()
            .map(SharedEnumUserRole::name)
            .collect(Collectors.joining(", "));
    }

    private static String formatMessage(Set<SharedEnumUserRole> required, Set<SharedEnumUserRole> actual) {
        return String.format("%s. Required roles: [%s], User roles: [%s]",
            DEFAULT_MESSAGE,
            required != null ? required.stream().map(SharedEnumUserRole::name).collect(Collectors.joining(", ")) : "none",
            actual != null ? actual.stream().map(SharedEnumUserRole::name).collect(Collectors.joining(", ")) : "none");
    }

    @Override
    public String toString() {
        return String.format("%s{requiredRoles=[%s], userRoles=[%s]}",
            getClass().getSimpleName(),
            requiredRoles.stream().map(SharedEnumUserRole::name).collect(Collectors.joining(", ")),
            userRoles.stream().map(SharedEnumUserRole::name).collect(Collectors.joining(", ")));
    }
}