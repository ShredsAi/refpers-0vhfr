package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.enums.DomainCardBrandEnum;
import ai.shreds.domain.enums.DomainPaymentTypeEnum;
import ai.shreds.domain.value_objects.DomainPaymentMethodDataValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfrastructurePaymentGatewayResponseDTO {

    @JsonProperty("token")
    private String token;

    @JsonProperty("card_brand")
    private String cardBrand;

    @JsonProperty("last4")
    private String last4;

    public DomainPaymentMethodDataValue toDomainPaymentMethodData() {
        DomainCardBrandEnum cardBrandEnum = mapCardBrand(cardBrand);
        
        return new DomainPaymentMethodDataValue(
                DomainPaymentTypeEnum.CREDIT_CARD,
                token,
                last4,
                null, // expiry month will be set separately
                null, // expiry year will be set separately
                cardBrandEnum
        );
    }

    private DomainCardBrandEnum mapCardBrand(String brand) {
        if (brand == null) {
            return null;
        }
        
        return switch (brand.toUpperCase()) {
            case "VISA" -> DomainCardBrandEnum.VISA;
            case "MASTERCARD", "MASTER" -> DomainCardBrandEnum.MASTERCARD;
            case "AMEX", "AMERICAN_EXPRESS" -> DomainCardBrandEnum.AMEX;
            case "DISCOVER" -> DomainCardBrandEnum.DISCOVER;
            default -> null;
        };
    }
}