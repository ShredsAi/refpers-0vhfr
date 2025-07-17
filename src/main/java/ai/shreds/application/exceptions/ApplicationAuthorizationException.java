package ai.shreds.application.exceptions;

import java.util.Set;
import ai.shreds.shared.enums.SharedEnumUserRole;

public class ApplicationAuthorizationException extends RuntimeException {
    private final Set<SharedEnumUserRole> requiredRoles;

    public ApplicationAuthorizationException(String message, Set<SharedEnumUserRole> requiredRoles) {
        super(message);
        this.requiredRoles = requiredRoles;
    }

    public Set<SharedEnumUserRole> getRequiredRoles() {
        return requiredRoles;
    }
}