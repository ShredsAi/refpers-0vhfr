package ai.shreds.shared.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import ai.shreds.shared.value_objects.SharedMoneyValue;
import ai.shreds.application.dtos.ApplicationTransactionRequestDTO;

/**
 * Shared DTO representing a transaction request.
 * Contains transaction details including amount, type, description and reference.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedTransactionRequestDTO {
    
    @Valid
    @NotNull(message = "Amount is required")
    private SharedMoneyValue amount;
    
    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "CREDIT|DEBIT", message = "Transaction type must be either CREDIT or DEBIT")
    private String type;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String reference;

    /**
     * Converts this shared DTO to an application layer DTO.
     *
     * @return The ApplicationTransactionRequestDTO with values from this shared DTO
     */
    public ApplicationTransactionRequestDTO toApplicationDTO() {
        return ApplicationTransactionRequestDTO.builder()
                .amount(this.amount.toApplicationMoneyValue())
                .type(this.type)
                .description(this.description)
                .reference(this.reference)
                .build();
    }
}