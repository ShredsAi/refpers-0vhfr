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
        DomainAddressValue address = DomainAddressValue.builder()
            .line1(billingAddressLine1)
            .line2(billingAddressLine2)
            .city(billingCity)
            .state(billingState)
            .postalCode(billingPostalCode)
            .country(billingCountry)
            .build();

        return DomainPaymentMethodRequest.builder()
            .accountId(accountId)
            .cardNumber(cardNumber)
            .expiryMonth(expiryMonth)
            .expiryYear(expiryYear)
            .cvv(cvv)
            .billingAddress(address)
            .build();
    }
}