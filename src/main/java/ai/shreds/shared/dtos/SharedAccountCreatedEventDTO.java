package ai.shreds.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Shared DTO representing an account creation event.
 * Used for inter-service communication when a new account is created.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedAccountCreatedEventDTO {
    
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;
    
    @NotBlank(message = "Customer ID cannot be blank")
    private String customerId;
    
    @NotBlank(message = "Created at timestamp cannot be blank")
    private String createdAt;
}
