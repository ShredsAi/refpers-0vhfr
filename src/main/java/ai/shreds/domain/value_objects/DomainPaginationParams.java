package ai.shreds.domain.value_objects;

import java.time.LocalDateTime;
import java.util.Objects;

public class DomainPaginationParams {
    private final Integer page;
    private final Integer size;
    private final LocalDateTime fromDate;
    private final LocalDateTime toDate;

    public DomainPaginationParams(Integer page, Integer size, LocalDateTime fromDate, LocalDateTime toDate) {
        this.page = page;
        this.size = size;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public LocalDateTime getToDate() {
        return toDate;
    }

    public boolean validate() {
        if (page == null || page < 0) return false;
        if (size == null || size <= 0) return false;
        if (fromDate == null || toDate == null) return false;
        if (fromDate.isAfter(toDate)) return false;
        return true;
    }

    /**
     * Generates a SQL WHERE clause fragment for timestamp filtering.
     */
    public String toSQL() {
        return "timestamp BETWEEN '" + fromDate + "' AND '" + toDate + "'";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainPaginationParams)) return false;
        DomainPaginationParams that = (DomainPaginationParams) o;
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