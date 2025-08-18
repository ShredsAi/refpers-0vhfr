package ai.shreds.shared.dtos;

import ai.shreds.application.dtos.ApplicationTransactionDTO;
import ai.shreds.shared.value_objects.SharedMoneyValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Shared DTO representing a transaction record.
 * Contains complete transaction information for historical display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedTransactionDTO {
    
    @NotBlank(message = "Transaction ID cannot be blank")
    private String transactionId;
    
    @NotNull(message = "Transaction amount cannot be null")
    private SharedMoneyValue amount;
    
    @NotBlank(message = "Transaction type cannot be blank")
    @Pattern(regexp = "^(CREDIT|DEBIT)$", message = "Transaction type must be either CREDIT or DEBIT")
    private String type;
    
    @NotBlank(message = "Transaction description cannot be blank")
    private String description;
    
    private String reference;
    
    @NotBlank(message = "Transaction timestamp cannot be blank")
    private String timestamp;

    /**
     * Creates a shared DTO from an application layer DTO.
     *
     * @param dto The application layer DTO to convert from
     * @return A new SharedTransactionDTO with values from the application DTO
     */
    public static SharedTransactionDTO fromApplicationDTO(ApplicationTransactionDTO dto) {
        return SharedTransactionDTO.builder()
                .transactionId(dto.getTransactionId())
                .amount(SharedMoneyValue.fromApplicationMoneyValue(dto.getAmount()))
                .type(dto.getType())
                .description(dto.getDescription())
                .reference(dto.getReference())
                .timestamp(dto.getTimestamp())
                .build();
    }
}