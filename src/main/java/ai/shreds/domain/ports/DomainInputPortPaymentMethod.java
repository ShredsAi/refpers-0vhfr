package ai.shreds.domain.ports;

import ai.shreds.domain.dtos.DomainPaymentMethod;
import ai.shreds.domain.dtos.DomainPaymentMethodRequest;

public interface DomainInputPortPaymentMethod {
    DomainPaymentMethod addPaymentMethod(DomainPaymentMethodRequest request);
    DomainPaymentMethod activatePaymentMethod(String paymentMethodId);
    void deactivatePaymentMethod(String paymentMethodId);
    void setDefaultPaymentMethod(String paymentMethodId);
}