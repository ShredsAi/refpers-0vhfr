package ai.shreds.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Shared DTO representing an error response.
 * Used to standardize error reporting across the API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedErrorResponseDTO {
    
    @NotBlank(message = "Error code cannot be blank")
    private String error;
    
    @NotBlank(message = "Error message cannot be blank")
    private String message;
    
    @NotBlank(message = "Timestamp cannot be blank")
    private String timestamp;
    
    @NotBlank(message = "Request path cannot be blank")
    private String path;
    
    /**
     * Creates a standard error response for API exceptions.
     *
     * @param error The error code or type
     * @param message The detailed error message
     * @param path The request path where the error occurred
     * @return A new SharedErrorResponseDTO with current timestamp
     */
    public static SharedErrorResponseDTO create(String error, String message, String path) {
        return SharedErrorResponseDTO.builder()
                .error(error)
                .message(message)
                .timestamp(java.time.Instant.now().toString())
                .path(path)
                .build();
    }
}