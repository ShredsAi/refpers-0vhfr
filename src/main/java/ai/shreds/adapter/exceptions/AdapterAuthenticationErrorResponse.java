package ai.shreds.adapter.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard error response for authentication failures.
 * Uses JsonInclude.Include.NON_NULL to omit null fields from JSON output.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdapterAuthenticationErrorResponse {
    private String error;
    private String message;
    private String path;
    private Long timestamp;
    
    public AdapterAuthenticationErrorResponse(String error) {
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }
    
    public AdapterAuthenticationErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}