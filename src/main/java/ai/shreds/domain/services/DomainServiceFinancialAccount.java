package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainEntityFinancialAccount;
import ai.shreds.domain.ports.DomainInputPortFinancialAccount;
import ai.shreds.domain.ports.DomainOutputPortFinancialAccountRepository;
import ai.shreds.domain.services.DomainFinancialAccountFactory;
import ai.shreds.domain.value_objects.DomainMoneyValue;

import java.util.UUID;

public class DomainServiceFinancialAccount implements DomainInputPortFinancialAccount {
    private final DomainOutputPortFinancialAccountRepository accountRepository;
    private final DomainFinancialAccountFactory accountFactory;

    public DomainServiceFinancialAccount(DomainOutputPortFinancialAccountRepository accountRepository,
                                        DomainFinancialAccountFactory accountFactory) {
        this.accountRepository = accountRepository;
        this.accountFactory = accountFactory;
    }

    @Override
    public void createFinancialAccount(String accountId, String currency) {
        UUID accountUUID = UUID.fromString(accountId);
        
        // Check if account already exists
        if (accountRepository.existsByAccountId(accountUUID)) {
            throw new IllegalStateException("Financial account already exists for account: " + accountId);
        }
        
        // Create new financial account with zero balance
        DomainEntityFinancialAccount newAccount = accountFactory.createNewAccount(accountUUID, currency);
        accountRepository.save(newAccount);
    }

    @Override
    public DomainEntityFinancialAccount getFinancialAccount(String accountId) {
        UUID accountUUID = UUID.fromString(accountId);
        DomainEntityFinancialAccount account = accountRepository.findByAccountId(accountUUID);
        if (account == null) {
            throw new IllegalArgumentException("Financial account not found for account: " + accountId);
        }
        return account;
    }

    @Override
    public DomainMoneyValue getBalance(String accountId) {
        DomainEntityFinancialAccount account = getFinancialAccount(accountId);
        return account.getBalance();
    }

    @Override
    public void suspendAccount(String accountId, String reason) {
        DomainEntityFinancialAccount account = getFinancialAccount(accountId);
        account.freeze();
        accountRepository.save(account);
    }

    @Override
    public void closeAccount(String accountId, String reason) {
        DomainEntityFinancialAccount account = getFinancialAccount(accountId);
        account.close();
        accountRepository.save(account);
    }
}