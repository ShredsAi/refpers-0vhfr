package ai.shreds.shared.dtos;

import ai.shreds.application.dtos.ApplicationBalanceDTO;
import ai.shreds.shared.value_objects.SharedMoneyValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Shared DTO representing the response for balance inquiries.
 * Contains the current balance as a money value and the timestamp when it was last updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedBalanceResponseDTO {
    
    @NotNull(message = "Balance cannot be null")
    private SharedMoneyValue balance;
    
    @NotNull(message = "Last updated timestamp cannot be null")
    private String lastUpdated;

    /**
     * Converts this shared DTO to an application layer DTO.
     *
     * @return The ApplicationBalanceDTO with values from this shared DTO
     */
    public ApplicationBalanceDTO toApplicationDTO() {
        return ApplicationBalanceDTO.builder()
                .balance(this.balance.toApplicationMoneyValue())
                .lastUpdated(this.lastUpdated)
                .build();
    }
    
    /**
     * Creates a shared DTO from an application layer DTO.
     *
     * @param dto The application layer DTO to convert from
     * @return A new SharedBalanceResponseDTO with values from the application DTO
     */
    public static SharedBalanceResponseDTO fromApplicationDTO(ApplicationBalanceDTO dto) {
        return SharedBalanceResponseDTO.builder()
                .balance(SharedMoneyValue.fromApplicationMoneyValue(dto.getBalance()))
                .lastUpdated(dto.getLastUpdated())
                .build();
    }
}
