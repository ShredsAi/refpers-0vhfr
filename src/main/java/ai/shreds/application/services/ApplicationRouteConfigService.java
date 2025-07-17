package ai.shreds.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.shreds.application.ports.ApplicationRouteConfigPort;
import ai.shreds.application.ports.ApplicationOutputPortRouteRepository;
import ai.shreds.application.exceptions.ApplicationAuthenticationException;
import ai.shreds.application.exceptions.ApplicationAuthorizationException;
import ai.shreds.domain.ports.DomainInputPortRouteValidation;
import ai.shreds.domain.entities.DomainEntityRoute;
import ai.shreds.domain.exceptions.DomainExceptionInsufficientPermissions;
import ai.shreds.shared.dtos.SharedRouteDTO;
import ai.shreds.shared.enums.SharedEnumHttpMethod;

@Service
public class ApplicationRouteConfigService implements ApplicationRouteConfigPort {

    private final ApplicationOutputPortRouteRepository routeRepository;
    private final DomainInputPortRouteValidation domainRouteService;

    @Autowired
    public ApplicationRouteConfigService(ApplicationOutputPortRouteRepository routeRepository,
                                         DomainInputPortRouteValidation domainRouteService) {
        this.routeRepository = routeRepository;
        this.domainRouteService = domainRouteService;
    }

    @Override
    public SharedRouteDTO getRouteForRequest(String path, SharedEnumHttpMethod method) {
        try {
            SharedRouteDTO routeDto = routeRepository.findRouteByPathAndMethod(path, method);
            if (routeDto == null) {
                throw new ApplicationAuthenticationException(
                    "Route not found for path: " + path + " and method: " + method, 
                    "ROUTE_NOT_FOUND"
                );
            }
            
            DomainEntityRoute route = DomainEntityRoute.fromDTO(routeDto);
            if (!domainRouteService.validateRoute(route)) {
                throw new ApplicationAuthenticationException(
                    "Invalid route configuration for path: " + path + " and method: " + method, 
                    "INVALID_ROUTE_CONFIG"
                );
            }
            
            return routeDto;
        } catch (DomainExceptionInsufficientPermissions e) {
            throw new ApplicationAuthorizationException(
                "Insufficient permissions for route: " + path + " (" + method + ")", 
                e.getRequiredRoles()
            );
        } catch (ApplicationAuthenticationException | ApplicationAuthorizationException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationAuthenticationException(
                "Failed to retrieve route configuration: " + e.getMessage(), 
                "ROUTE_CONFIG_ERROR"
            );
        }
    }

    @Override
    public boolean isAuthenticationRequired(SharedRouteDTO route) {
        if (route == null) {
            return true; // Default to requiring authentication for safety
        }
        return route.isAuthenticationRequired();
    }
}