package ai.shreds.application.services;

import ai.shreds.application.dtos.ApplicationTransactionDTO;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationEventPublishingService implements ApplicationOutputPortEventPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${financial.kafka.topic.name:financial-events}")
    private String kafkaTopicName;
    
    @Override
    public void publishFinancialAccountCreated(String accountId) {
        log.info("Publishing FinancialAccountCreated event for accountId: {}", accountId);
        
        try {
            Map<String, Object> eventPayload = new HashMap<>();
            eventPayload.put("eventType", "FinancialAccountCreated");
            eventPayload.put("accountId", accountId);
            eventPayload.put("timestamp", Instant.now().toString());
            
            String eventJson = objectMapper.writeValueAsString(eventPayload);
            kafkaTemplate.send(kafkaTopicName, accountId, eventJson);
            
            log.info("Successfully published FinancialAccountCreated event for accountId: {}", accountId);
        } catch (Exception e) {
            log.error("Failed to publish FinancialAccountCreated event for accountId: {}", accountId, e);
            // Don't throw exception to avoid breaking the main business flow
        }
    }
    
    @Override
    public void publishTransactionProcessed(ApplicationTransactionDTO transaction) {
        log.info("Publishing TransactionProcessed event for transactionId: {}", transaction.getTransactionId());
        
        try {
            Map<String, Object> eventPayload = new HashMap<>();
            eventPayload.put("eventType", "TransactionProcessed");
            eventPayload.put("transactionId", transaction.getTransactionId());
            eventPayload.put("amount", transaction.getAmount());
            eventPayload.put("type", transaction.getType());
            eventPayload.put("description", transaction.getDescription());
            eventPayload.put("reference", transaction.getReference());
            eventPayload.put("timestamp", transaction.getTimestamp());
            
            String eventJson = objectMapper.writeValueAsString(eventPayload);
            
            // Use transaction reference as key if available, otherwise use transactionId
            String messageKey = transaction.getReference() != null ? transaction.getReference() : transaction.getTransactionId();
            kafkaTemplate.send(kafkaTopicName, messageKey, eventJson);
            
            log.info("Successfully published TransactionProcessed event for transactionId: {}", transaction.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish TransactionProcessed event for transactionId: {}", transaction.getTransactionId(), e);
            // Don't throw exception to avoid breaking the main business flow
        }
    }
    
    @Override
    public void publishPaymentMethodAdded(String paymentMethodId, String accountId) {
        log.info("Publishing PaymentMethodAdded event for paymentMethodId: {} and accountId: {}", paymentMethodId, accountId);
        
        try {
            Map<String, Object> eventPayload = new HashMap<>();
            eventPayload.put("eventType", "PaymentMethodAdded");
            eventPayload.put("paymentMethodId", paymentMethodId);
            eventPayload.put("accountId", accountId);
            eventPayload.put("timestamp", Instant.now().toString());
            
            String eventJson = objectMapper.writeValueAsString(eventPayload);
            kafkaTemplate.send(kafkaTopicName, accountId, eventJson);
            
            log.info("Successfully published PaymentMethodAdded event for paymentMethodId: {}", paymentMethodId);
        } catch (Exception e) {
            log.error("Failed to publish PaymentMethodAdded event for paymentMethodId: {}", paymentMethodId, e);
            // Don't throw exception to avoid breaking the main business flow
        }
    }
}