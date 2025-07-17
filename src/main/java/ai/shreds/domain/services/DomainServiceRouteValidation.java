package ai.shreds.domain.services;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.stereotype.Service;

import ai.shreds.domain.ports.DomainInputPortRouteValidation;
import ai.shreds.domain.entities.DomainEntityRoute;
import ai.shreds.domain.exceptions.DomainExceptionInsufficientPermissions;
import ai.shreds.shared.enums.SharedEnumUserRole;
import ai.shreds.shared.value_objects.SharedValuePathPattern;

/**
 * Service implementing route validation logic.
 */
@Service
public class DomainServiceRouteValidation implements DomainInputPortRouteValidation {
    
    @Override
    public boolean validateRoute(DomainEntityRoute route) {
        if (route == null) {
            return false;
        }
        
        // Check if route is active
        if (!isRouteActive(route)) {
            return false;
        }
        
        // Validate path pattern
        if (route.getPathPattern() == null) {
            return false;
        }
        
        // Validate target service
        if (route.getTargetService() == null) {
            return false;
        }
        
        // Validate HTTP method
        if (route.getHttpMethod() == null) {
            return false;
        }
        
        // Validate route ID
        if (route.getRouteId() == null) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean matchPathPattern(String path, SharedValuePathPattern pattern) {
        if (path == null || pattern == null) {
            return false;
        }
        
        return patternMatchesPath(pattern.getPattern(), path);
    }
    
    @Override
    public boolean checkRouteAuthorization(DomainEntityRoute route, Set<SharedEnumUserRole> roles) {
        if (route == null || roles == null) {
            return false;
        }
        
        // If route doesn't require authentication, allow access
        if (!route.isAuthenticationRequired()) {
            return true;
        }
        
        // For authenticated routes, check if user has required roles
        // For now, we'll allow any authenticated user (can be extended later)
        return !roles.isEmpty() && !roles.contains(SharedEnumUserRole.GUEST);
    }
    
    private boolean isRouteActive(DomainEntityRoute route) {
        return route.isActive();
    }
    
    private boolean hasRequiredRole(Set<SharedEnumUserRole> requiredRoles, Set<SharedEnumUserRole> userRoles) {
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return true; // No specific roles required
        }
        
        if (userRoles == null || userRoles.isEmpty()) {
            return false; // No user roles provided
        }
        
        // Check if user has any of the required roles
        for (SharedEnumUserRole userRole : userRoles) {
            if (requiredRoles.contains(userRole)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean patternMatchesPath(String pattern, String path) {
        if (pattern == null || path == null) {
            return false;
        }
        
        try {
            // Convert path pattern to regex
            String regex = convertPathPatternToRegex(pattern);
            Pattern compiledPattern = Pattern.compile(regex);
            return compiledPattern.matcher(path).matches();
        } catch (PatternSyntaxException e) {
            // Invalid pattern, return false
            return false;
        }
    }
    
    private String convertPathPatternToRegex(String pathPattern) {
        // Handle common path patterns
        String regex = pathPattern
            .replaceAll("\\*\\*", ".*") // ** matches any number of path segments
            .replaceAll("\\*", "[^/]*") // * matches within a single path segment
            .replaceAll("\\{[^}]+\\}", "[^/]+"); // {param} matches path parameters
        
        // Escape special regex characters that are not wildcards
        regex = regex.replaceAll("([\\[\\]\\(\\)\\+\\?\\^\\\\\\.|])", "\\\\$1");
        
        return "^" + regex + "$";
    }
}