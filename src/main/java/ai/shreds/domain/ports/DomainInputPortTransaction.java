package ai.shreds.domain.ports;

import ai.shreds.domain.dtos.DomainTransaction;
import ai.shreds.domain.dtos.DomainTransactionRequest;
import ai.shreds.domain.value_objects.DomainPaginationParams;
import java.util.List;

public interface DomainInputPortTransaction {
    DomainTransaction processTransaction(String accountId, DomainTransactionRequest request);
    List<DomainTransaction> getTransactionHistory(String accountId, DomainPaginationParams params);
    Integer getTransactionCount(String accountId, DomainPaginationParams params);
}