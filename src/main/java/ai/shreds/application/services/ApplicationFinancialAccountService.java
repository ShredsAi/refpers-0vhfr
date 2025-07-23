package ai.shreds.application.services;

import ai.shreds.application.dtos.ApplicationBalanceDTO;
import ai.shreds.application.ports.ApplicationInputPortFinancialAccount;
import ai.shreds.application.ports.ApplicationOutputPortCache;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.domain.ports.DomainInputPortFinancialAccount;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.application.exceptions.ApplicationServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationFinancialAccountService implements ApplicationInputPortFinancialAccount {
    
    private final DomainInputPortFinancialAccount domainFinancialAccountPort;
    private final ApplicationOutputPortCache cachePort;
    private final ApplicationOutputPortEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public void createFinancialAccount(String accountId) {
        log.info("Creating financial account for accountId: {}", accountId);
        
        try {
            // Create financial account with default currency USD
            domainFinancialAccountPort.createFinancialAccount(accountId, "USD");
            
            // Publish event for account creation
            eventPublisher.publishFinancialAccountCreated(accountId);
            
            log.info("Successfully created financial account for accountId: {}", accountId);
        } catch (Exception e) {
            log.error("Failed to create financial account for accountId: {}", accountId, e);
            throw new ApplicationServiceException("Failed to create financial account for accountId: " + accountId, "ACCOUNT_CREATION_FAILED", e);
        }
    }
    
    @Override
    public ApplicationBalanceDTO getAccountBalance(String accountId) {
        log.debug("Retrieving balance for accountId: {}", accountId);
        
        try {
            // First try to get from cache
            var cachedBalance = cachePort.getBalance(accountId);
            if (cachedBalance != null) {
                log.debug("Balance found in cache for accountId: {}", accountId);
                return ApplicationBalanceDTO.builder()
                    .balance(cachedBalance)
                    .lastUpdated(Instant.now().toString())
                    .build();
            }
            
            // Get from domain layer
            DomainMoneyValue domainBalance = domainFinancialAccountPort.getBalance(accountId);
            Instant lastUpdated = Instant.now();
            
            // Create application DTO
            ApplicationBalanceDTO balanceDTO = ApplicationBalanceDTO.fromDomainBalance(domainBalance, lastUpdated);
            
            // Cache the balance
            if (balanceDTO != null && balanceDTO.getBalance() != null) {
                cachePort.putBalance(accountId, balanceDTO.getBalance());
            }
            
            log.debug("Successfully retrieved balance for accountId: {}", accountId);
            return balanceDTO;
            
        } catch (Exception e) {
            log.error("Failed to retrieve balance for accountId: {}", accountId, e);
            throw new ApplicationServiceException("Failed to retrieve balance for accountId: " + accountId, "BALANCE_RETRIEVAL_FAILED", e);
        }
    }
    
    @Override
    @Transactional
    public void suspendAccount(String accountId, String reason) {
        log.info("Suspending account: {} with reason: {}", accountId, reason);
        
        try {
            domainFinancialAccountPort.suspendAccount(accountId, reason);
            
            // Invalidate cache
            cachePort.invalidateBalance(accountId);
            
            log.info("Successfully suspended account: {}", accountId);
        } catch (Exception e) {
            log.error("Failed to suspend account: {}", accountId, e);
            throw new ApplicationServiceException("Failed to suspend account: " + accountId, "ACCOUNT_SUSPENSION_FAILED", e);
        }
    }
    
    @Override
    @Transactional
    public void closeAccount(String accountId, String reason) {
        log.info("Closing account: {} with reason: {}", accountId, reason);
        
        try {
            domainFinancialAccountPort.closeAccount(accountId, reason);
            
            // Invalidate cache
            cachePort.invalidateBalance(accountId);
            
            log.info("Successfully closed account: {}", accountId);
        } catch (Exception e) {
            log.error("Failed to close account: {}", accountId, e);
            throw new ApplicationServiceException("Failed to close account: " + accountId, "ACCOUNT_CLOSURE_FAILED", e);
        }
    }
}