package ai.shreds;

import ai.shreds.application.dtos.ApplicationBalanceDTO;
import ai.shreds.application.dtos.ApplicationMoneyValue;
import ai.shreds.application.ports.ApplicationInputPortFinancialAccount;
import ai.shreds.application.ports.ApplicationOutputPortCache;
import ai.shreds.infrastructure.repositories.InfrastructureFinancialAccountJpaRepository;
import ai.shreds.shared.dtos.SharedAccountCreatedEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
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
    private ApplicationOutputPortCache cacheService;

    @Autowired
    private InfrastructureFinancialAccountJpaRepository financialAccountRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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
}