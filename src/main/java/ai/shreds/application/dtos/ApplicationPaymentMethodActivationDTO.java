package ai.shreds.application.dtos;

import ai.shreds.domain.dtos.DomainPaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationPaymentMethodActivationDTO {
    private String paymentMethodId;
    private Boolean isActive;
    private String activatedAt;
    
    public static ApplicationPaymentMethodActivationDTO fromDomainPaymentMethod(DomainPaymentMethod paymentMethod) {
        if (Objects.isNull(paymentMethod)) {
            return null;
        }
        return ApplicationPaymentMethodActivationDTO.builder()
            .paymentMethodId(paymentMethod.getPaymentMethodId())
            .isActive(paymentMethod.getIsActive())
            .activatedAt(java.time.Instant.now().toString())
            .build();
    }
}