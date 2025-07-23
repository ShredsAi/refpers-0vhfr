package ai.shreds.shared.dtos;

import ai.shreds.application.dtos.ApplicationTransactionResponseDTO;
import ai.shreds.shared.value_objects.SharedMoneyValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Shared DTO representing the response after a transaction has been processed.
 * Contains the transaction ID, updated balance, and processing timestamp.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedTransactionResponseDTO {
    
    @NotBlank(message = "Transaction ID cannot be blank")
    private String transactionId;
    
    @NotNull(message = "New balance cannot be null")
    private SharedMoneyValue newBalance;
    
    @NotBlank(message = "Processed at timestamp cannot be blank")
    private String processedAt;

    /**
     * Creates a shared DTO from an application layer DTO.
     *
     * @param dto The application layer DTO to convert from
     * @return A new SharedTransactionResponseDTO with values from the application DTO
     */
    public static SharedTransactionResponseDTO fromApplicationDTO(ApplicationTransactionResponseDTO dto) {
        return SharedTransactionResponseDTO.builder()
                .transactionId(dto.getTransactionId())
                .newBalance(SharedMoneyValue.fromApplicationMoneyValue(dto.getNewBalance()))
                .processedAt(dto.getProcessedAt())
                .build();
    }
}
