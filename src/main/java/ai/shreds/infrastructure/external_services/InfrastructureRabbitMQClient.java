package ai.shreds.infrastructure.external_services;

import ai.shreds.infrastructure.config.InfrastructureRabbitMQConfig;
import ai.shreds.infrastructure.exceptions.InfrastructureMessagePublishException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class InfrastructureRabbitMQClient {

    private final RabbitTemplate rabbitTemplate;
    private final InfrastructureRabbitMQConfig config;

    public InfrastructureRabbitMQClient(RabbitTemplate rabbitTemplate, InfrastructureRabbitMQConfig config) {
        this.rabbitTemplate = rabbitTemplate;
        this.config = config;
    }

    public void publish(String exchange, String routingKey, String message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
                msg.getMessageProperties().setContentType("application/json");
                msg.getMessageProperties().setContentEncoding("UTF-8");
                return msg;
            });
        } catch (Exception e) {
            throw new InfrastructureMessagePublishException(
                "Failed to publish message: " + e.getMessage(),
                exchange,
                routingKey
            );
        }
    }

    public boolean publishWithConfirm(String exchange, String routingKey, String message) {
        try {
            return rabbitTemplate.invoke(operations -> {
                operations.convertAndSend(exchange, routingKey, message);
                return operations.waitForConfirms(config.getConnectionTimeout().toMillis());
            });
        } catch (Exception e) {
            throw new InfrastructureMessagePublishException(
                "Failed to publish message with confirmation: " + e.getMessage(),
                exchange,
                routingKey
            );
        }
    }

    public void setupExchangeAndQueue() {
        try {
            rabbitTemplate.execute(channel -> {
                channel.exchangeDeclare(config.getExchange(), "topic", true);
                channel.queueDeclare(config.getQueueName(), true, false, false, null);
                channel.queueBind(config.getQueueName(), config.getExchange(), config.getRoutingKey());
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup RabbitMQ exchange and queue: " + e.getMessage(), e);
        }
    }
}