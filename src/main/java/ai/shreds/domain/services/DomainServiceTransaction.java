package ai.shreds.domain.services;

import ai.shreds.domain.dtos.DomainTransaction;
import ai.shreds.domain.dtos.DomainTransactionRequest;
import ai.shreds.domain.entities.DomainEntityFinancialAccount;
import ai.shreds.domain.entities.DomainEntityTransaction;
import ai.shreds.domain.ports.DomainInputPortTransaction;
import ai.shreds.domain.ports.DomainOutputPortFinancialAccountRepository;
import ai.shreds.domain.ports.DomainOutputPortTransactionRepository;
import ai.shreds.domain.ports.DomainOutputPortCurrencyService;
import ai.shreds.domain.value_objects.DomainPaginationParams;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.domain.enums.DomainTransactionTypeEnum;
import ai.shreds.domain.exceptions.DomainAccountFrozenException;
import ai.shreds.domain.exceptions.DomainInsufficientFundsException;
import ai.shreds.domain.exceptions.DomainInvalidCurrencyException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DomainServiceTransaction implements DomainInputPortTransaction {
    private final DomainOutputPortFinancialAccountRepository accountRepository;
    private final DomainOutputPortTransactionRepository transactionRepository;
    private final DomainOutputPortCurrencyService currencyService;

    public DomainServiceTransaction(DomainOutputPortFinancialAccountRepository accountRepository,
                                   DomainOutputPortTransactionRepository transactionRepository,
                                   DomainOutputPortCurrencyService currencyService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.currencyService = currencyService;
    }

    @Override
    public DomainTransaction processTransaction(String accountId, DomainTransactionRequest request) {
        UUID accountUUID = UUID.fromString(accountId);
        DomainEntityFinancialAccount account = accountRepository.findByAccountId(accountUUID);
        
        if (account == null) {
            throw new IllegalArgumentException("Financial account not found: " + accountId);
        }

        validateTransaction(account, request);

        // Handle currency conversion if needed
        DomainMoneyValue transactionAmount = request.getAmount();
        if (!account.getBalance().getCurrency().equals(request.getAmount().getCurrency())) {
            transactionAmount = currencyService.convertCurrency(
                request.getAmount(), 
                account.getBalance().getCurrency().getCode()
            );
        }

        // Process the transaction
        DomainEntityTransaction transactionEntity;
        if (request.getType() == DomainTransactionTypeEnum.CREDIT) {
            transactionEntity = account.credit(transactionAmount, request.getDescription(), request.getReference());
        } else {
            transactionEntity = account.debit(transactionAmount, request.getDescription(), request.getReference());
        }

        // Save both account (with updated balance) and transaction
        accountRepository.save(account);
        DomainEntityTransaction savedTransaction = transactionRepository.save(transactionEntity);

        return savedTransaction.toDomainModel();
    }

    @Override
    public List<DomainTransaction> getTransactionHistory(String accountId, DomainPaginationParams params) {
        UUID accountUUID = UUID.fromString(accountId);
        DomainEntityFinancialAccount account = accountRepository.findByAccountId(accountUUID);
        
        if (account == null) {
            throw new IllegalArgumentException("Financial account not found: " + accountId);
        }

        List<DomainEntityTransaction> transactions = transactionRepository
            .findByFinancialAccountId(account.getFinancialAccountId(), params);

        return transactions.stream()
            .map(DomainEntityTransaction::toDomainModel)
            .collect(Collectors.toList());
    }

    @Override
    public Integer getTransactionCount(String accountId, DomainPaginationParams params) {
        UUID accountUUID = UUID.fromString(accountId);
        DomainEntityFinancialAccount account = accountRepository.findByAccountId(accountUUID);
        
        if (account == null) {
            throw new IllegalArgumentException("Financial account not found: " + accountId);
        }

        return transactionRepository.countByFinancialAccountId(account.getFinancialAccountId(), params);
    }

    private void validateTransaction(DomainEntityFinancialAccount account, DomainTransactionRequest request) {
        if (!request.validate()) {
            throw new IllegalArgumentException("Invalid transaction request");
        }

        if (account.getStatus() != ai.shreds.domain.enums.DomainAccountStatusEnum.ACTIVE) {
            throw new DomainAccountFrozenException(account.getAccountId().toString(), "Account is not active");
        }

        if (request.getType() == DomainTransactionTypeEnum.DEBIT) {
            if (!account.canDebit(request.getAmount())) {
                throw new DomainInsufficientFundsException(
                    account.getAccountId().toString(),
                    request.getAmount().getAmount(),
                    account.getBalance().getAmount()
                );
            }
        }
    }
}