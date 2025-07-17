package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedAuthenticationContextDTO;
import ai.shreds.shared.dtos.SharedRequestContextDTO;
import ai.shreds.shared.dtos.SharedServerHttpRequestDTO;

public interface ApplicationRequestContextPort {

    /**
     * Build a shared request context combining request metadata and authentication context.
     */
    SharedRequestContextDTO buildRequestContext(SharedServerHttpRequestDTO request,
                                                 SharedAuthenticationContextDTO authContext);

    /**
     * Publish the built request context to downstream services or message channels.
     */
    void publishRequestContext(SharedRequestContextDTO context);
}