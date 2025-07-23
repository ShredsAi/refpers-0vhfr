package ai.shreds.domain.services;

import ai.shreds.domain.dtos.DomainPaymentMethod;
import ai.shreds.domain.dtos.DomainPaymentMethodRequest;
import ai.shreds.domain.entities.DomainEntityPaymentMethod;
import ai.shreds.domain.enums.DomainPaymentTypeEnum;
import ai.shreds.domain.enums.DomainCardBrandEnum;
import ai.shreds.domain.ports.DomainInputPortPaymentMethod;
import ai.shreds.domain.ports.DomainOutputPortPaymentMethodRepository;
import ai.shreds.domain.ports.DomainOutputPortPaymentGateway;
import ai.shreds.domain.value_objects.DomainPaymentMethodDataValue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DomainServicePaymentMethod implements DomainInputPortPaymentMethod {
    private final DomainOutputPortPaymentMethodRepository paymentMethodRepository;
    private final DomainOutputPortPaymentGateway paymentGateway;

    public DomainServicePaymentMethod(DomainOutputPortPaymentMethodRepository paymentMethodRepository,
                                     DomainOutputPortPaymentGateway paymentGateway) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentGateway = paymentGateway;
    }

    @Override
    public DomainPaymentMethod addPaymentMethod(DomainPaymentMethodRequest request) {
        if (!request.validate()) {
            throw new IllegalArgumentException("Invalid payment method request");
        }

        // Tokenize card with payment gateway
        DomainPaymentMethodDataValue tokenizedData = paymentGateway.tokenizeCard(
            request.getCardNumber(),
            request.getExpiryMonth(),
            request.getExpiryYear(),
            request.getCvv()
        );

        UUID accountUUID = UUID.fromString(request.getAccountId());
        UUID paymentMethodId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // Check if this will be the first (default) payment method for the account
        List<DomainEntityPaymentMethod> existingMethods = paymentMethodRepository.findByAccountId(accountUUID);
        boolean isDefault = existingMethods.isEmpty();

        // Create new payment method entity
        DomainEntityPaymentMethod paymentMethodEntity = new DomainEntityPaymentMethod(
            paymentMethodId,
            accountUUID,
            tokenizedData.getPaymentType(),
            tokenizedData.getToken(),
            tokenizedData.getLastFourDigits(),
            tokenizedData.getExpiryMonth(),
            tokenizedData.getExpiryYear(),
            tokenizedData.getCardBrand(),
            request.getBillingAddress(),
            isDefault,
            true, // active by default
            now,
            now
        );

        DomainEntityPaymentMethod saved = paymentMethodRepository.save(paymentMethodEntity);
        return saved.toDomainModel();
    }

    @Override
    public DomainPaymentMethod activatePaymentMethod(String paymentMethodId) {
        UUID paymentMethodUUID = UUID.fromString(paymentMethodId);
        DomainEntityPaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodUUID);
        
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method not found: " + paymentMethodId);
        }

        paymentMethod.activate();
        DomainEntityPaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        return saved.toDomainModel();
    }

    @Override
    public void deactivatePaymentMethod(String paymentMethodId) {
        UUID paymentMethodUUID = UUID.fromString(paymentMethodId);
        DomainEntityPaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodUUID);
        
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method not found: " + paymentMethodId);
        }

        paymentMethod.deactivate();
        paymentMethodRepository.save(paymentMethod);
    }

    @Override
    public void setDefaultPaymentMethod(String paymentMethodId) {
        UUID paymentMethodUUID = UUID.fromString(paymentMethodId);
        DomainEntityPaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodUUID);
        
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method not found: " + paymentMethodId);
        }

        // First, unset the previous default
        unsetPreviousDefault(paymentMethod.getAccountId());
        
        // Then set the new default
        paymentMethod.setAsDefault();
        paymentMethodRepository.save(paymentMethod);
    }

    private void unsetPreviousDefault(UUID accountId) {
        DomainEntityPaymentMethod currentDefault = paymentMethodRepository.findDefaultByAccountId(accountId);
        if (currentDefault != null) {
            currentDefault.unsetDefault();
            paymentMethodRepository.save(currentDefault);
        }
    }
}