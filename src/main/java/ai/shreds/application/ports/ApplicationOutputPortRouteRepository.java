package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedRouteDTO;
import ai.shreds.shared.enums.SharedEnumHttpMethod;
import java.util.List;

public interface ApplicationOutputPortRouteRepository {

    /**
     * Find a route configuration by path and HTTP method.
     * @param path request path
     * @param method HTTP method
     * @return matching route DTO
     */
    SharedRouteDTO findRouteByPathAndMethod(String path, SharedEnumHttpMethod method);

    /**
     * Retrieve all active route configurations.
     * @return list of active route DTOs
     */
    List<SharedRouteDTO> getAllActiveRoutes();
}