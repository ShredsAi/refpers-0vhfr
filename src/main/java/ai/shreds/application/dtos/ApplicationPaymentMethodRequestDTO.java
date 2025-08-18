package ai.shreds.application.dtos;

import ai.shreds.domain.dtos.DomainPaymentMethodRequest;
import ai.shreds.domain.value_objects.DomainAddressValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationPaymentMethodRequestDTO {
    private String accountId;
    private String cardNumber;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String cvv;
    private String billingAddressLine1;
    private String billingAddressLine2;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;

    public DomainPaymentMethodRequest toDomainRequest() {
        DomainAddressValue address = new DomainAddressValue(
            billingAddressLine1,
            billingAddressLine2,
            billingCity,
            billingState,
            billingPostalCode,
            billingCountry
        );

        return new DomainPaymentMethodRequest(
            accountId,
            cardNumber,
            expiryMonth,
            expiryYear,
            cvv,
            address
        );
    }
}