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
public class ApplicationPaymentMethodResponseDTO {
    private String paymentMethodId;
    private String lastFourDigits;
    private String cardBrand;
    private Boolean isDefault;
    
    public static ApplicationPaymentMethodResponseDTO fromDomainPaymentMethod(DomainPaymentMethod paymentMethod) {
        if (Objects.isNull(paymentMethod)) {
            return null;
        }
        return ApplicationPaymentMethodResponseDTO.builder()
            .paymentMethodId(paymentMethod.getPaymentMethodId())
            .lastFourDigits(paymentMethod.getPaymentData().getLastFourDigits())
            .cardBrand(paymentMethod.getPaymentData().getCardBrand().name())
            .isDefault(paymentMethod.isDefault())
            .build();
    }
}