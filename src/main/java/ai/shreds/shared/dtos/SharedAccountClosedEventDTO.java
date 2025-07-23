package ai.shreds.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Shared DTO representing an account closure event.
 * Used for inter-service communication when an account is closed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedAccountClosedEventDTO {
    
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;
    
    @NotBlank(message = "Closure reason cannot be blank")
    private String reason;
    
    @NotBlank(message = "Closed at timestamp cannot be blank")
    private String closedAt;
}
