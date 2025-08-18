package ai.shreds.application.ports;

import ai.shreds.application.dtos.ApplicationTransactionRequestDTO;
import ai.shreds.application.dtos.ApplicationTransactionResponseDTO;
import ai.shreds.application.dtos.ApplicationTransactionHistoryDTO;
import ai.shreds.application.dtos.ApplicationPaginationParams;

public interface ApplicationInputPortTransaction {
    ApplicationTransactionResponseDTO processTransaction(String accountId, ApplicationTransactionRequestDTO request);

    ApplicationTransactionHistoryDTO getTransactionHistory(String accountId, ApplicationPaginationParams params);
}
