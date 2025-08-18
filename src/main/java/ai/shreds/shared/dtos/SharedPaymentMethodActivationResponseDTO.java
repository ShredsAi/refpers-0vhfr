package ai.shreds.shared.dtos;

import ai.shreds.application.dtos.ApplicationPaymentMethodActivationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Shared DTO representing the response after a payment method activation.
 * Contains activation status and timestamp information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedPaymentMethodActivationResponseDTO {
    
    @NotBlank(message = "Payment method ID cannot be blank")
    private String paymentMethodId;
    
    @NotNull(message = "Is active flag cannot be null")
    private Boolean isActive;
    
    @NotBlank(message = "Activated at timestamp cannot be blank")
    private String activatedAt;

    /**
     * Creates a shared DTO from an application layer DTO.
     *
     * @param dto The application layer DTO to convert from
     * @return A new SharedPaymentMethodActivationResponseDTO with values from the application DTO
     */
    public static SharedPaymentMethodActivationResponseDTO fromApplicationDTO(ApplicationPaymentMethodActivationDTO dto) {
        return SharedPaymentMethodActivationResponseDTO.builder()
                .paymentMethodId(dto.getPaymentMethodId())
                .isActive(dto.getIsActive())
                .activatedAt(dto.getActivatedAt())
                .build();
    }
}