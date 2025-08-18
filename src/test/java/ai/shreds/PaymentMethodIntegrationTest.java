package ai.shreds;

import ai.shreds.application.ports.ApplicationInputPortPaymentMethod;
import ai.shreds.infrastructure.external_services.InfrastructurePaymentGatewayClient;
import ai.shreds.infrastructure.external_services.InfrastructurePaymentGatewayResponseDTO;
import ai.shreds.infrastructure.repositories.InfrastructurePaymentMethodJpaRepository;
import ai.shreds.shared.dtos.SharedPaymentMethodRequestDTO;
import ai.shreds.shared.dtos.SharedPaymentMethodResponseDTO;
import ai.shreds.shared.dtos.SharedPaymentMethodActivationResponseDTO;
import ai.shreds.shared.dtos.SharedErrorResponseDTO;
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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Integration test for payment method lifecycle including tokenization with external payment gateway,
 * database persistence, and activation workflow.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class PaymentMethodIntegrationTest {

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
    private ApplicationInputPortPaymentMethod paymentMethodService;

    @Autowired
    private InfrastructurePaymentMethodJpaRepository paymentMethodRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private InfrastructurePaymentGatewayClient paymentGatewayClient;

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
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        
        System.out.println("✅ Test setup completed - database and cache cleared");
    }

    @Test
    @Transactional
    void When_PaymentMethod_Added_Then_Card_Tokenized_And_Stored_Securely() {
        // Given
        String accountId = UUID.randomUUID().toString();
        String cardNumber = "4111111111111111";
        String expectedToken = "tok_1J2x3y4z5a6b7c8d9e0f";
        String expectedLastFour = "1111";
        String expectedCardBrand = "VISA";
        
        // Mock the payment gateway tokenization response
        InfrastructurePaymentGatewayResponseDTO mockGatewayResponse = InfrastructurePaymentGatewayResponseDTO.builder()
                .token(expectedToken)
                .cardBrand(expectedCardBrand)
                .last4(expectedLastFour)
                .build();
        
        when(paymentGatewayClient.tokenizeCard(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(mockGatewayResponse.toDomainPaymentMethodData());
        
        // Prepare payment method request
        SharedPaymentMethodRequestDTO paymentMethodRequest = SharedPaymentMethodRequestDTO.builder()
                .accountId(accountId)
                .cardNumber(cardNumber)
                .expiryMonth(12)
                .expiryYear(2025)
                .cvv("123")
                .billingAddressLine1("123 Main St")
                .billingAddressLine2("Apt 4B")
                .billingCity("New York")
                .billingState("NY")
                .billingPostalCode("10001")
                .billingCountry("US")
                .build();
        
        System.out.println("🧪 Test: Adding payment method for accountId: " + accountId);
        System.out.println("🧪 Card number (masked): ****-****-****-" + expectedLastFour);
        
        // When - Add payment method via REST API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SharedPaymentMethodRequestDTO> requestEntity = new HttpEntity<>(paymentMethodRequest, headers);
        
        String paymentMethodUrl = "http://localhost:" + port + "/payment-methods";
        ResponseEntity<SharedPaymentMethodResponseDTO> response = restTemplate.exchange(
                paymentMethodUrl, HttpMethod.POST, requestEntity, SharedPaymentMethodResponseDTO.class);
        
        // Then - Verify payment method response
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Payment method should be created successfully");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        SharedPaymentMethodResponseDTO paymentMethodResponse = response.getBody();
        assertNotNull(paymentMethodResponse.getPaymentMethodId(), "Payment method ID should be generated");
        assertEquals(expectedLastFour, paymentMethodResponse.getLastFourDigits(), "Last four digits should match");
        assertEquals(expectedCardBrand, paymentMethodResponse.getCardBrand(), "Card brand should match");
        assertTrue(paymentMethodResponse.getIsDefault(), "First payment method should be set as default");
        
        System.out.println("✅ Payment method created successfully with ID: " + paymentMethodResponse.getPaymentMethodId());
        System.out.println("✅ Last four digits: " + paymentMethodResponse.getLastFourDigits());
        System.out.println("✅ Card brand: " + paymentMethodResponse.getCardBrand());
        System.out.println("✅ Is default: " + paymentMethodResponse.getIsDefault());
        
        // Verify external payment gateway was called for tokenization
        verify(paymentGatewayClient, times(1)).tokenizeCard(
                cardNumber, 12, 2025, "123");
        System.out.println("✅ Payment gateway tokenization API was called correctly");
        
        // Verify database persistence
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var storedPaymentMethods = paymentMethodRepository.findByAccountId(UUID.fromString(accountId));
            assertEquals(1, storedPaymentMethods.size(), "One payment method should be stored in database");
            
            var storedPaymentMethod = storedPaymentMethods.get(0);
            assertEquals(UUID.fromString(accountId), storedPaymentMethod.getAccountId(), "Account ID should match");
            assertEquals(expectedToken, storedPaymentMethod.getToken(), "Token should be stored securely");
            assertEquals(expectedLastFour, storedPaymentMethod.getLastFourDigits(), "Last four digits should be stored");
            assertEquals(expectedCardBrand, storedPaymentMethod.getCardBrand(), "Card brand should be stored");
            assertEquals("CREDIT_CARD", storedPaymentMethod.getPaymentType(), "Payment type should be CREDIT_CARD");
            assertEquals(12, storedPaymentMethod.getExpiryMonth(), "Expiry month should be stored");
            assertEquals(2025, storedPaymentMethod.getExpiryYear(), "Expiry year should be stored");
            assertTrue(storedPaymentMethod.getIsDefault(), "Should be set as default");
            assertTrue(storedPaymentMethod.getIsActive(), "Should be active by default");
            
            // Verify billing address is stored
            assertEquals("123 Main St", storedPaymentMethod.getBillingAddressLine1(), "Billing address line 1 should be stored");
            assertEquals("Apt 4B", storedPaymentMethod.getBillingAddressLine2(), "Billing address line 2 should be stored");
            assertEquals("New York", storedPaymentMethod.getBillingCity(), "Billing city should be stored");
            assertEquals("NY", storedPaymentMethod.getBillingState(), "Billing state should be stored");
            assertEquals("10001", storedPaymentMethod.getBillingPostalCode(), "Billing postal code should be stored");
            assertEquals("US", storedPaymentMethod.getBillingCountry(), "Billing country should be stored");
            
            // Verify timestamps
            assertNotNull(storedPaymentMethod.getAddedAt(), "Added timestamp should be set");
            assertNotNull(storedPaymentMethod.getUpdatedAt(), "Updated timestamp should be set");
            
            System.out.println("✅ Payment method stored in database with token: " + storedPaymentMethod.getToken().substring(0, 10) + "...");
            System.out.println("✅ Billing address stored: " + storedPaymentMethod.getBillingAddressLine1() + ", " + storedPaymentMethod.getBillingCity());
        });
        
        // Verify Kafka event publishing for PaymentMethodAdded
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
            assertTrue(capturedMessage.contains("PAYMENT_METHOD_ADDED"), "Event should contain PAYMENT_METHOD_ADDED type");
            assertTrue(capturedMessage.contains(accountId), "Event should contain account ID");
            assertTrue(capturedMessage.contains(paymentMethodResponse.getPaymentMethodId()), "Event should contain payment method ID");
            
            System.out.println("✅ Kafka event published successfully to topic: " + capturedTopic);
            System.out.println("✅ Event message contains: " + capturedMessage.substring(0, Math.min(200, capturedMessage.length())) + "...");
        });
        
        // Verify security: sensitive card data is not stored
        var allPaymentMethods = paymentMethodRepository.findAll();
        assertEquals(1, allPaymentMethods.size(), "Only one payment method should exist");
        
        var securityCheck = allPaymentMethods.get(0);
        assertNotEquals(cardNumber, securityCheck.getToken(), "Original card number should not be stored as token");
        assertFalse(securityCheck.getToken().contains(cardNumber), "Token should not contain original card number");
        assertEquals(expectedToken, securityCheck.getToken(), "Only the secure token should be stored");
        
        System.out.println("✅ Security verified: Original card number is not stored, only secure token");
        System.out.println("✅ Test completed successfully: Payment method added with card tokenization, secure storage, and event publishing");
    }
}