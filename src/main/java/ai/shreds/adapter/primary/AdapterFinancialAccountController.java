package ai.shreds.adapter.primary;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ai.shreds.application.dtos.ApplicationBalanceDTO;
import ai.shreds.application.dtos.ApplicationPaginationParams;
import ai.shreds.application.dtos.ApplicationTransactionHistoryDTO;
import ai.shreds.application.dtos.ApplicationTransactionRequestDTO;
import ai.shreds.application.dtos.ApplicationTransactionResponseDTO;
import ai.shreds.application.ports.ApplicationInputPortFinancialAccount;
import ai.shreds.application.ports.ApplicationInputPortTransaction;
import ai.shreds.shared.dtos.SharedBalanceResponseDTO;
import ai.shreds.shared.dtos.SharedTransactionHistoryResponseDTO;
import ai.shreds.shared.dtos.SharedTransactionRequestDTO;
import ai.shreds.shared.dtos.SharedTransactionResponseDTO;
import ai.shreds.shared.value_objects.SharedPaginationParams;

@RestController
@RequestMapping("/financial-accounts")
public class AdapterFinancialAccountController {

    private final ApplicationInputPortFinancialAccount financialAccountService;
    private final ApplicationInputPortTransaction transactionService;

    public AdapterFinancialAccountController(ApplicationInputPortFinancialAccount financialAccountService,
                                             ApplicationInputPortTransaction transactionService) {
        this.financialAccountService = financialAccountService;
        this.transactionService = transactionService;
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<SharedBalanceResponseDTO> getBalance(@PathVariable("id") UUID accountId) {
        ApplicationBalanceDTO appDto = financialAccountService.getAccountBalance(accountId.toString());
        SharedBalanceResponseDTO response = SharedBalanceResponseDTO.fromApplicationDTO(appDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/transactions")
    public ResponseEntity<SharedTransactionResponseDTO> processTransaction(
            @PathVariable("id") UUID accountId,
            @Valid @RequestBody SharedTransactionRequestDTO request) {
        ApplicationTransactionRequestDTO appRequest = request.toApplicationDTO();
        ApplicationTransactionResponseDTO appResponse = transactionService.processTransaction(accountId.toString(), appRequest);
        SharedTransactionResponseDTO response = SharedTransactionResponseDTO.fromApplicationDTO(appResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/transactions/history")
    public ResponseEntity<SharedTransactionHistoryResponseDTO> getTransactionHistory(
            @PathVariable("id") UUID accountId,
            @Valid @ModelAttribute SharedPaginationParams params) {
        ApplicationPaginationParams appParams = params.toApplicationParams();
        ApplicationTransactionHistoryDTO appHistory = transactionService.getTransactionHistory(accountId.toString(), appParams);
        SharedTransactionHistoryResponseDTO response = SharedTransactionHistoryResponseDTO.fromApplicationDTO(appHistory);
        return ResponseEntity.ok(response);
    }
}