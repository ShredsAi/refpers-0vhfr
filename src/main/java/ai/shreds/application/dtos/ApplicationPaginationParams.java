package ai.shreds.application.dtos;

import ai.shreds.domain.value_objects.DomainPaginationParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationPaginationParams {
    private Integer page;
    private Integer size;
    private String fromDate;
    private String toDate;

    public DomainPaginationParams toDomainPaginationParams() {
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;
        if (fromDate != null && !fromDate.isEmpty()) {
            fromDateTime = LocalDateTime.ofInstant(Instant.parse(fromDate), ZoneId.systemDefault());
        }
        if (toDate != null && !toDate.isEmpty()) {
            toDateTime = LocalDateTime.ofInstant(Instant.parse(toDate), ZoneId.systemDefault());
        }
        return new DomainPaginationParams(
            page != null ? page : 0,
            size != null ? size : 20,
            fromDateTime,
            toDateTime
        );
    }
}