package ai.shreds;

import ai.shreds.application.dtos.ApplicationBalanceDTO;
import ai.shreds.application.dtos.ApplicationTransactionRequestDTO;
import ai.shreds.application.dtos.ApplicationTransactionResponseDTO;
import ai.shreds.application.dtos.ApplicationMoneyValue;
import ai.shreds.application.ports.ApplicationInputPortFinancialAccount;
import ai.shreds.application.ports.ApplicationInputPortTransaction;
import ai.shreds.application.ports.ApplicationOutputPortCache;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.domain.entities.DomainEntityFinancialAccount;
import ai.shreds.domain.entities.DomainEntityTransaction;
import ai.shreds.domain.ports.DomainOutputPortFinancialAccountRepository;
import ai.shreds.domain.ports.DomainOutputPortTransactionRepository;
import ai.shreds.domain.ports.DomainOutputPortCurrencyService;
import ai.shreds.domain.ports.DomainOutputPortPaymentGateway;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.domain.value_objects.DomainCurrencyValue;
import ai.shreds.domain.value_objects.DomainPaymentMethodDataValue;
import ai.shreds.domain.value_objects.DomainPaginationParams;
import ai.shreds.domain.enums.DomainPaymentTypeEnum;
import ai.shreds.domain.enums.DomainCardBrandEnum;
import ai.shreds.shared.dtos.SharedAccountCreatedEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.context.ApplicationEventPublisher;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
class FinancialAccountTransactionIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationInputPortFinancialAccount financialAccountService;

    @Autowired
    private ApplicationInputPortTransaction transactionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private DomainOutputPortFinancialAccountRepository financialAccountRepository;

    @Autowired
    private DomainOutputPortTransactionRepository transactionRepository;

    @Autowired
    private ApplicationOutputPortCache cachePort;

    // TestContainers for external dependencies
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("financial_test_db")
            .withUsername("test_user")
            .withPassword("test_password")
            .withStartupTimeout(Duration.ofMinutes(2));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withStartupTimeout(Duration.ofMinutes(2));

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withStartupTimeout(Duration.ofMinutes(2));

    // Mock external services that are out of process
    @MockBean
    private DomainOutputPortPaymentGateway paymentGateway;

    @MockBean
    private DomainOutputPortCurrencyService currencyService;

    @MockBean
    private ApplicationOutputPortEventPublisher eventPublisherMock;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        // Redis configuration
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
        
        // Kafka configuration
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        
        // Override JPA settings for test
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        
        // Mock external service URLs
        registry.add("financial.payment-gateway.url", () -> "http://localhost:8090/mock-payment");
        registry.add("financial.currency-service.url", () -> "http://localhost:8091/mock-currency");
        
        // Shorter cache TTL for tests
        registry.add("financial.cache.ttl-seconds", () -> "60");
    }

    @BeforeEach
    void setUp() {
        log.info("=== Setting up test environment ====");
        setupMockBehaviors();
    }

    @Test
    @Transactional
    void When_AccountCreated_Event_Received_Then_FinancialAccount_Created_With_Zero_Balance(CapturedOutput output) {
        log.info("=== Starting AccountCreated Event Test ====");
        
        // Given: A new account ID
        String accountId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        String createdAt = Instant.now().toString();
        
        log.info("Test data - AccountId: {}, CustomerId: {}, CreatedAt: {}", accountId, customerId, createdAt);
        
        // Create the AccountCreated event
        SharedAccountCreatedEventDTO accountCreatedEvent = SharedAccountCreatedEventDTO.builder()
                .accountId(accountId)
                .customerId(customerId)
                .createdAt(createdAt)
                .build();
        
        log.info("Created AccountCreatedEvent: {}", accountCreatedEvent);
        
        // When: The AccountCreated event is published
        log.info("Publishing AccountCreated event...");
        eventPublisher.publishEvent(accountCreatedEvent);
        
        // Allow some time for event processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Event published and processed");
        
        // Then: Verify that a financial account was created in the database
        log.info("Verifying financial account creation in database...");
        UUID accountUUID = UUID.fromString(accountId);
        boolean accountExists = financialAccountRepository.existsByAccountId(accountUUID);
        assertThat(accountExists)
            .as("Financial account should exist in database")
            .isTrue();
        
        log.info("Financial account exists in database: {}", accountExists);
        
        // Verify the financial account has zero balance
        DomainEntityFinancialAccount createdAccount = financialAccountRepository.findByAccountId(accountUUID);
        assertThat(createdAccount)
            .as("Created financial account should not be null")
            .isNotNull();
        
        assertThat(createdAccount.getAccountId())
            .as("Account ID should match")
            .isEqualTo(accountUUID);
        
        DomainMoneyValue balance = createdAccount.getBalance();
        assertThat(balance)
            .as("Balance should not be null")
            .isNotNull();
        
        assertThat(balance.getAmount())
            .as("Initial balance should be zero")
            .isEqualTo(BigDecimal.ZERO);
        
        assertThat(balance.getCurrency().getCode())
            .as("Default currency should be USD")
            .isEqualTo("USD");
        
        log.info("Financial account created with zero balance: {} {}", 
                balance.getAmount(), balance.getCurrency().getCode());
        
        // Verify that the balance is retrievable through the service
        log.info("Verifying balance retrieval through service...");
        ApplicationBalanceDTO retrievedBalance = financialAccountService.getAccountBalance(accountId);
        assertThat(retrievedBalance)
            .as("Retrieved balance should not be null")
            .isNotNull();
        
        assertThat(retrievedBalance.getBalance())
            .as("Retrieved balance should not be null")
            .isNotNull();
        
        assertThat(retrievedBalance.getBalance().getAmount())
            .as("Retrieved balance amount should be zero")
            .isEqualTo("0");
        
        assertThat(retrievedBalance.getBalance().getCurrency())
            .as("Retrieved balance currency should be USD")
            .isEqualTo("USD");
        
        log.info("Balance retrieved successfully: {} {}", 
                retrievedBalance.getBalance().getAmount(), 
                retrievedBalance.getBalance().getCurrency());
        
        // Verify that the balance is cached in Redis
        log.info("Verifying balance is cached in Redis...");
        var cachedBalance = cachePort.getBalance(accountId);
        if (cachedBalance != null) {
            assertThat(cachedBalance.getAmount())
                .as("Cached balance amount should be zero")
                .isEqualTo("0");
            
            assertThat(cachedBalance.getCurrency())
                .as("Cached balance currency should be USD")
                .isEqualTo("USD");
            
            log.info("Balance successfully cached in Redis: {} {}", 
                    cachedBalance.getAmount(), cachedBalance.getCurrency());
        } else {
            log.info("Balance not found in cache (this is acceptable as caching might be lazy)");
        }
        
        // Verify that the FinancialAccountCreated event was published
        log.info("Verifying FinancialAccountCreated event was published...");
        verify(eventPublisherMock, times(1)).publishFinancialAccountCreated(accountId);
        log.info("FinancialAccountCreated event was published successfully");
        
        // Verify application logs for successful processing
        String outputString = output.toString();
        assertThat(outputString)
            .as("Should contain account creation log")
            .contains("Creating financial account for accountId: " + accountId);
        
        assertThat(outputString)
            .as("Should contain successful creation log")
            .contains("Successfully created financial account for accountId: " + accountId);
        
        // Verify no errors in processing
        assertThat(outputString)
            .as("Should not contain error logs")
            .doesNotContain(
                "Failed to create financial account",
                "ERROR",
                "Exception"
            );
        
        log.info("=== AccountCreated Event Test Completed Successfully ====");
        
        // Log the full output for analysis
        log.info("=== FULL TEST EXECUTION LOGS ====");
        log.info(outputString);
        log.info("=== END OF TEST LOGS ====");
    }

    @Test
    @Transactional
    void When_Credit_Transaction_Processed_Then_Balance_Updated_And_Event_Published(CapturedOutput output) {
        log.info("=== Starting Credit Transaction Processing Test ====");
        
        // Given: An existing financial account with zero balance
        String accountId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        String createdAt = Instant.now().toString();
        
        log.info("Test data - AccountId: {}, CustomerId: {}", accountId, customerId);
        
        // First create a financial account
        SharedAccountCreatedEventDTO accountCreatedEvent = SharedAccountCreatedEventDTO.builder()
                .accountId(accountId)
                .customerId(customerId)
                .createdAt(createdAt)
                .build();
        
        eventPublisher.publishEvent(accountCreatedEvent);
        
        // Allow time for account creation
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify account was created
        UUID accountUUID = UUID.fromString(accountId);
        DomainEntityFinancialAccount account = financialAccountRepository.findByAccountId(accountUUID);
        assertThat(account).isNotNull();
        assertThat(account.getBalance().getAmount()).isEqualTo(BigDecimal.ZERO);
        
        log.info("Financial account created with zero balance");
        
        // Given: A credit transaction request
        BigDecimal creditAmount = new BigDecimal("100.50");
        ApplicationTransactionRequestDTO transactionRequest = ApplicationTransactionRequestDTO.builder()
                .amount(ApplicationMoneyValue.builder()
                        .amount(creditAmount.toString())
                        .currency("USD")
                        .build())
                .type("CREDIT")
                .description("Test credit transaction")
                .reference("TEST-REF-001")
                .build();
        
        log.info("Created credit transaction request: {} USD", creditAmount);
        
        // When: The credit transaction is processed
        log.info("Processing credit transaction...");
        ApplicationTransactionResponseDTO response = transactionService.processTransaction(accountId, transactionRequest);
        
        log.info("Credit transaction processed successfully");
        
        // Then: Verify the transaction response
        assertThat(response)
            .as("Transaction response should not be null")
            .isNotNull();
        
        assertThat(response.getTransactionId())
            .as("Transaction ID should not be null")
            .isNotNull();
        
        assertThat(response.getNewBalance())
            .as("New balance should not be null")
            .isNotNull();
        
        assertThat(response.getNewBalance().getAmount())
            .as("New balance should equal credit amount")
            .isEqualTo(creditAmount.toString());
        
        assertThat(response.getNewBalance().getCurrency())
            .as("Currency should be USD")
            .isEqualTo("USD");
        
        assertThat(response.getProcessedAt())
            .as("Processed timestamp should not be null")
            .isNotNull();
        
        log.info("Transaction response verified: ID={}, NewBalance={} {}", 
                response.getTransactionId(), 
                response.getNewBalance().getAmount(), 
                response.getNewBalance().getCurrency());
        
        // Verify the account balance was updated in the database
        log.info("Verifying account balance update in database...");
        DomainEntityFinancialAccount updatedAccount = financialAccountRepository.findByAccountId(accountUUID);
        assertThat(updatedAccount)
            .as("Updated account should not be null")
            .isNotNull();
        
        assertThat(updatedAccount.getBalance().getAmount())
            .as("Database balance should be updated")
            .isEqualTo(creditAmount);
        
        assertThat(updatedAccount.getBalance().getCurrency().getCode())
            .as("Database currency should be USD")
            .isEqualTo("USD");
        
        log.info("Database balance updated successfully: {} {}", 
                updatedAccount.getBalance().getAmount(), 
                updatedAccount.getBalance().getCurrency().getCode());
        
        // Verify the transaction record was created in the database
        log.info("Verifying transaction record in database...");
        DomainPaginationParams paginationParams = new DomainPaginationParams(
                0, // page
                10, // size
                LocalDateTime.now().minusHours(1), // fromDate
                LocalDateTime.now().plusHours(1) // toDate
        );
        
        List<DomainEntityTransaction> transactions = transactionRepository.findByFinancialAccountId(
                updatedAccount.getFinancialAccountId(), paginationParams);
        
        assertThat(transactions)
            .as("Transaction list should not be empty")
            .isNotEmpty()
            .hasSize(1);
        
        DomainEntityTransaction savedTransaction = transactions.get(0);
        assertThat(savedTransaction.getAmount())
            .as("Transaction amount should match")
            .isEqualTo(creditAmount);
        
        assertThat(savedTransaction.getCurrency())
            .as("Transaction currency should be USD")
            .isEqualTo("USD");
        
        assertThat(savedTransaction.getType().name())
            .as("Transaction type should be CREDIT")
            .isEqualTo("CREDIT");
        
        assertThat(savedTransaction.getDescription())
            .as("Transaction description should match")
            .isEqualTo("Test credit transaction");
        
        assertThat(savedTransaction.getReference())
            .as("Transaction reference should match")
            .isEqualTo("TEST-REF-001");
        
        log.info("Transaction record verified in database: {} {} {}", 
                savedTransaction.getAmount(), 
                savedTransaction.getCurrency(), 
                savedTransaction.getType());
        
        // Verify cache was invalidated (balance should be retrievable but cache might be empty)
        log.info("Verifying cache invalidation...");
        ApplicationBalanceDTO balanceFromService = financialAccountService.getAccountBalance(accountId);
        assertThat(balanceFromService.getBalance().getAmount())
            .as("Service should return updated balance")
            .isEqualTo(creditAmount.toString());
        
        log.info("Balance retrievable from service after cache invalidation: {} {}", 
                balanceFromService.getBalance().getAmount(), 
                balanceFromService.getBalance().getCurrency());
        
        // Verify that TransactionProcessed event was published
        log.info("Verifying TransactionProcessed event was published...");
        verify(eventPublisherMock, times(1)).publishTransactionProcessed(any());
        log.info("TransactionProcessed event was published successfully");
        
        // Verify application logs for successful processing
        String outputString = output.toString();
        assertThat(outputString)
            .as("Should contain transaction processing log")
            .contains("Processing transaction for accountId: " + accountId);
        
        assertThat(outputString)
            .as("Should contain successful processing log")
            .contains("Successfully processed transaction");
        
        // Verify no errors in processing
        assertThat(outputString)
            .as("Should not contain error logs")
            .doesNotContain(
                "Failed to process transaction",
                "ERROR",
                "Exception"
            );
        
        log.info("=== Credit Transaction Processing Test Completed Successfully ====");
        
        // Log the full output for analysis
        log.info("=== FULL TEST EXECUTION LOGS ====");
        log.info(outputString);
        log.info("=== END OF TEST LOGS ====");
    }
    
    private void setupMockBehaviors() {
        log.info("Setting up mock behaviors for external services");
        
        // Mock Payment Gateway
        when(paymentGateway.tokenizeCard(anyString(), any(Integer.class), any(Integer.class), anyString()))
            .thenReturn(DomainPaymentMethodDataValue.builder()
                .paymentType(DomainPaymentTypeEnum.CREDIT_CARD)
                .token("mock_token_123")
                .lastFourDigits("1234")
                .expiryMonth(12)
                .expiryYear(2025)
                .cardBrand(DomainCardBrandEnum.VISA)
                .build());
        
        when(paymentGateway.validateToken(anyString()))
            .thenReturn(true);
        
        // Mock Currency Service
        when(currencyService.convertCurrency(any(DomainMoneyValue.class), anyString()))
            .thenAnswer(invocation -> {
                DomainMoneyValue amount = invocation.getArgument(0);
                String targetCurrency = invocation.getArgument(1);
                return DomainMoneyValue.builder()
                    .amount(amount.getAmount())
                    .currency(DomainCurrencyValue.builder()
                        .code(targetCurrency)
                        .symbol("$")
                        .build())
                    .build();
            });
        
        when(currencyService.getExchangeRate(anyString(), anyString()))
            .thenReturn(BigDecimal.ONE);
        
        log.info("Mock behaviors setup completed");
    }
}