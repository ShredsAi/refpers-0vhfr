package ai.shreds.domain.ports;

import ai.shreds.domain.value_objects.DomainPaymentMethodDataValue;

public interface DomainOutputPortPaymentGateway {
    DomainPaymentMethodDataValue tokenizeCard(String cardNumber, Integer expiryMonth, Integer expiryYear, String cvv);
    boolean validateToken(String token);
}