package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityTransaction;
import ai.shreds.domain.value_objects.DomainPaginationParams;
import java.util.List;
import java.util.UUID;

public interface DomainOutputPortTransactionRepository {
    DomainEntityTransaction save(DomainEntityTransaction transaction);
    List<DomainEntityTransaction> findByFinancialAccountId(UUID financialAccountId, DomainPaginationParams params);
    Integer countByFinancialAccountId(UUID financialAccountId, DomainPaginationParams params);
} 