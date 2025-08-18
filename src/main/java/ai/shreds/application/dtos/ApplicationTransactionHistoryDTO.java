package ai.shreds.application.dtos;

import ai.shreds.domain.dtos.DomainTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTransactionHistoryDTO {
    private List<ApplicationTransactionDTO> transactions;
    private Integer totalCount;
    private Integer page;
    private Integer size;

    public static ApplicationTransactionHistoryDTO fromDomainTransactions(
            List<DomainTransaction> transactions,
            Integer totalCount,
            Integer page,
            Integer size) {
        if (Objects.isNull(transactions)) {
            return ApplicationTransactionHistoryDTO.builder()
                .transactions(List.of())
                .totalCount(totalCount)
                .page(page)
                .size(size)
                .build();
        }
        List<ApplicationTransactionDTO> dtos = transactions.stream()
            .map(ApplicationTransactionDTO::fromDomainTransaction)
            .collect(Collectors.toList());
        return ApplicationTransactionHistoryDTO.builder()
            .transactions(dtos)
            .totalCount(totalCount)
            .page(page)
            .size(size)
            .build();
    }
}