package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedRequestContextDTO;

public interface ApplicationOutputPortMessagePublisher {

    /**
     * Publish the authenticated request context to downstream message channels.
     * @param context the request context containing authentication and metadata
     */
    void publishAuthenticatedRequest(SharedRequestContextDTO context);
}