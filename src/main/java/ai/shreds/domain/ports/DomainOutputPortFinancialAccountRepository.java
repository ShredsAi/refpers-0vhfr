package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityFinancialAccount;
import java.util.UUID;

public interface DomainOutputPortFinancialAccountRepository {
    DomainEntityFinancialAccount save(DomainEntityFinancialAccount account);
    DomainEntityFinancialAccount findByAccountId(UUID accountId);
    boolean existsByAccountId(UUID accountId);
}