package ai.shreds;

import ai.shreds.application.dtos.ApplicationBalanceDTO;
import ai.shreds.application.ports.ApplicationInputPortFinancialAccount;
import ai.shreds.application.ports.ApplicationInputPortTransaction;
import ai.shreds.application.ports.ApplicationInputPortPaymentMethod;
import ai.shreds.infrastructure.repositories.InfrastructureFinancialAccountJpaRepository;
import ai.shreds.infrastructure.repositories.InfrastructureTransactionJpaRepository;
import ai.shreds.infrastructure.repositories.InfrastructurePaymentMethodJpaRepository;
import ai.shreds.shared.dtos.SharedAccountCreatedEventDTO;
import ai.shreds.shared.dtos.SharedAccountSuspendedEventDTO;
import ai.shreds.shared.dtos.SharedAccountClosedEventDTO;
import ai.shreds.shared.dtos.SharedTransactionRequestDTO;
import ai.shreds.shared.dtos.SharedErrorResponseDTO;
import ai.shreds.shared.dtos.SharedPaymentMethodRequestDTO;
import ai.shreds.shared.value_objects.SharedMoneyValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

/**
 * Integration test for account lifecycle events (suspension and closure) and their impact on financial operations.
 * Tests the handling of AccountSuspended and AccountClosed events and verifies that financial accounts are properly frozen/closed
 * and subsequent transactions are blocked.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class AccountLifecycleEventIntegrationTest {

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
    private ApplicationInputPortPaymentMethod paymentMethodService;

    @Autowired
    private InfrastructureFinancialAccountJpaRepository financialAccountRepository;

    @Autowired
    private InfrastructureTransactionJpaRepository transactionRepository;

    @Autowired
    private InfrastructurePaymentMethodJpaRepository paymentMethodRepository;

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
        paymentMethodRepository.deleteAll();
        transactionRepository.deleteAll();
        financialAccountRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        
        System.out.println("✅ Test setup completed - database and cache cleared");
    }

    @Test
    @Transactional
    void When_AccountSuspended_Event_Received_Then_FinancialAccount_Frozen_And_Transactions_Blocked() {
        // Given - Create a financial account first
        String accountId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        String createdAt = Instant.now().toString();
        
        SharedAccountCreatedEventDTO accountCreatedEvent = SharedAccountCreatedEventDTO.builder()
                .accountId(accountId)
                .customerId(customerId)
                .createdAt(createdAt)
                .build();
        
        System.out.println("🧪 Test: Setting up financial account for suspension test, accountId: " + accountId);
        
        // Create the financial account
        eventPublisher.publishEvent(accountCreatedEvent);
        
        // Wait for account creation and verify it's active
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var financialAccountEntity = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertTrue(financialAccountEntity.isPresent(), "Financial account should be created");
            assertEquals("ACTIVE", financialAccountEntity.get().getStatus(), "Account should initially be ACTIVE");
            System.out.println("✅ Financial account created with ACTIVE status");
        });
        
        // Add some initial balance to test transaction blocking later
        SharedMoneyValue initialCredit = SharedMoneyValue.builder()
                .amount("100.00")
                .currency("USD")
                .build();
        
        SharedTransactionRequestDTO initialCreditRequest = SharedTransactionRequestDTO.builder()
                .amount(initialCredit)
                .type("CREDIT")
                .description("Initial credit before suspension")
                .reference("INITIAL-CREDIT")
                .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SharedTransactionRequestDTO> initialCreditEntity = new HttpEntity<>(initialCreditRequest, headers);
        
        String transactionUrl = "http://localhost:" + port + "/financial-accounts/" + accountId + "/transactions";
        ResponseEntity<Object> initialCreditResponse = restTemplate.exchange(
                transactionUrl, HttpMethod.POST, initialCreditEntity, Object.class);
        
        assertEquals(HttpStatus.CREATED, initialCreditResponse.getStatusCode(), "Initial credit should be successful");
        System.out.println("✅ Initial credit of $100.00 added successfully");
        
        // Wait for initial credit to be processed and verify balance
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ApplicationBalanceDTO balance = financialAccountService.getAccountBalance(accountId);
            assertNotNull(balance, "Balance should be retrievable");
            assertEquals("100.00", balance.getBalance().getAmount(), "Balance should be $100.00");
            System.out.println("✅ Initial balance verified: $" + balance.getBalance().getAmount());
        });
        
        // Record initial state for comparison
        var initialAccountState = financialAccountRepository.findByAccountId(UUID.fromString(accountId)).get();
        var initialTransactionCount = transactionRepository.findAll().size();
        
        System.out.println("✅ Initial state recorded - Status: " + initialAccountState.getStatus() + ", Transactions: " + initialTransactionCount);
        
        // When - Publish AccountSuspended event
        String suspensionReason = "Account suspended due to suspicious activity";
        String suspendedAt = Instant.now().toString();
        
        SharedAccountSuspendedEventDTO accountSuspendedEvent = SharedAccountSuspendedEventDTO.builder()
                .accountId(accountId)
                .reason(suspensionReason)
                .suspendedAt(suspendedAt)
                .build();
        
        System.out.println("🧪 Publishing AccountSuspended event for accountId: " + accountId);
        System.out.println("🧪 Suspension reason: " + suspensionReason);
        
        eventPublisher.publishEvent(accountSuspendedEvent);
        
        // Then - Verify financial account status is set to FROZEN
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var suspendedAccountEntity = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertTrue(suspendedAccountEntity.isPresent(), "Financial account should still exist after suspension");
            assertEquals("FROZEN", suspendedAccountEntity.get().getStatus(), "Account status should be FROZEN after suspension");
            
            // Verify other account details remain unchanged
            assertEquals(initialAccountState.getBalanceAmount(), suspendedAccountEntity.get().getBalanceAmount(), "Balance should remain unchanged");
            assertEquals(initialAccountState.getBalanceCurrency(), suspendedAccountEntity.get().getBalanceCurrency(), "Currency should remain unchanged");
            assertEquals(initialAccountState.getAccountId(), suspendedAccountEntity.get().getAccountId(), "Account ID should remain unchanged");
            
            System.out.println("✅ Account status successfully changed to FROZEN");
            System.out.println("✅ Balance preserved: $" + suspendedAccountEntity.get().getBalanceAmount() + " " + suspendedAccountEntity.get().getBalanceCurrency());
        });
        
        // Verify balance can still be retrieved (read operations should work)
        ApplicationBalanceDTO balanceAfterSuspension = financialAccountService.getAccountBalance(accountId);
        assertNotNull(balanceAfterSuspension, "Balance should still be retrievable after suspension");
        assertEquals("100.00", balanceAfterSuspension.getBalance().getAmount(), "Balance should remain $100.00 after suspension");
        assertEquals("USD", balanceAfterSuspension.getBalance().getCurrency(), "Currency should remain USD after suspension");
        
        System.out.println("✅ Balance still retrievable after suspension: $" + balanceAfterSuspension.getBalance().getAmount() + " " + balanceAfterSuspension.getBalance().getCurrency());
        
        // Verify that subsequent transactions are blocked - Test CREDIT transaction
        SharedMoneyValue blockedCreditAmount = SharedMoneyValue.builder()
                .amount("50.00")
                .currency("USD")
                .build();
        
        SharedTransactionRequestDTO blockedCreditRequest = SharedTransactionRequestDTO.builder()
                .amount(blockedCreditAmount)
                .type("CREDIT")
                .description("Credit transaction on frozen account")
                .reference("BLOCKED-CREDIT-TEST")
                .build();
        
        System.out.println("🧪 Attempting CREDIT transaction on frozen account");
        
        HttpEntity<SharedTransactionRequestDTO> blockedCreditEntity = new HttpEntity<>(blockedCreditRequest, headers);
        ResponseEntity<SharedErrorResponseDTO> blockedCreditResponse = restTemplate.exchange(
                transactionUrl, HttpMethod.POST, blockedCreditEntity, SharedErrorResponseDTO.class);
        
        // Verify CREDIT transaction is rejected
        assertEquals(HttpStatus.LOCKED, blockedCreditResponse.getStatusCode(), "Credit transaction should be rejected with LOCKED status");
        assertNotNull(blockedCreditResponse.getBody(), "Error response body should not be null");
        
        SharedErrorResponseDTO creditErrorResponse = blockedCreditResponse.getBody();
        assertEquals("ACCOUNT_FROZEN", creditErrorResponse.getError(), "Error code should be ACCOUNT_FROZEN");
        assertNotNull(creditErrorResponse.getMessage(), "Error message should not be null");
        assertTrue(creditErrorResponse.getMessage().contains("frozen") || creditErrorResponse.getMessage().contains("FROZEN"), "Error message should mention frozen account");
        assertTrue(creditErrorResponse.getMessage().contains(accountId), "Error message should contain account ID");
        
        System.out.println("✅ CREDIT transaction properly rejected with error: " + creditErrorResponse.getError());
        System.out.println("✅ Error message: " + creditErrorResponse.getMessage());
        
        // Verify that subsequent transactions are blocked - Test DEBIT transaction
        SharedMoneyValue blockedDebitAmount = SharedMoneyValue.builder()
                .amount("25.00")
                .currency("USD")
                .build();
        
        SharedTransactionRequestDTO blockedDebitRequest = SharedTransactionRequestDTO.builder()
                .amount(blockedDebitAmount)
                .type("DEBIT")
                .description("Debit transaction on frozen account")
                .reference("BLOCKED-DEBIT-TEST")
                .build();
        
        System.out.println("🧪 Attempting DEBIT transaction on frozen account");
        
        HttpEntity<SharedTransactionRequestDTO> blockedDebitEntity = new HttpEntity<>(blockedDebitRequest, headers);
        ResponseEntity<SharedErrorResponseDTO> blockedDebitResponse = restTemplate.exchange(
                transactionUrl, HttpMethod.POST, blockedDebitEntity, SharedErrorResponseDTO.class);
        
        // Verify DEBIT transaction is rejected
        assertEquals(HttpStatus.LOCKED, blockedDebitResponse.getStatusCode(), "Debit transaction should be rejected with LOCKED status");
        assertNotNull(blockedDebitResponse.getBody(), "Error response body should not be null");
        
        SharedErrorResponseDTO debitErrorResponse = blockedDebitResponse.getBody();
        assertEquals("ACCOUNT_FROZEN", debitErrorResponse.getError(), "Error code should be ACCOUNT_FROZEN");
        assertNotNull(debitErrorResponse.getMessage(), "Error message should not be null");
        assertTrue(debitErrorResponse.getMessage().contains("frozen") || debitErrorResponse.getMessage().contains("FROZEN"), "Error message should mention frozen account");
        assertTrue(debitErrorResponse.getMessage().contains(accountId), "Error message should contain account ID");
        
        System.out.println("✅ DEBIT transaction properly rejected with error: " + debitErrorResponse.getError());
        System.out.println("✅ Error message: " + debitErrorResponse.getMessage());
        
        // Verify no new transactions were created after suspension
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            var finalTransactionCount = transactionRepository.findAll().size();
            assertEquals(initialTransactionCount, finalTransactionCount, "No new transactions should be created after account suspension");
            
            System.out.println("✅ Transaction count unchanged after suspension: " + finalTransactionCount);
            
            // Verify account balance remains unchanged
            var finalAccountState = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertTrue(finalAccountState.isPresent(), "Account should still exist");
            assertEquals(initialAccountState.getBalanceAmount(), finalAccountState.get().getBalanceAmount(), "Balance should remain unchanged after blocked transactions");
            assertEquals("FROZEN", finalAccountState.get().getStatus(), "Account should remain FROZEN");
            
            System.out.println("✅ Account balance unchanged: $" + finalAccountState.get().getBalanceAmount());
            System.out.println("✅ Account status remains: " + finalAccountState.get().getStatus());
        });
        
        // Verify balance is still retrievable and unchanged
        ApplicationBalanceDTO finalBalance = financialAccountService.getAccountBalance(accountId);
        assertNotNull(finalBalance, "Balance should still be retrievable");
        assertEquals("100.00", finalBalance.getBalance().getAmount(), "Final balance should still be $100.00");
        assertEquals("USD", finalBalance.getBalance().getCurrency(), "Final balance currency should still be USD");
        
        System.out.println("✅ Final balance verification: $" + finalBalance.getBalance().getAmount() + " " + finalBalance.getBalance().getCurrency());
        
        // Verify overall system integrity
        var allAccounts = financialAccountRepository.findAll();
        assertEquals(1, allAccounts.size(), "Should have exactly one financial account");
        
        var finalAccount = allAccounts.get(0);
        assertEquals("FROZEN", finalAccount.getStatus(), "Account should be FROZEN");
        assertEquals(UUID.fromString(accountId), finalAccount.getAccountId(), "Account ID should match");
        
        var allTransactions = transactionRepository.findAll();
        assertEquals(1, allTransactions.size(), "Should have exactly one transaction (the initial credit)");
        
        var onlyTransaction = allTransactions.get(0);
        assertEquals("CREDIT", onlyTransaction.getType(), "The only transaction should be the initial credit");
        assertEquals("INITIAL-CREDIT", onlyTransaction.getReference(), "The only transaction should be the initial credit");
        
        System.out.println("✅ System integrity verified - account frozen, transactions blocked, balance preserved");
        System.out.println("✅ Test completed successfully: AccountSuspended event properly freezes financial account and blocks all subsequent transactions");
    }

    @Test
    @Transactional
    void When_AccountClosed_Event_Received_Then_FinancialAccount_Closed_And_PaymentMethods_Deactivated() {
        // Given - Create a financial account first
        String accountId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        String createdAt = Instant.now().toString();
        
        SharedAccountCreatedEventDTO accountCreatedEvent = SharedAccountCreatedEventDTO.builder()
                .accountId(accountId)
                .customerId(customerId)
                .createdAt(createdAt)
                .build();
        
        System.out.println("🧪 Test: Setting up financial account for closure test, accountId: " + accountId);
        
        // Create the financial account
        eventPublisher.publishEvent(accountCreatedEvent);
        
        // Wait for account creation and verify it's active
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var financialAccountEntity = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertTrue(financialAccountEntity.isPresent(), "Financial account should be created");
            assertEquals("ACTIVE", financialAccountEntity.get().getStatus(), "Account should initially be ACTIVE");
            System.out.println("✅ Financial account created with ACTIVE status");
        });
        
        // Add some initial balance
        SharedMoneyValue initialCredit = SharedMoneyValue.builder()
                .amount("200.00")
                .currency("USD")
                .build();
        
        SharedTransactionRequestDTO initialCreditRequest = SharedTransactionRequestDTO.builder()
                .amount(initialCredit)
                .type("CREDIT")
                .description("Initial credit before closure")
                .reference("INITIAL-CREDIT")
                .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SharedTransactionRequestDTO> initialCreditEntity = new HttpEntity<>(initialCreditRequest, headers);
        
        String transactionUrl = "http://localhost:" + port + "/financial-accounts/" + accountId + "/transactions";
        ResponseEntity<Object> initialCreditResponse = restTemplate.exchange(
                transactionUrl, HttpMethod.POST, initialCreditEntity, Object.class);
        
        assertEquals(HttpStatus.CREATED, initialCreditResponse.getStatusCode(), "Initial credit should be successful");
        System.out.println("✅ Initial credit of $200.00 added successfully");
        
        // Wait for initial credit to be processed and verify balance
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ApplicationBalanceDTO balance = financialAccountService.getAccountBalance(accountId);
            assertNotNull(balance, "Balance should be retrievable");
            assertEquals("200.00", balance.getBalance().getAmount(), "Balance should be $200.00");
            System.out.println("✅ Initial balance verified: $" + balance.getBalance().getAmount());
        });
        
        // Add some payment methods to test deactivation
        SharedPaymentMethodRequestDTO paymentMethod1 = SharedPaymentMethodRequestDTO.builder()
                .accountId(accountId)
                .cardNumber("4111111111111111")
                .expiryMonth(12)
                .expiryYear(2025)
                .cvv("123")
                .billingAddressLine1("123 Main St")
                .billingCity("New York")
                .billingState("NY")
                .billingPostalCode("10001")
                .billingCountry("US")
                .build();
        
        SharedPaymentMethodRequestDTO paymentMethod2 = SharedPaymentMethodRequestDTO.builder()
                .accountId(accountId)
                .cardNumber("5555555555554444")
                .expiryMonth(6)
                .expiryYear(2026)
                .cvv("456")
                .billingAddressLine1("456 Oak Ave")
                .billingCity("Los Angeles")
                .billingState("CA")
                .billingPostalCode("90210")
                .billingCountry("US")
                .build();
        
        // Mock the payment gateway responses for adding payment methods
        String paymentMethodUrl = "http://localhost:" + port + "/payment-methods";
        
        System.out.println("🧪 Adding payment methods before account closure");
        
        // Note: In a real scenario, we would need to mock the payment gateway
        // For this test, we'll create payment methods directly in the database to simulate them being added
        var paymentMethodEntity1 = new ai.shreds.infrastructure.repositories.InfrastructurePaymentMethodJpaEntity();
        paymentMethodEntity1.setAccountId(UUID.fromString(accountId));
        paymentMethodEntity1.setPaymentType("CREDIT_CARD");
        paymentMethodEntity1.setToken("tok_test_1");
        paymentMethodEntity1.setLastFourDigits("1111");
        paymentMethodEntity1.setExpiryMonth(12);
        paymentMethodEntity1.setExpiryYear(2025);
        paymentMethodEntity1.setCardBrand("VISA");
        paymentMethodEntity1.setBillingAddressLine1("123 Main St");
        paymentMethodEntity1.setBillingCity("New York");
        paymentMethodEntity1.setBillingState("NY");
        paymentMethodEntity1.setBillingPostalCode("10001");
        paymentMethodEntity1.setBillingCountry("US");
        paymentMethodEntity1.setIsDefault(true);
        paymentMethodEntity1.setIsActive(true);
        paymentMethodRepository.save(paymentMethodEntity1);
        
        var paymentMethodEntity2 = new ai.shreds.infrastructure.repositories.InfrastructurePaymentMethodJpaEntity();
        paymentMethodEntity2.setAccountId(UUID.fromString(accountId));
        paymentMethodEntity2.setPaymentType("CREDIT_CARD");
        paymentMethodEntity2.setToken("tok_test_2");
        paymentMethodEntity2.setLastFourDigits("4444");
        paymentMethodEntity2.setExpiryMonth(6);
        paymentMethodEntity2.setExpiryYear(2026);
        paymentMethodEntity2.setCardBrand("MASTERCARD");
        paymentMethodEntity2.setBillingAddressLine1("456 Oak Ave");
        paymentMethodEntity2.setBillingCity("Los Angeles");
        paymentMethodEntity2.setBillingState("CA");
        paymentMethodEntity2.setBillingPostalCode("90210");
        paymentMethodEntity2.setBillingCountry("US");
        paymentMethodEntity2.setIsDefault(false);
        paymentMethodEntity2.setIsActive(true);
        paymentMethodRepository.save(paymentMethodEntity2);
        
        System.out.println("✅ Two payment methods added successfully");
        
        // Verify payment methods are active
        var initialPaymentMethods = paymentMethodRepository.findByAccountId(UUID.fromString(accountId));
        assertEquals(2, initialPaymentMethods.size(), "Should have 2 payment methods");
        assertTrue(initialPaymentMethods.stream().allMatch(pm -> pm.getIsActive()), "All payment methods should be active initially");
        
        long activePaymentMethodsCount = initialPaymentMethods.stream().filter(pm -> pm.getIsActive()).count();
        System.out.println("✅ Initial active payment methods count: " + activePaymentMethodsCount);
        
        // Record initial state for comparison
        var initialAccountState = financialAccountRepository.findByAccountId(UUID.fromString(accountId)).get();
        var initialTransactionCount = transactionRepository.findAll().size();
        
        System.out.println("✅ Initial state recorded - Status: " + initialAccountState.getStatus() + ", Transactions: " + initialTransactionCount);
        
        // When - Publish AccountClosed event
        String closureReason = "Account closed by user request";
        String closedAt = Instant.now().toString();
        
        SharedAccountClosedEventDTO accountClosedEvent = SharedAccountClosedEventDTO.builder()
                .accountId(accountId)
                .reason(closureReason)
                .closedAt(closedAt)
                .build();
        
        System.out.println("🧪 Publishing AccountClosed event for accountId: " + accountId);
        System.out.println("🧪 Closure reason: " + closureReason);
        
        eventPublisher.publishEvent(accountClosedEvent);
        
        // Then - Verify financial account status is set to CLOSED
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var closedAccountEntity = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertTrue(closedAccountEntity.isPresent(), "Financial account should still exist after closure");
            assertEquals("CLOSED", closedAccountEntity.get().getStatus(), "Account status should be CLOSED after closure");
            
            // Verify other account details remain unchanged
            assertEquals(initialAccountState.getBalanceAmount(), closedAccountEntity.get().getBalanceAmount(), "Balance should remain unchanged");
            assertEquals(initialAccountState.getBalanceCurrency(), closedAccountEntity.get().getBalanceCurrency(), "Currency should remain unchanged");
            assertEquals(initialAccountState.getAccountId(), closedAccountEntity.get().getAccountId(), "Account ID should remain unchanged");
            
            System.out.println("✅ Account status successfully changed to CLOSED");
            System.out.println("✅ Balance preserved: $" + closedAccountEntity.get().getBalanceAmount() + " " + closedAccountEntity.get().getBalanceCurrency());
        });
        
        // Verify all payment methods are deactivated
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var paymentMethodsAfterClosure = paymentMethodRepository.findByAccountId(UUID.fromString(accountId));
            assertEquals(2, paymentMethodsAfterClosure.size(), "Should still have 2 payment methods");
            
            long activePaymentMethodsAfterClosure = paymentMethodsAfterClosure.stream().filter(pm -> pm.getIsActive()).count();
            assertEquals(0, activePaymentMethodsAfterClosure, "All payment methods should be deactivated after account closure");
            
            // Verify all payment methods are inactive
            assertTrue(paymentMethodsAfterClosure.stream().allMatch(pm -> !pm.getIsActive()), "All payment methods should be inactive after account closure");
            
            System.out.println("✅ All payment methods successfully deactivated");
            System.out.println("✅ Active payment methods after closure: " + activePaymentMethodsAfterClosure);
        });
        
        // Verify that subsequent transactions are blocked - Test CREDIT transaction
        SharedMoneyValue blockedCreditAmount = SharedMoneyValue.builder()
                .amount("50.00")
                .currency("USD")
                .build();
        
        SharedTransactionRequestDTO blockedCreditRequest = SharedTransactionRequestDTO.builder()
                .amount(blockedCreditAmount)
                .type("CREDIT")
                .description("Credit transaction on closed account")
                .reference("BLOCKED-CREDIT-TEST")
                .build();
        
        System.out.println("🧪 Attempting CREDIT transaction on closed account");
        
        HttpEntity<SharedTransactionRequestDTO> blockedCreditEntity = new HttpEntity<>(blockedCreditRequest, headers);
        ResponseEntity<SharedErrorResponseDTO> blockedCreditResponse = restTemplate.exchange(
                transactionUrl, HttpMethod.POST, blockedCreditEntity, SharedErrorResponseDTO.class);
        
        // Verify CREDIT transaction is rejected
        assertEquals(HttpStatus.LOCKED, blockedCreditResponse.getStatusCode(), "Credit transaction should be rejected with LOCKED status");
        assertNotNull(blockedCreditResponse.getBody(), "Error response body should not be null");
        
        SharedErrorResponseDTO creditErrorResponse = blockedCreditResponse.getBody();
        assertEquals("ACCOUNT_CLOSED", creditErrorResponse.getError(), "Error code should be ACCOUNT_CLOSED");
        assertNotNull(creditErrorResponse.getMessage(), "Error message should not be null");
        assertTrue(creditErrorResponse.getMessage().contains("closed") || creditErrorResponse.getMessage().contains("CLOSED"), "Error message should mention closed account");
        assertTrue(creditErrorResponse.getMessage().contains(accountId), "Error message should contain account ID");
        
        System.out.println("✅ CREDIT transaction properly rejected with error: " + creditErrorResponse.getError());
        System.out.println("✅ Error message: " + creditErrorResponse.getMessage());
        
        // Verify that subsequent transactions are blocked - Test DEBIT transaction
        SharedMoneyValue blockedDebitAmount = SharedMoneyValue.builder()
                .amount("25.00")
                .currency("USD")
                .build();
        
        SharedTransactionRequestDTO blockedDebitRequest = SharedTransactionRequestDTO.builder()
                .amount(blockedDebitAmount)
                .type("DEBIT")
                .description("Debit transaction on closed account")
                .reference("BLOCKED-DEBIT-TEST")
                .build();
        
        System.out.println("🧪 Attempting DEBIT transaction on closed account");
        
        HttpEntity<SharedTransactionRequestDTO> blockedDebitEntity = new HttpEntity<>(blockedDebitRequest, headers);
        ResponseEntity<SharedErrorResponseDTO> blockedDebitResponse = restTemplate.exchange(
                transactionUrl, HttpMethod.POST, blockedDebitEntity, SharedErrorResponseDTO.class);
        
        // Verify DEBIT transaction is rejected
        assertEquals(HttpStatus.LOCKED, blockedDebitResponse.getStatusCode(), "Debit transaction should be rejected with LOCKED status");
        assertNotNull(blockedDebitResponse.getBody(), "Error response body should not be null");
        
        SharedErrorResponseDTO debitErrorResponse = blockedDebitResponse.getBody();
        assertEquals("ACCOUNT_CLOSED", debitErrorResponse.getError(), "Error code should be ACCOUNT_CLOSED");
        assertNotNull(debitErrorResponse.getMessage(), "Error message should not be null");
        assertTrue(debitErrorResponse.getMessage().contains("closed") || debitErrorResponse.getMessage().contains("CLOSED"), "Error message should mention closed account");
        assertTrue(debitErrorResponse.getMessage().contains(accountId), "Error message should contain account ID");
        
        System.out.println("✅ DEBIT transaction properly rejected with error: " + debitErrorResponse.getError());
        System.out.println("✅ Error message: " + debitErrorResponse.getMessage());
        
        // Verify no new transactions were created after closure
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            var finalTransactionCount = transactionRepository.findAll().size();
            assertEquals(initialTransactionCount, finalTransactionCount, "No new transactions should be created after account closure");
            
            System.out.println("✅ Transaction count unchanged after closure: " + finalTransactionCount);
            
            // Verify account balance remains unchanged
            var finalAccountState = financialAccountRepository.findByAccountId(UUID.fromString(accountId));
            assertTrue(finalAccountState.isPresent(), "Account should still exist");
            assertEquals(initialAccountState.getBalanceAmount(), finalAccountState.get().getBalanceAmount(), "Balance should remain unchanged after blocked transactions");
            assertEquals("CLOSED", finalAccountState.get().getStatus(), "Account should remain CLOSED");
            
            System.out.println("✅ Account balance unchanged: $" + finalAccountState.get().getBalanceAmount());
            System.out.println("✅ Account status remains: " + finalAccountState.get().getStatus());
        });
        
        // Verify balance is still retrievable and unchanged
        ApplicationBalanceDTO finalBalance = financialAccountService.getAccountBalance(accountId);
        assertNotNull(finalBalance, "Balance should still be retrievable");
        assertEquals("200.00", finalBalance.getBalance().getAmount(), "Final balance should still be $200.00");
        assertEquals("USD", finalBalance.getBalance().getCurrency(), "Final balance currency should still be USD");
        
        System.out.println("✅ Final balance verification: $" + finalBalance.getBalance().getAmount() + " " + finalBalance.getBalance().getCurrency());
        
        // Verify overall system integrity
        var allAccounts = financialAccountRepository.findAll();
        assertEquals(1, allAccounts.size(), "Should have exactly one financial account");
        
        var finalAccount = allAccounts.get(0);
        assertEquals("CLOSED", finalAccount.getStatus(), "Account should be CLOSED");
        assertEquals(UUID.fromString(accountId), finalAccount.getAccountId(), "Account ID should match");
        
        var allTransactions = transactionRepository.findAll();
        assertEquals(1, allTransactions.size(), "Should have exactly one transaction (the initial credit)");
        
        var onlyTransaction = allTransactions.get(0);
        assertEquals("CREDIT", onlyTransaction.getType(), "The only transaction should be the initial credit");
        assertEquals("INITIAL-CREDIT", onlyTransaction.getReference(), "The only transaction should be the initial credit");
        
        var allPaymentMethods = paymentMethodRepository.findAll();
        assertEquals(2, allPaymentMethods.size(), "Should have exactly two payment methods");
        assertTrue(allPaymentMethods.stream().allMatch(pm -> !pm.getIsActive()), "All payment methods should be inactive");
        
        System.out.println("✅ System integrity verified - account closed, payment methods deactivated, transactions blocked, balance preserved");
        System.out.println("✅ Test completed successfully: AccountClosed event properly closes financial account and deactivates all payment methods");
    }
}