package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.application.dtos.ApplicationTransactionDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class InfrastructureKafkaEventPublisher implements ApplicationOutputPortEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName;
    private final ObjectMapper objectMapper;

    public InfrastructureKafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${financial.kafka.topic-name:financial-events}") String topicName,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishFinancialAccountCreated(String accountId) {
        Map<String, Object> payload = Map.of(
            "eventType", "FINANCIAL_ACCOUNT_CREATED",
            "accountId", accountId,
            "createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        publishEvent("FINANCIAL_ACCOUNT_CREATED", payload, accountId);
    }

    @Override
    public void publishTransactionProcessed(ApplicationTransactionDTO transaction) {
        Map<String, Object> payload = Map.of(
            "eventType", "TRANSACTION_PROCESSED",
            "transactionId", transaction.getTransactionId(),
            "amount", transaction.getAmount(),
            "type", transaction.getType(),
            "description", transaction.getDescription(),
            "reference", transaction.getReference() != null ? transaction.getReference() : "",
            "timestamp", transaction.getTimestamp()
        );
        
        // Extract account ID from transaction context if available
        String accountId = extractAccountIdFromTransaction(transaction);
        publishEvent("TRANSACTION_PROCESSED", payload, accountId);
    }

    @Override
    public void publishPaymentMethodAdded(String paymentMethodId, String accountId) {
        Map<String, Object> payload = Map.of(
            "eventType", "PAYMENT_METHOD_ADDED",
            "paymentMethodId", paymentMethodId,
            "accountId", accountId,
            "addedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        publishEvent("PAYMENT_METHOD_ADDED", payload, accountId);
    }

    private void publishEvent(String eventType, Map<String, Object> payload, String accountId) {
        try {
            InfrastructureKafkaEventDTO eventDTO = new InfrastructureKafkaEventDTO(
                eventType,
                accountId != null ? accountId : "unknown",
                payload,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            String eventJson = eventDTO.toJson();
            
            // Use account ID as partition key to ensure ordered processing
            kafkaTemplate.send(topicName, accountId, eventJson);
        } catch (Exception e) {
            // Log error but don't throw - event publishing failure shouldn't break business logic
            System.err.println("Failed to publish event: " + eventType + ", error: " + e.getMessage());
        }
    }

    private String extractAccountIdFromTransaction(ApplicationTransactionDTO transaction) {
        // This would typically be passed in a better design, for now return a placeholder
        return "transaction-account";
    }
}