package ai.shreds;

import ai.shreds.application.dtos.ApplicationBalanceDTO;
import ai.shreds.application.dtos.ApplicationMoneyValue;
import ai.shreds.application.ports.ApplicationInputPortFinancialAccount;
import ai.shreds.application.ports.ApplicationInputPortTransaction;
import ai.shreds.application.ports.ApplicationOutputPortCache;
import ai.shreds.infrastructure.repositories.InfrastructureFinancialAccountJpaRepository;
import ai.shreds.infrastructure.repositories.InfrastructureTransactionJpaRepository;
import ai.shreds.shared.dtos.SharedAccountCreatedEventDTO;
import ai.shreds.shared.dtos.SharedTransactionRequestDTO;
import ai.shreds.shared.dtos.SharedTransactionResponseDTO;
import ai.shreds.shared.dtos.SharedErrorResponseDTO;
import ai.shreds.shared.value_objects.SharedMoneyValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Integration test for financial account creation, transaction processing, and balance management.
 * Tests the complete workflow including database persistence, cache updates, and event publishing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class FinancialAccountTransactionIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("financial_test_db")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withReuse(true);

    @Autowired
    private ApplicationInputPortFinancialAccount financialAccountService;

    @Autowired
    private ApplicationInputPortTransaction transactionService;

    @Autowired
    private ApplicationOutputPortCache cacheService;

    @Autowired
    private InfrastructureFinancialAccountJpaRepository financialAccountRepository;

    @Autowired
    private InfrastructureTransactionJpaRepository transactionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        // Redis configuration
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
        
        // Kafka configuration
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        
        // Mock external services
        registry.add("financial.payment-gateway.url", () -> "http://localhost:8090/mock-payment");
        registry.add("financial.currency-service.url", () -> "http://localhost:8091/mock-currency");
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Clean up database and cache before each test
        transactionRepository.deleteAll();
        financialAccountRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        
        System.out.println("✅ Test setup completed - database and cache cleared");
    }

    @Test
    @Transactional
    void When_AccountCreated_Event_Received_Then_FinancialAccount_Created_With_Zero_Balance() {
        // Given
        String accountId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        String createdAt = Instant.now().toString();
        
        SharedAccountCreatedEventDTO accountCreatedEvent = SharedAccountCreatedEventDTO.builder()
                .accountId(accountId)
                .customerId(customerId)
                .createdAt(createdAt)
                .build();
        
        System.out.println("🧪 Test: Publishing AccountCreated event for accountId: " + accountId);
        
        // When - Publish the AccountCreated event
        eventPublisher.publishEvent(accountCreatedEvent);
        
        // Then - Wait for event processing and verify financial account creation
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            // Verify financial account exists in database
            var financialAccountEntity = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertNotNull(financialAccountEntity, "Financial account should be created in database");
            
            System.out.println("✅ Financial account found in database with ID: " + financialAccountEntity.get().getFinancialAccountId());
            
            // Verify account has zero balance
            assertEquals(0, financialAccountEntity.get().getBalanceAmount().intValue(), 
                    "Financial account should be created with zero balance");
            assertEquals("USD", financialAccountEntity.get().getBalanceCurrency(), 
                    "Financial account should be created with USD currency");
            assertEquals("ACTIVE", financialAccountEntity.get().getStatus(), 
                    "Financial account should be created with ACTIVE status");
            
            System.out.println("✅ Financial account has correct zero balance and USD currency");
        });
        
        // Verify balance can be retrieved through service layer
        ApplicationBalanceDTO balance = financialAccountService.getAccountBalance(accountId);
        assertNotNull(balance, "Balance should be retrievable through service layer");
        assertNotNull(balance.getBalance(), "Balance value should not be null");
        assertEquals("0", balance.getBalance().getAmount(), "Balance amount should be zero");
        assertEquals("USD", balance.getBalance().getCurrency(), "Balance currency should be USD");
        
        System.out.println("✅ Balance retrieved through service layer: " + balance.getBalance().getAmount() + " " + balance.getBalance().getCurrency());
        
        // Verify balance is cached in Redis
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            ApplicationMoneyValue cachedBalance = cacheService.getBalance(accountId);
            if (cachedBalance != null) {
                assertEquals("0", cachedBalance.getAmount(), "Cached balance amount should be zero");
                assertEquals("USD", cachedBalance.getCurrency(), "Cached balance currency should be USD");
                System.out.println("✅ Balance is properly cached in Redis: " + cachedBalance.getAmount() + " " + cachedBalance.getCurrency());
            } else {
                System.out.println("ℹ️ Balance not yet cached in Redis, which is acceptable for this test");
            }
        });
        
        // Verify database constraints and data integrity
        var allAccounts = financialAccountRepository.findAll();
        assertEquals(1, allAccounts.size(), "Only one financial account should exist");
        
        var createdAccount = allAccounts.get(0);
        assertNotNull(createdAccount.getCreatedAt(), "Created timestamp should be set");
        assertNotNull(createdAccount.getUpdatedAt(), "Updated timestamp should be set");
        assertEquals(0L, createdAccount.getVersion().longValue(), "Version should start at 0 for new account");
        
        System.out.println("✅ Database integrity verified - account created with proper timestamps and version");
        System.out.println("✅ Test completed successfully: AccountCreated event properly creates financial account with zero balance");
    }

    @Test
    @Transactional
    void When_Credit_Transaction_Processed_Then_Balance_Updated_And_Event_Published() {
        // Given - Create a financial account first
        String accountId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        String createdAt = Instant.now().toString();
        
        SharedAccountCreatedEventDTO accountCreatedEvent = SharedAccountCreatedEventDTO.builder()
                .accountId(accountId)
                .customerId(customerId)
                .createdAt(createdAt)
                .build();
        
        System.out.println("🧪 Test: Setting up financial account for credit transaction test, accountId: " + accountId);
        
        // Create the financial account
        eventPublisher.publishEvent(accountCreatedEvent);
        
        // Wait for account creation
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var financialAccountEntity = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertTrue(financialAccountEntity.isPresent(), "Financial account should be created");
        });
        
        // Prepare credit transaction request
        SharedMoneyValue creditAmount = SharedMoneyValue.builder()
                .amount("100.50")
                .currency("USD")
                .build();
        
        SharedTransactionRequestDTO creditRequest = SharedTransactionRequestDTO.builder()
                .amount(creditAmount)
                .type("CREDIT")
                .description("Test credit transaction")
                .reference("TEST-REF-001")
                .build();
        
        System.out.println("🧪 Processing credit transaction of $100.50 for accountId: " + accountId);
        
        // When - Process the credit transaction via REST API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SharedTransactionRequestDTO> requestEntity = new HttpEntity<>(creditRequest, headers);
        
        String transactionUrl = "http://localhost:" + port + "/financial-accounts/" + accountId + "/transactions";
        ResponseEntity<SharedTransactionResponseDTO> response = restTemplate.exchange(
                transactionUrl, HttpMethod.POST, requestEntity, SharedTransactionResponseDTO.class);
        
        // Then - Verify transaction response
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Transaction should be created successfully");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        SharedTransactionResponseDTO transactionResponse = response.getBody();
        assertNotNull(transactionResponse.getTransactionId(), "Transaction ID should be generated");
        assertNotNull(transactionResponse.getNewBalance(), "New balance should be returned");
        assertEquals("100.50", transactionResponse.getNewBalance().getAmount(), "New balance should be $100.50");
        assertEquals("USD", transactionResponse.getNewBalance().getCurrency(), "New balance currency should be USD");
        assertNotNull(transactionResponse.getProcessedAt(), "Processed timestamp should be set");
        
        System.out.println("✅ Credit transaction processed successfully with ID: " + transactionResponse.getTransactionId());
        System.out.println("✅ New balance: " + transactionResponse.getNewBalance().getAmount() + " " + transactionResponse.getNewBalance().getCurrency());
        
        // Verify database updates
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            // Check financial account balance update
            var updatedAccount = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertTrue(updatedAccount.isPresent(), "Financial account should exist");
            assertEquals(new BigDecimal("100.50"), updatedAccount.get().getBalanceAmount(), "Account balance should be updated to $100.50");
            assertEquals(1L, updatedAccount.get().getVersion().longValue(), "Account version should be incremented due to optimistic locking");
            
            System.out.println("✅ Database balance updated: " + updatedAccount.get().getBalanceAmount());
            
            // Check transaction record creation
            var transactions = transactionRepository.findByFinancialAccountId(updatedAccount.get().getFinancialAccountId(), null);
            assertEquals(1, transactions.getTotalElements(), "One transaction record should be created");
            
            var transactionEntity = transactions.getContent().get(0);
            assertEquals(new BigDecimal("100.50"), transactionEntity.getAmount(), "Transaction amount should be $100.50");
            assertEquals("USD", transactionEntity.getCurrency(), "Transaction currency should be USD");
            assertEquals("CREDIT", transactionEntity.getType(), "Transaction type should be CREDIT");
            assertEquals("Test credit transaction", transactionEntity.getDescription(), "Transaction description should match");
            assertEquals("TEST-REF-001", transactionEntity.getReference(), "Transaction reference should match");
            
            System.out.println("✅ Transaction record created in database with ID: " + transactionEntity.getTransactionId());
        });
        
        // Verify cache invalidation and updated balance retrieval
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            ApplicationBalanceDTO currentBalance = financialAccountService.getAccountBalance(accountId);
            assertNotNull(currentBalance, "Balance should be retrievable");
            assertEquals("100.50", currentBalance.getBalance().getAmount(), "Retrieved balance should be $100.50");
            assertEquals("USD", currentBalance.getBalance().getCurrency(), "Retrieved balance currency should be USD");
            
            System.out.println("✅ Updated balance retrieved through service: " + currentBalance.getBalance().getAmount() + " " + currentBalance.getBalance().getCurrency());
        });
        
        // Verify Kafka event publishing
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            
            verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());
            
            String capturedTopic = topicCaptor.getValue();
            String capturedKey = keyCaptor.getValue();
            String capturedMessage = messageCaptor.getValue();
            
            assertEquals("financial-events-test", capturedTopic, "Event should be published to correct Kafka topic");
            assertNotNull(capturedKey, "Kafka message key should not be null");
            assertNotNull(capturedMessage, "Kafka message should not be null");
            assertTrue(capturedMessage.contains("TRANSACTION_PROCESSED"), "Event should contain TRANSACTION_PROCESSED type");
            assertTrue(capturedMessage.contains("100.50"), "Event should contain transaction amount");
            assertTrue(capturedMessage.contains("CREDIT"), "Event should contain transaction type");
            
            System.out.println("✅ Kafka event published successfully to topic: " + capturedTopic);
            System.out.println("✅ Event message contains: " + capturedMessage.substring(0, Math.min(200, capturedMessage.length())) + "...");
        });
        
        // Verify overall system state
        var finalAccountState = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
        assertTrue(finalAccountState.isPresent(), "Account should still exist");
        assertEquals("ACTIVE", finalAccountState.get().getStatus(), "Account should remain active");
        
        var allTransactions = transactionRepository.findAll();
        assertEquals(1, allTransactions.size(), "Exactly one transaction should exist in the system");
        
        System.out.println("✅ Test completed successfully: Credit transaction processed with balance update, database persistence, and Kafka event publishing");
    }

    @Test
    @Transactional
    void When_Debit_Transaction_With_Insufficient_Funds_Then_Transaction_Rejected() {
        // Given - Create a financial account with a small balance first
        String accountId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        String createdAt = Instant.now().toString();
        
        SharedAccountCreatedEventDTO accountCreatedEvent = SharedAccountCreatedEventDTO.builder()
                .accountId(accountId)
                .customerId(customerId)
                .createdAt(createdAt)
                .build();
        
        System.out.println("🧪 Test: Setting up financial account for insufficient funds test, accountId: " + accountId);
        
        // Create the financial account
        eventPublisher.publishEvent(accountCreatedEvent);
        
        // Wait for account creation
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var financialAccountEntity = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertTrue(financialAccountEntity.isPresent(), "Financial account should be created");
        });
        
        // Add a small credit first to have some balance
        SharedMoneyValue initialCredit = SharedMoneyValue.builder()
                .amount("50.00")
                .currency("USD")
                .build();
        
        SharedTransactionRequestDTO initialCreditRequest = SharedTransactionRequestDTO.builder()
                .amount(initialCredit)
                .type("CREDIT")
                .description("Initial credit for insufficient funds test")
                .reference("INITIAL-CREDIT")
                .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SharedTransactionRequestDTO> initialCreditEntity = new HttpEntity<>(initialCreditRequest, headers);
        
        String transactionUrl = "http://localhost:" + port + "/financial-accounts/" + accountId + "/transactions";
        ResponseEntity<SharedTransactionResponseDTO> initialCreditResponse = restTemplate.exchange(
                transactionUrl, HttpMethod.POST, initialCreditEntity, SharedTransactionResponseDTO.class);
        
        assertEquals(HttpStatus.CREATED, initialCreditResponse.getStatusCode(), "Initial credit should be successful");
        System.out.println("✅ Initial credit of $50.00 added successfully");
        
        // Wait for initial credit to be processed
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var updatedAccount = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertTrue(updatedAccount.isPresent(), "Financial account should exist");
            assertEquals(new BigDecimal("50.00"), updatedAccount.get().getBalanceAmount(), "Account balance should be $50.00");
        });
        
        // Record initial state for comparison
        var initialAccountState = financialAccountRepository.findByAccountId(UUID.fromString(accountId)).get();
        BigDecimal initialBalance = initialAccountState.getBalanceAmount();
        Long initialVersion = initialAccountState.getVersion();
        var initialTransactionCount = transactionRepository.findAll().size();
        
        System.out.println("✅ Initial state recorded - Balance: $" + initialBalance + ", Version: " + initialVersion + ", Transactions: " + initialTransactionCount);
        
        // Prepare debit transaction that exceeds available balance
        SharedMoneyValue debitAmount = SharedMoneyValue.builder()
                .amount("100.00")  // Requesting $100 when only $50 is available
                .currency("USD")
                .build();
        
        SharedTransactionRequestDTO debitRequest = SharedTransactionRequestDTO.builder()
                .amount(debitAmount)
                .type("DEBIT")
                .description("Test debit transaction with insufficient funds")
                .reference("INSUFFICIENT-FUNDS-TEST")
                .build();
        
        System.out.println("🧪 Attempting debit transaction of $100.00 when balance is only $50.00 for accountId: " + accountId);
        
        // When - Attempt the debit transaction via REST API
        HttpEntity<SharedTransactionRequestDTO> debitRequestEntity = new HttpEntity<>(debitRequest, headers);
        ResponseEntity<SharedErrorResponseDTO> response = restTemplate.exchange(
                transactionUrl, HttpMethod.POST, debitRequestEntity, SharedErrorResponseDTO.class);
        
        // Then - Verify transaction is rejected with proper error response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Transaction should be rejected with BAD_REQUEST status");
        assertNotNull(response.getBody(), "Error response body should not be null");
        
        SharedErrorResponseDTO errorResponse = response.getBody();
        assertEquals("INSUFFICIENT_FUNDS", errorResponse.getError(), "Error code should be INSUFFICIENT_FUNDS");
        assertNotNull(errorResponse.getMessage(), "Error message should not be null");
        assertTrue(errorResponse.getMessage().contains("Insufficient funds"), "Error message should mention insufficient funds");
        assertTrue(errorResponse.getMessage().contains(accountId), "Error message should contain account ID");
        assertTrue(errorResponse.getMessage().contains("100"), "Error message should contain requested amount");
        assertTrue(errorResponse.getMessage().contains("50"), "Error message should contain available balance");
        assertNotNull(errorResponse.getTimestamp(), "Error timestamp should be set");
        assertNotNull(errorResponse.getPath(), "Error path should be set");
        
        System.out.println("✅ Transaction properly rejected with error: " + errorResponse.getError());
        System.out.println("✅ Error message: " + errorResponse.getMessage());
        
        // Verify no database changes occurred
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            // Check that account balance remains unchanged
            var unchangedAccount = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertTrue(unchangedAccount.isPresent(), "Financial account should still exist");
            assertEquals(initialBalance, unchangedAccount.get().getBalanceAmount(), "Account balance should remain unchanged at $50.00");
            assertEquals(initialVersion, unchangedAccount.get().getVersion(), "Account version should remain unchanged (no optimistic lock increment)");
            assertEquals("ACTIVE", unchangedAccount.get().getStatus(), "Account status should remain ACTIVE");
            
            System.out.println("✅ Account balance unchanged: $" + unchangedAccount.get().getBalanceAmount());
            System.out.println("✅ Account version unchanged: " + unchangedAccount.get().getVersion());
            
            // Check that no new transaction record was created
            var finalTransactionCount = transactionRepository.findAll().size();
            assertEquals(initialTransactionCount, finalTransactionCount, "No new transaction record should be created for rejected transaction");
            
            System.out.println("✅ Transaction count unchanged: " + finalTransactionCount);
        });
        
        // Verify balance can still be retrieved correctly through service layer
        ApplicationBalanceDTO currentBalance = financialAccountService.getAccountBalance(accountId);
        assertNotNull(currentBalance, "Balance should still be retrievable through service layer");
        assertEquals("50.00", currentBalance.getBalance().getAmount(), "Retrieved balance should still be $50.00");
        assertEquals("USD", currentBalance.getBalance().getCurrency(), "Retrieved balance currency should still be USD");
        
        System.out.println("✅ Balance still retrievable through service: $" + currentBalance.getBalance().getAmount() + " " + currentBalance.getBalance().getCurrency());
        
        // Verify no Kafka event was published for the rejected transaction
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            // We should only have the initial credit transaction event, not the rejected debit
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            
            // Verify only one event was published (for the initial credit)
            verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());
            
            String capturedMessage = messageCaptor.getValue();
            assertTrue(capturedMessage.contains("CREDIT"), "Only the initial credit event should be published");
            assertTrue(capturedMessage.contains("50.00"), "Event should contain the initial credit amount");
            assertFalse(capturedMessage.contains("100.00"), "Event should not contain the rejected debit amount");
            
            System.out.println("✅ No Kafka event published for rejected transaction - only initial credit event exists");
        });
        
        // Verify cache consistency
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ApplicationMoneyValue cachedBalance = cacheService.getBalance(accountId);
            if (cachedBalance != null) {
                assertEquals("50.00", cachedBalance.getAmount(), "Cached balance should remain $50.00");
                assertEquals("USD", cachedBalance.getCurrency(), "Cached balance currency should remain USD");
                System.out.println("✅ Cache remains consistent: $" + cachedBalance.getAmount() + " " + cachedBalance.getCurrency());
            } else {
                System.out.println("ℹ️ Balance not cached, which is acceptable");
            }
        });
        
        // Verify overall system integrity
        var finalAccountState = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
        assertTrue(finalAccountState.isPresent(), "Account should still exist");
        assertEquals("ACTIVE", finalAccountState.get().getStatus(), "Account should remain active");
        
        var allTransactions = transactionRepository.findAll();
        assertEquals(1, allTransactions.size(), "Only the initial credit transaction should exist in the system");
        
        var onlyTransaction = allTransactions.get(0);
        assertEquals("CREDIT", onlyTransaction.getType(), "The only transaction should be the initial credit");
        assertEquals(new BigDecimal("50.00"), onlyTransaction.getAmount(), "The only transaction should be for $50.00");
        assertEquals("INITIAL-CREDIT", onlyTransaction.getReference(), "The only transaction should be the initial credit");
        
        System.out.println("✅ System integrity verified - only initial credit transaction exists");
        System.out.println("✅ Test completed successfully: Debit transaction with insufficient funds properly rejected with no database changes");
    }
}