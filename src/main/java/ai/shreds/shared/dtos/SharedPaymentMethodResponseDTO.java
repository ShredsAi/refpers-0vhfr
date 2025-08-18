package ai.shreds.shared.dtos;

import ai.shreds.application.dtos.ApplicationPaymentMethodResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Shared DTO representing the response after a payment method has been added.
 * Contains payment method details without sensitive information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedPaymentMethodResponseDTO {
    
    @NotBlank(message = "Payment method ID cannot be blank")
    private String paymentMethodId;
    
    @NotBlank(message = "Last four digits cannot be blank")
    @Pattern(regexp = "^\\d{4}$", message = "Last four digits must contain exactly 4 digits")
    private String lastFourDigits;
    
    @NotBlank(message = "Card brand cannot be blank")
    private String cardBrand;
    
    @NotNull(message = "Is default flag cannot be null")
    private Boolean isDefault;

    /**
     * Creates a shared DTO from an application layer DTO.
     *
     * @param dto The application layer DTO to convert from
     * @return A new SharedPaymentMethodResponseDTO with values from the application DTO
     */
    public static SharedPaymentMethodResponseDTO fromApplicationDTO(ApplicationPaymentMethodResponseDTO dto) {
        return SharedPaymentMethodResponseDTO.builder()
                .paymentMethodId(dto.getPaymentMethodId())
                .lastFourDigits(dto.getLastFourDigits())
                .cardBrand(dto.getCardBrand())
                .isDefault(dto.getIsDefault())
                .build();
    }
}
