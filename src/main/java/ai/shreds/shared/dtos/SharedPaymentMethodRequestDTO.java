package ai.shreds.shared.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import ai.shreds.application.dtos.ApplicationPaymentMethodRequestDTO;

/**
 * Shared DTO representing a payment method request.
 * Contains payment method and billing address details for processing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedPaymentMethodRequestDTO {
    
    @NotBlank(message = "Account ID is required")
    private String accountId;
    
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be 13-19 digits")
    private String cardNumber;
    
    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    private Integer expiryMonth;
    
    @NotNull(message = "Expiry year is required")
    @Min(value = 2024, message = "Expiry year must be in the future")
    private Integer expiryYear;
    
    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3-4 digits")
    private String cvv;
    
    @NotBlank(message = "Billing address line 1 is required")
    @Size(max = 255, message = "Billing address line 1 cannot exceed 255 characters")
    private String billingAddressLine1;
    
    @Size(max = 255, message = "Billing address line 2 cannot exceed 255 characters")
    private String billingAddressLine2;
    
    @NotBlank(message = "Billing city is required")
    @Size(max = 100, message = "Billing city cannot exceed 100 characters")
    private String billingCity;
    
    @NotBlank(message = "Billing state is required")
    @Size(max = 100, message = "Billing state cannot exceed 100 characters")
    private String billingState;
    
    @NotBlank(message = "Billing postal code is required")
    @Size(max = 20, message = "Billing postal code cannot exceed 20 characters")
    private String billingPostalCode;
    
    @NotBlank(message = "Billing country is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Billing country must be a valid ISO 3166-1 alpha-2 country code")
    private String billingCountry;

    /**
     * Converts this shared DTO to an application layer DTO.
     *
     * @return The ApplicationPaymentMethodRequestDTO with values from this shared DTO
     */
    public ApplicationPaymentMethodRequestDTO toApplicationDTO() {
        return ApplicationPaymentMethodRequestDTO.builder()
                .accountId(this.accountId)
                .cardNumber(this.cardNumber)
                .expiryMonth(this.expiryMonth)
                .expiryYear(this.expiryYear)
                .cvv(this.cvv)
                .billingAddressLine1(this.billingAddressLine1)
                .billingAddressLine2(this.billingAddressLine2)
                .billingCity(this.billingCity)
                .billingState(this.billingState)
                .billingPostalCode(this.billingPostalCode)
                .billingCountry(this.billingCountry)
                .build();
    }
}