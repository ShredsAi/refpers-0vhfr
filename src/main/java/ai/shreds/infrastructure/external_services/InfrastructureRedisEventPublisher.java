package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationEventPublisherOutputPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException;

@Service
public class InfrastructureRedisEventPublisher implements ApplicationEventPublisherOutputPort {

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public InfrastructureRedisEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishEvent(Object event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (Exception ex) {
            throw new InfrastructureExternalServiceException(
                "Failed to publish event: " + ex.getMessage(),
                "RedisEventPublisher",
                0
            );
        }
    }
}
