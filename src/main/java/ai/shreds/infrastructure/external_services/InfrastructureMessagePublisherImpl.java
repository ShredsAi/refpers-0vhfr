package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationOutputPortMessagePublisher;
import ai.shreds.infrastructure.config.InfrastructureRabbitMQConfig;
import ai.shreds.infrastructure.exceptions.InfrastructureMessagePublishException;
import ai.shreds.infrastructure.messaging.InfrastructureAuthenticatedRequestMessage;
import ai.shreds.shared.dtos.SharedRequestContextDTO;
import ai.shreds.shared.utils.SharedUtilJsonSerializer;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class InfrastructureMessagePublisherImpl implements ApplicationOutputPortMessagePublisher {

    private final InfrastructureRabbitMQClient rabbitMQClient;
    private final SharedUtilJsonSerializer jsonSerializer;
    private final InfrastructureRabbitMQConfig rabbitMQConfig;

    public InfrastructureMessagePublisherImpl(InfrastructureRabbitMQClient rabbitMQClient, 
                                              SharedUtilJsonSerializer jsonSerializer,
                                              InfrastructureRabbitMQConfig rabbitMQConfig) {
        this.rabbitMQClient = rabbitMQClient;
        this.jsonSerializer = jsonSerializer;
        this.rabbitMQConfig = rabbitMQConfig;
    }

    @Override
    public void publishAuthenticatedRequest(SharedRequestContextDTO context) {
        try {
            InfrastructureAuthenticatedRequestMessage message = buildMessage(context);
            String json = jsonSerializer.serialize(message);
            rabbitMQClient.publish(rabbitMQConfig.getExchange(), rabbitMQConfig.getRoutingKey(), json);
        } catch (Exception e) {
            throw new InfrastructureMessagePublishException(
                    "Failed to publish authenticated request: " + e.getMessage(),
                    rabbitMQConfig.getExchange(),
                    rabbitMQConfig.getRoutingKey()
            );
        }
    }

    private InfrastructureAuthenticatedRequestMessage buildMessage(SharedRequestContextDTO context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-User-Id", context.getAuthContext().getUserId().getValue());
        headers.put("X-User-Roles", String.join(",", 
                context.getAuthContext().getUserRoles().stream()
                .map(Enum::name)
                .toList()));
        
        return new InfrastructureAuthenticatedRequestMessage(
                context.getRequestId().getValue(),
                Instant.now(),
                jsonSerializer.serialize(context),
                headers
        );
    }
}