package ai.shreds.application.ports;

import ai.shreds.application.dtos.ApplicationPaymentMethodRequestDTO;
import ai.shreds.application.dtos.ApplicationPaymentMethodResponseDTO;
import ai.shreds.application.dtos.ApplicationPaymentMethodActivationDTO;

public interface ApplicationInputPortPaymentMethod {
    ApplicationPaymentMethodResponseDTO addPaymentMethod(ApplicationPaymentMethodRequestDTO request);

    ApplicationPaymentMethodActivationDTO activatePaymentMethod(String paymentMethodId);
}
