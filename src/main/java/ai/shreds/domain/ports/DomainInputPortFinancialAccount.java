package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityFinancialAccount;
import ai.shreds.domain.value_objects.DomainMoneyValue;

public interface DomainInputPortFinancialAccount {
    void createFinancialAccount(String accountId, String currency);
    DomainEntityFinancialAccount getFinancialAccount(String accountId);
    DomainMoneyValue getBalance(String accountId);
    void suspendAccount(String accountId, String reason);
    void closeAccount(String accountId, String reason);
}