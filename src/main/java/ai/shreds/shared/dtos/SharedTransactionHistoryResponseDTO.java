package ai.shreds.shared.dtos;

import ai.shreds.application.dtos.ApplicationTransactionHistoryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared DTO representing a paginated list of transactions with metadata.
 * Contains transaction history with pagination information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedTransactionHistoryResponseDTO {
    
    @Valid
    @Builder.Default
    private List<SharedTransactionDTO> transactions = Collections.emptyList();
    
    @NotNull(message = "Total count cannot be null")
    @Min(value = 0, message = "Total count must be non-negative")
    private Integer totalCount;
    
    @NotNull(message = "Page number cannot be null")
    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page;
    
    @NotNull(message = "Page size cannot be null")
    @Min(value = 1, message = "Page size must be positive")
    private Integer size;

    /**
     * Creates a shared DTO from an application layer DTO.
     *
     * @param dto The application layer DTO to convert from
     * @return A new SharedTransactionHistoryResponseDTO with values from the application DTO
     */
    public static SharedTransactionHistoryResponseDTO fromApplicationDTO(ApplicationTransactionHistoryDTO dto) {
        List<SharedTransactionDTO> sharedTransactions = dto.getTransactions() != null
                ? dto.getTransactions().stream()
                    .map(SharedTransactionDTO::fromApplicationDTO)
                    .collect(Collectors.toList())
                : Collections.emptyList();
        
        return SharedTransactionHistoryResponseDTO.builder()
                .transactions(sharedTransactions)
                .totalCount(dto.getTotalCount())
                .page(dto.getPage())
                .size(dto.getSize())
                .build();
    }
}