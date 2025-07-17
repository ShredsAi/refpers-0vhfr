package ai.shreds.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.shreds.application.ports.ApplicationRequestContextPort;
import ai.shreds.application.ports.ApplicationOutputPortMessagePublisher;
import ai.shreds.application.exceptions.ApplicationAuthenticationException;
import ai.shreds.domain.ports.DomainInputPortRequestContext;
import ai.shreds.domain.value_objects.DomainValueRequestMetadata;
import ai.shreds.domain.entities.DomainEntityAuthenticationContext;
import ai.shreds.domain.entities.DomainEntityRequestContext;
import ai.shreds.shared.dtos.SharedRequestContextDTO;
import ai.shreds.shared.dtos.SharedServerHttpRequestDTO;
import ai.shreds.shared.dtos.SharedAuthenticationContextDTO;

@Service
public class ApplicationRequestContextService implements ApplicationRequestContextPort {

    private final ApplicationOutputPortMessagePublisher messagePublisher;
    private final DomainInputPortRequestContext domainContextBuilder;

    @Autowired
    public ApplicationRequestContextService(ApplicationOutputPortMessagePublisher messagePublisher,
                                            DomainInputPortRequestContext domainContextBuilder) {
        this.messagePublisher = messagePublisher;
        this.domainContextBuilder = domainContextBuilder;
    }

    @Override
    public SharedRequestContextDTO buildRequestContext(SharedServerHttpRequestDTO request,
                                                       SharedAuthenticationContextDTO authContextDTO) {
        try {
            if (request == null) {
                throw new ApplicationAuthenticationException(
                    "Request cannot be null", 
                    "INVALID_REQUEST"
                );
            }
            
            if (authContextDTO == null) {
                throw new ApplicationAuthenticationException(
                    "Authentication context cannot be null", 
                    "INVALID_AUTH_CONTEXT"
                );
            }
            
            DomainValueRequestMetadata metadata = new DomainValueRequestMetadata(
                request.getPath(),
                request.getMethod(),
                request.getHeaders(),
                request.getRemoteAddress(),
                request.getQueryParams()
            );
            
            DomainEntityAuthenticationContext authContext = authContextDTO.toEntity();
            DomainEntityRequestContext domainContext = domainContextBuilder.createRequestContext(metadata, authContext);
            
            return domainContext.toDTO();
        } catch (Exception e) {
            throw new ApplicationAuthenticationException(
                "Failed to build request context: " + e.getMessage(), 
                "REQUEST_CONTEXT_BUILD_ERROR"
            );
        }
    }

    @Override
    public void publishRequestContext(SharedRequestContextDTO context) {
        try {
            if (context == null) {
                throw new ApplicationAuthenticationException(
                    "Request context cannot be null for publishing", 
                    "INVALID_CONTEXT_FOR_PUBLISHING"
                );
            }
            
            messagePublisher.publishAuthenticatedRequest(context);
        } catch (Exception e) {
            throw new ApplicationAuthenticationException(
                "Failed to publish request context: " + e.getMessage(), 
                "REQUEST_CONTEXT_PUBLISH_ERROR"
            );
        }
    }
}