package ai.shreds.application.services;

import ai.shreds.application.dtos.ApplicationPaymentMethodRequestDTO;
import ai.shreds.application.dtos.ApplicationPaymentMethodResponseDTO;
import ai.shreds.application.dtos.ApplicationPaymentMethodActivationDTO;
import ai.shreds.application.ports.ApplicationInputPortPaymentMethod;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.domain.ports.DomainInputPortPaymentMethod;
import ai.shreds.domain.dtos.DomainPaymentMethod;
import ai.shreds.application.exceptions.ApplicationServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationPaymentMethodService implements ApplicationInputPortPaymentMethod {
    
    private final DomainInputPortPaymentMethod domainPaymentMethodPort;
    private final ApplicationOutputPortEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public ApplicationPaymentMethodResponseDTO addPaymentMethod(ApplicationPaymentMethodRequestDTO request) {
        log.info("Adding payment method for accountId: {}", request.getAccountId());
        
        try {
            // Convert to domain request and process
            var domainRequest = request.toDomainRequest();
            DomainPaymentMethod domainPaymentMethod = domainPaymentMethodPort.addPaymentMethod(domainRequest);
            
            // Convert to application response
            ApplicationPaymentMethodResponseDTO response = ApplicationPaymentMethodResponseDTO.fromDomainPaymentMethod(domainPaymentMethod);
            
            // Publish event
            eventPublisher.publishPaymentMethodAdded(domainPaymentMethod.getPaymentMethodId(), request.getAccountId());
            
            log.info("Successfully added payment method {} for accountId: {}", 
                domainPaymentMethod.getPaymentMethodId(), request.getAccountId());
            
            return response;
            
        } catch (Exception e) {
            log.error("Failed to add payment method for accountId: {}", request.getAccountId(), e);
            throw new ApplicationServiceException("Failed to add payment method for accountId: " + request.getAccountId(), 
                "PAYMENT_METHOD_ADDITION_FAILED", e);
        }
    }
    
    @Override
    @Transactional
    public ApplicationPaymentMethodActivationDTO activatePaymentMethod(String paymentMethodId) {
        log.info("Activating payment method: {}", paymentMethodId);
        
        try {
            // Activate through domain layer
            DomainPaymentMethod activatedPaymentMethod = domainPaymentMethodPort.activatePaymentMethod(paymentMethodId);
            
            // Convert to application response
            ApplicationPaymentMethodActivationDTO response = ApplicationPaymentMethodActivationDTO.fromDomainPaymentMethod(activatedPaymentMethod);
            
            log.info("Successfully activated payment method: {}", paymentMethodId);
            return response;
            
        } catch (Exception e) {
            log.error("Failed to activate payment method: {}", paymentMethodId, e);
            throw new ApplicationServiceException("Failed to activate payment method: " + paymentMethodId, 
                "PAYMENT_METHOD_ACTIVATION_FAILED", e);
        }
    }
}