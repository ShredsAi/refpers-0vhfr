package ai.shreds.shared.value_objects;

import ai.shreds.application.dtos.ApplicationPaginationParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Shared value object representing pagination parameters for query operations.
 * Contains page number, page size, and optional date filtering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedPaginationParams {
    
    @NotNull(message = "Page number cannot be null")
    @Min(value = 0, message = "Page number must be non-negative")
    @Builder.Default
    private Integer page = 0;
    
    @NotNull(message = "Page size cannot be null")
    @Min(value = 1, message = "Page size must be positive")
    @Builder.Default
    private Integer size = 20;
    
    private String fromDate;
    
    private String toDate;

    /**
     * Converts this shared value object to an application layer value object.
     *
     * @return The ApplicationPaginationParams with values from this shared value object
     */
    public ApplicationPaginationParams toApplicationParams() {
        return ApplicationPaginationParams.builder()
                .page(this.page)
                .size(this.size)
                .fromDate(this.fromDate)
                .toDate(this.toDate)
                .build();
    }
    
    /**
     * Creates a shared value object from an application layer value object.
     *
     * @param params The application layer value object to convert from
     * @return A new SharedPaginationParams with values from the application value object
     */
    public static SharedPaginationParams fromApplicationParams(ApplicationPaginationParams params) {
        if (params == null) {
            return SharedPaginationParams.builder().build();
        }
        return SharedPaginationParams.builder()
                .page(params.getPage())
                .size(params.getSize())
                .fromDate(params.getFromDate())
                .toDate(params.getToDate())
                .build();
    }
    
    /**
     * Creates pagination parameters with default values.
     *
     * @return A new SharedPaginationParams with page=0 and size=20
     */
    public static SharedPaginationParams defaultParams() {
        return SharedPaginationParams.builder().build();
    }
    
    /**
     * Creates pagination parameters with specified page and size.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @return A new SharedPaginationParams instance
     */
    public static SharedPaginationParams of(Integer page, Integer size) {
        return SharedPaginationParams.builder()
                .page(page)
                .size(size)
                .build();
    }
    
    /**
     * Checks if date filtering is enabled.
     *
     * @return true if either fromDate or toDate is specified
     */
    public boolean hasDateFilter() {
        return fromDate != null || toDate != null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SharedPaginationParams that = (SharedPaginationParams) obj;
        return Objects.equals(page, that.page) &&
               Objects.equals(size, that.size) &&
               Objects.equals(fromDate, that.fromDate) &&
               Objects.equals(toDate, that.toDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(page, size, fromDate, toDate);
    }
}
