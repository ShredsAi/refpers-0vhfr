package ai.shreds.application.services;

import ai.shreds.application.dtos.ApplicationTransactionRequestDTO;
import ai.shreds.application.dtos.ApplicationTransactionResponseDTO;
import ai.shreds.application.dtos.ApplicationTransactionHistoryDTO;
import ai.shreds.application.dtos.ApplicationPaginationParams;
import ai.shreds.application.ports.ApplicationInputPortTransaction;
import ai.shreds.application.ports.ApplicationOutputPortCache;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.domain.ports.DomainInputPortTransaction;
import ai.shreds.domain.ports.DomainInputPortFinancialAccount;
import ai.shreds.domain.dtos.DomainTransaction;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.application.exceptions.ApplicationServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationTransactionService implements ApplicationInputPortTransaction {
    
    private final DomainInputPortTransaction domainTransactionPort;
    private final DomainInputPortFinancialAccount domainFinancialAccountPort;
    private final ApplicationOutputPortCache cachePort;
    private final ApplicationOutputPortEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public ApplicationTransactionResponseDTO processTransaction(String accountId, ApplicationTransactionRequestDTO request) {
        log.info("Processing transaction for accountId: {} with amount: {} {}", 
            accountId, request.getAmount().getAmount(), request.getAmount().getCurrency());
        
        try {
            // Process the transaction through domain layer
            var domainRequest = request.toDomainRequest();
            DomainTransaction domainTransaction = domainTransactionPort.processTransaction(accountId, domainRequest);
            
            // Get updated balance from domain
            DomainMoneyValue newBalance = domainFinancialAccountPort.getBalance(accountId);
            
            // Create response DTO
            ApplicationTransactionResponseDTO response = ApplicationTransactionResponseDTO.fromDomainTransaction(domainTransaction, newBalance);
            
            // Invalidate cache since balance changed
            cachePort.invalidateBalance(accountId);
            
            // Publish transaction processed event
            var transactionDTO = ai.shreds.application.dtos.ApplicationTransactionDTO.fromDomainTransaction(domainTransaction);
            eventPublisher.publishTransactionProcessed(transactionDTO);
            
            log.info("Successfully processed transaction {} for accountId: {}", 
                domainTransaction.getTransactionId(), accountId);
            
            return response;
            
        } catch (Exception e) {
            log.error("Failed to process transaction for accountId: {}", accountId, e);
            throw new ApplicationServiceException("Failed to process transaction for accountId: " + accountId, 
                "TRANSACTION_PROCESSING_FAILED", e);
        }
    }
    
    @Override
    public ApplicationTransactionHistoryDTO getTransactionHistory(String accountId, ApplicationPaginationParams params) {
        log.debug("Retrieving transaction history for accountId: {} with params: {}", accountId, params);
        
        try {
            // Convert to domain pagination params
            var domainParams = params.toDomainPaginationParams();
            
            // Get transactions from domain layer
            List<DomainTransaction> domainTransactions = domainTransactionPort.getTransactionHistory(accountId, domainParams);
            
            // Get total count
            Integer totalCount = domainTransactionPort.getTransactionCount(accountId, domainParams);
            
            // Convert to application DTOs
            List<ai.shreds.application.dtos.ApplicationTransactionDTO> transactionDTOs = domainTransactions.stream()
                .map(ai.shreds.application.dtos.ApplicationTransactionDTO::fromDomainTransaction)
                .collect(Collectors.toList());
            
            // Create history DTO
            ApplicationTransactionHistoryDTO historyDTO = ApplicationTransactionHistoryDTO.fromDomainTransactions(
                domainTransactions, totalCount, params.getPage(), params.getSize());
            
            log.debug("Successfully retrieved {} transactions for accountId: {}", transactionDTOs.size(), accountId);
            return historyDTO;
            
        } catch (Exception e) {
            log.error("Failed to retrieve transaction history for accountId: {}", accountId, e);
            throw new ApplicationServiceException("Failed to retrieve transaction history for accountId: " + accountId, 
                "TRANSACTION_HISTORY_RETRIEVAL_FAILED", e);
        }
    }
}