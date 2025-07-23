package ai.shreds.application.dtos;

import ai.shreds.domain.dtos.DomainTransactionRequest;
import ai.shreds.domain.enums.DomainTransactionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTransactionRequestDTO {
    
    @Valid
    @NotNull(message = "Amount is required")
    private ApplicationMoneyValue amount;
    
    @NotBlank(message = "Transaction type is required")
    private String type;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String reference;
    
    public DomainTransactionRequest toDomainRequest() {
        return DomainTransactionRequest.builder()
            .amount(amount.toDomainMoneyValue())
            .type(DomainTransactionTypeEnum.valueOf(type.toUpperCase()))
            .description(description)
            .reference(reference)
            .build();
    }
}