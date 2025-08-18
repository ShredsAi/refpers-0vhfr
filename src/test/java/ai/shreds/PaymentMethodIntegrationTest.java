package ai.shreds;

import ai.shreds.application.dtos.ApplicationPaymentMethodRequestDTO;
import ai.shreds.application.dtos.ApplicationPaymentMethodResponseDTO;
import ai.shreds.application.dtos.ApplicationPaymentMethodActivationDTO;
import ai.shreds.application.ports.ApplicationInputPortPaymentMethod;
import ai.shreds.application.ports.ApplicationInputPortFinancialAccount;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.domain.ports.DomainOutputPortPaymentMethodRepository;
import ai.shreds.domain.ports.DomainOutputPortPaymentGateway;
import ai.shreds.domain.ports.DomainOutputPortCurrencyService;
import ai.shreds.domain.entities.DomainEntityPaymentMethod;
import ai.shreds.domain.value_objects.DomainPaymentMethodDataValue;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.domain.value_objects.DomainCurrencyValue;
import ai.shreds.domain.enums.DomainPaymentTypeEnum;
import ai.shreds.domain.enums.DomainCardBrandEnum;
import ai.shreds.infrastructure.repositories.InfrastructurePaymentMethodJpaEntity;
import ai.shreds.infrastructure.repositories.InfrastructurePaymentMethodJpaRepository;
import ai.shreds.shared.dtos.SharedAccountCreatedEventDTO;
import ai.shreds.shared.dtos.SharedPaymentMethodRequestDTO;
import ai.shreds.shared.dtos.SharedPaymentMethodResponseDTO;
import ai.shreds.shared.dtos.SharedPaymentMethodActivationResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
class PaymentMethodIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationInputPortPaymentMethod paymentMethodService;

    @Autowired
    private ApplicationInputPortFinancialAccount financialAccountService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private DomainOutputPortPaymentMethodRepository paymentMethodRepository;

    @Autowired
    private InfrastructurePaymentMethodJpaRepository paymentMethodJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
        log.info("=== Setting up PaymentMethod test environment ====");
        setupMockBehaviors();
    }

    @Test
    @Transactional
    void When_Payment_Method_Added_Then_Card_Tokenized_And_Stored_Securely(CapturedOutput output) {
        log.info("=== Starting Payment Method Addition Test ====");
        
        // Given: A financial account exists
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
        
        log.info("Financial account created for testing");
        
        // Given: A payment method request with card details
        SharedPaymentMethodRequestDTO paymentMethodRequest = SharedPaymentMethodRequestDTO.builder()
                .accountId(accountId)
                .cardNumber("4111111111111111") // Test Visa card number
                .expiryMonth(12)
                .expiryYear(2025)
                .cvv("123")
                .billingAddressLine1("123 Test Street")
                .billingAddressLine2("Apt 4B")
                .billingCity("Test City")
                .billingState("Test State")
                .billingPostalCode("12345")
                .billingCountry("US")
                .build();
        
        log.info("Created payment method request for card ending in: {}", 
                paymentMethodRequest.getCardNumber().substring(paymentMethodRequest.getCardNumber().length() - 4));
        
        // When: The payment method is added via REST API
        log.info("Adding payment method via REST API...");
        String url = "http://localhost:" + port + "/api/v1/payment-methods";
        
        ResponseEntity<SharedPaymentMethodResponseDTO> response = restTemplate.postForEntity(
                url, paymentMethodRequest, SharedPaymentMethodResponseDTO.class);
        
        log.info("Payment method addition API call completed");
        
        // Then: Verify the HTTP response
        assertThat(response.getStatusCode())
            .as("HTTP status should be CREATED")
            .isEqualTo(HttpStatus.CREATED);
        
        assertThat(response.getBody())
            .as("Response body should not be null")
            .isNotNull();
        
        SharedPaymentMethodResponseDTO responseBody = response.getBody();
        
        assertThat(responseBody.getPaymentMethodId())
            .as("Payment method ID should not be null")
            .isNotNull();
        
        assertThat(responseBody.getLastFourDigits())
            .as("Last four digits should match card number")
            .isEqualTo("1111");
        
        assertThat(responseBody.getCardBrand())
            .as("Card brand should be VISA")
            .isEqualTo("VISA");
        
        assertThat(responseBody.getIsDefault())
            .as("Should be set as default (first payment method)")
            .isTrue();
        
        log.info("HTTP response verified: PaymentMethodId={}, LastFourDigits={}, CardBrand={}, IsDefault={}", 
                responseBody.getPaymentMethodId(), responseBody.getLastFourDigits(), 
                responseBody.getCardBrand(), responseBody.getIsDefault());
        
        // Verify that the payment gateway was called for tokenization
        log.info("Verifying payment gateway tokenization was called...");
        verify(paymentGateway, times(1)).tokenizeCard(
                eq("4111111111111111"), 
                eq(12), 
                eq(2025), 
                eq("123")
        );
        log.info("Payment gateway tokenization verified");
        
        // Verify that the payment method was stored in the database with tokenized data
        log.info("Verifying payment method storage in database...");
        UUID paymentMethodId = UUID.fromString(responseBody.getPaymentMethodId());
        
        // Check via JPA repository
        var jpaEntity = paymentMethodJpaRepository.findById(paymentMethodId);
        assertThat(jpaEntity)
            .as("Payment method should exist in database")
            .isPresent();
        
        InfrastructurePaymentMethodJpaEntity savedEntity = jpaEntity.get();
        
        assertThat(savedEntity.getAccountId())
            .as("Account ID should match")
            .isEqualTo(UUID.fromString(accountId));
        
        assertThat(savedEntity.getToken())
            .as("Token should be stored (from payment gateway)")
            .isEqualTo("mock_token_123");
        
        assertThat(savedEntity.getLastFourDigits())
            .as("Last four digits should be stored")
            .isEqualTo("1111");
        
        assertThat(savedEntity.getCardBrand())
            .as("Card brand should be stored")
            .isEqualTo("VISA");
        
        assertThat(savedEntity.getPaymentType())
            .as("Payment type should be CREDIT_CARD")
            .isEqualTo("CREDIT_CARD");
        
        assertThat(savedEntity.getIsDefault())
            .as("Should be marked as default")
            .isTrue();
        
        assertThat(savedEntity.getIsActive())
            .as("Should be active by default")
            .isTrue();
        
        // Verify billing address is stored
        assertThat(savedEntity.getBillingAddressLine1())
            .as("Billing address line 1 should be stored")
            .isEqualTo("123 Test Street");
        
        assertThat(savedEntity.getBillingAddressLine2())
            .as("Billing address line 2 should be stored")
            .isEqualTo("Apt 4B");
        
        assertThat(savedEntity.getBillingCity())
            .as("Billing city should be stored")
            .isEqualTo("Test City");
        
        assertThat(savedEntity.getBillingState())
            .as("Billing state should be stored")
            .isEqualTo("Test State");
        
        assertThat(savedEntity.getBillingPostalCode())
            .as("Billing postal code should be stored")
            .isEqualTo("12345");
        
        assertThat(savedEntity.getBillingCountry())
            .as("Billing country should be stored")
            .isEqualTo("US");
        
        assertThat(savedEntity.getAddedAt())
            .as("Added timestamp should be set")
            .isNotNull();
        
        assertThat(savedEntity.getUpdatedAt())
            .as("Updated timestamp should be set")
            .isNotNull();
        
        log.info("Database storage verified: Token={}, LastFour={}, Brand={}, IsDefault={}, IsActive={}", 
                savedEntity.getToken(), savedEntity.getLastFourDigits(), savedEntity.getCardBrand(), 
                savedEntity.getIsDefault(), savedEntity.getIsActive());
        
        // Verify that the PaymentMethodAdded event was published
        log.info("Verifying PaymentMethodAdded event was published...");
        verify(eventPublisherMock, times(1)).publishPaymentMethodAdded(
                eq(responseBody.getPaymentMethodId()), 
                eq(accountId)
        );
        log.info("PaymentMethodAdded event publication verified");
        
        // Verify that sensitive card data is NOT stored in database
        log.info("Verifying sensitive card data is not stored...");
        String entityAsString = savedEntity.toString();
        assertThat(entityAsString)
            .as("Full card number should not be stored")
            .doesNotContain("4111111111111111");
        
        assertThat(entityAsString)
            .as("CVV should not be stored")
            .doesNotContain("123");
        
        log.info("Verified sensitive card data is not stored in database");
        
        // Verify application logs for successful processing
        String outputString = output.toString();
        assertThat(outputString)
            .as("Should contain payment method addition log")
            .contains("Adding payment method for accountId: " + accountId);
        
        assertThat(outputString)
            .as("Should contain successful addition log")
            .contains("Successfully added payment method");
        
        // Verify no errors in processing
        assertThat(outputString)
            .as("Should not contain error logs")
            .doesNotContain(
                "Failed to add payment method",
                "ERROR",
                "Exception"
            );
        
        log.info("=== Payment Method Addition Test Completed Successfully ====");
        
        // Log the full output for analysis
        log.info("=== FULL TEST EXECUTION LOGS ====");
        log.info(outputString);
        log.info("=== END OF TEST LOGS ====");
    }

    @Test
    @Transactional
    void When_Payment_Method_Activated_Then_Status_Updated_And_Available_For_Use(CapturedOutput output) {
        log.info("=== Starting Payment Method Activation Test ====");
        
        // Given: A financial account exists
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
        
        log.info("Financial account created for testing");
        
        // Given: A payment method is added first
        SharedPaymentMethodRequestDTO paymentMethodRequest = SharedPaymentMethodRequestDTO.builder()
                .accountId(accountId)
                .cardNumber("4111111111111111") // Test Visa card number
                .expiryMonth(12)
                .expiryYear(2025)
                .cvv("123")
                .billingAddressLine1("123 Test Street")
                .billingAddressLine2("Apt 4B")
                .billingCity("Test City")
                .billingState("Test State")
                .billingPostalCode("12345")
                .billingCountry("US")
                .build();
        
        log.info("Adding payment method first...");
        String addUrl = "http://localhost:" + port + "/api/v1/payment-methods";
        
        ResponseEntity<SharedPaymentMethodResponseDTO> addResponse = restTemplate.postForEntity(
                addUrl, paymentMethodRequest, SharedPaymentMethodResponseDTO.class);
        
        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(addResponse.getBody()).isNotNull();
        
        String paymentMethodId = addResponse.getBody().getPaymentMethodId();
        log.info("Payment method added with ID: {}", paymentMethodId);
        
        // Manually deactivate the payment method to test activation
        UUID paymentMethodUuid = UUID.fromString(paymentMethodId);
        var jpaEntity = paymentMethodJpaRepository.findById(paymentMethodUuid);
        assertThat(jpaEntity).isPresent();
        
        InfrastructurePaymentMethodJpaEntity entity = jpaEntity.get();
        entity.setIsActive(false); // Deactivate for testing
        paymentMethodJpaRepository.save(entity);
        
        log.info("Payment method deactivated for testing activation flow");
        
        // Verify it's deactivated
        var deactivatedEntity = paymentMethodJpaRepository.findById(paymentMethodUuid);
        assertThat(deactivatedEntity.get().getIsActive())
            .as("Payment method should be deactivated before test")
            .isFalse();
        
        // When: The payment method is activated via REST API
        log.info("Activating payment method via REST API...");
        String activateUrl = "http://localhost:" + port + "/api/v1/payment-methods/" + paymentMethodId + "/activate";
        
        ResponseEntity<SharedPaymentMethodActivationResponseDTO> activationResponse = restTemplate.exchange(
                activateUrl, 
                HttpMethod.PUT, 
                HttpEntity.EMPTY, 
                SharedPaymentMethodActivationResponseDTO.class
        );
        
        log.info("Payment method activation API call completed");
        
        // Then: Verify the HTTP response
        assertThat(activationResponse.getStatusCode())
            .as("HTTP status should be OK")
            .isEqualTo(HttpStatus.OK);
        
        assertThat(activationResponse.getBody())
            .as("Response body should not be null")
            .isNotNull();
        
        SharedPaymentMethodActivationResponseDTO activationResponseBody = activationResponse.getBody();
        
        assertThat(activationResponseBody.getPaymentMethodId())
            .as("Payment method ID should match")
            .isEqualTo(paymentMethodId);
        
        assertThat(activationResponseBody.getIsActive())
            .as("Payment method should be active")
            .isTrue();
        
        assertThat(activationResponseBody.getActivatedAt())
            .as("Activation timestamp should be set")
            .isNotNull();
        
        log.info("HTTP response verified: PaymentMethodId={}, IsActive={}, ActivatedAt={}", 
                activationResponseBody.getPaymentMethodId(), 
                activationResponseBody.getIsActive(), 
                activationResponseBody.getActivatedAt());
        
        // Verify that the payment method status was updated in the database
        log.info("Verifying payment method status update in database...");
        
        var updatedEntity = paymentMethodJpaRepository.findById(paymentMethodUuid);
        assertThat(updatedEntity)
            .as("Payment method should exist in database")
            .isPresent();
        
        InfrastructurePaymentMethodJpaEntity activatedEntity = updatedEntity.get();
        
        assertThat(activatedEntity.getIsActive())
            .as("Payment method should be active in database")
            .isTrue();
        
        assertThat(activatedEntity.getUpdatedAt())
            .as("Updated timestamp should be recent")
            .isNotNull()
            .isAfter(entity.getUpdatedAt());
        
        log.info("Database status update verified: IsActive={}, UpdatedAt={}", 
                activatedEntity.getIsActive(), activatedEntity.getUpdatedAt());
        
        // Verify that the payment method is available for use (can be retrieved via domain service)
        log.info("Verifying payment method is available for use via domain service...");
        
        try {
            DomainEntityPaymentMethod domainEntity = paymentMethodRepository.findById(paymentMethodUuid);
            assertThat(domainEntity)
                .as("Domain entity should be retrievable")
                .isNotNull();
            
            assertThat(domainEntity.isActive())
                .as("Domain entity should show as active")
                .isTrue();
            
            log.info("Domain service verification successful: PaymentMethod is active and available");
        } catch (Exception e) {
            log.error("Failed to retrieve payment method via domain service", e);
            throw e;
        }
        
        // Verify application logs for successful activation
        String outputString = output.toString();
        assertThat(outputString)
            .as("Should contain payment method activation log")
            .contains("Activating payment method with ID: " + paymentMethodId);
        
        assertThat(outputString)
            .as("Should contain successful activation log")
            .contains("Successfully activated payment method");
        
        // Verify no errors in processing
        assertThat(outputString)
            .as("Should not contain error logs")
            .doesNotContain(
                "Failed to activate payment method",
                "ERROR",
                "Exception"
            );
        
        log.info("=== Payment Method Activation Test Completed Successfully ====");
        
        // Log the full output for analysis
        log.info("=== FULL TEST EXECUTION LOGS ====");
        log.info(outputString);
        log.info("=== END OF TEST LOGS ====");
    }
    
    private void setupMockBehaviors() {
        log.info("Setting up mock behaviors for external services");
        
        // Mock Payment Gateway - return tokenized card data
        when(paymentGateway.tokenizeCard(anyString(), any(Integer.class), any(Integer.class), anyString()))
            .thenReturn(DomainPaymentMethodDataValue.builder()
                .paymentType(DomainPaymentTypeEnum.CREDIT_CARD)
                .token("mock_token_123")
                .lastFourDigits("1111")
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