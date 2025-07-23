package ai.shreds.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Shared DTO representing an account suspension event.
 * Used for inter-service communication when an account is suspended.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedAccountSuspendedEventDTO {
    
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;
    
    @NotBlank(message = "Suspension reason cannot be blank")
    private String reason;
    
    @NotBlank(message = "Suspended at timestamp cannot be blank")
    private String suspendedAt;
}
