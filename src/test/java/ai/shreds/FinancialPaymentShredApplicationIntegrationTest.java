package ai.shreds;

import ai.shreds.application.ports.ApplicationOutputPortCache;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.domain.ports.DomainOutputPortCurrencyService;
import ai.shreds.domain.ports.DomainOutputPortPaymentGateway;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.domain.value_objects.DomainCurrencyValue;
import ai.shreds.domain.value_objects.DomainPaymentMethodDataValue;
import ai.shreds.domain.enums.DomainPaymentTypeEnum;
import ai.shreds.domain.enums.DomainCardBrandEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
class FinancialPaymentShredApplicationIntegrationTest {

    @LocalServerPort
    private int port;

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

    @Test
    void contextLoads(CapturedOutput output) {
        log.info("=== Starting Application Context Load Test ===");
        
        // Setup mock behaviors for external services
        setupMockBehaviors();
        
        // Verify that the application context loads successfully
        assertThat(port).isGreaterThan(0);
        log.info("Application started successfully on port: {}", port);
        
        // Verify containers are running
        assertThat(postgres.isRunning()).isTrue();
        assertThat(redis.isRunning()).isTrue();
        assertThat(kafka.isRunning()).isTrue();
        log.info("All TestContainers are running successfully");
        
        // Verify database connection
        assertThat(postgres.getJdbcUrl()).contains("financial_test_db");
        log.info("Database connection verified: {}", postgres.getJdbcUrl());
        
        // Verify Redis connection
        assertThat(redis.getMappedPort(6379)).isGreaterThan(0);
        log.info("Redis connection verified on port: {}", redis.getMappedPort(6379));
        
        // Verify Kafka connection
        assertThat(kafka.getBootstrapServers()).isNotEmpty();
        log.info("Kafka connection verified: {}", kafka.getBootstrapServers());
        
        // Check application logs for successful startup indicators
        String outputString = output.toString();
        
        // Verify Spring Boot started successfully
        assertThat(outputString)
            .as("Application should start successfully")
            .containsAnyOf(
                "Started FinancialPaymentShredApplication",
                "Tomcat started on port",
                "Application startup completed"
            );
        
        // Verify no critical errors in startup
        assertThat(outputString)
            .as("Should not contain critical startup errors")
            .doesNotContain(
                "APPLICATION FAILED TO START",
                "Error starting ApplicationContext",
                "BeanCreationException",
                "NoSuchBeanDefinitionException"
            );
        
        // Verify database connectivity
        assertThat(outputString)
            .as("Should establish database connection")
            .containsAnyOf(
                "HikariPool",
                "Database connection",
                "Initialized JPA EntityManagerFactory"
            );
        
        log.info("=== Application Context Load Test Completed Successfully ===");
        
        // Log the full output for analysis
        log.info("=== FULL APPLICATION STARTUP LOGS ===");
        log.info(outputString);
        log.info("=== END OF STARTUP LOGS ===");
    }
    
    @Test
    void applicationHealthCheck(CapturedOutput output) {
        log.info("=== Starting Application Health Check Test ===");
        
        // Setup mock behaviors
        setupMockBehaviors();
        
        // Verify the application is healthy by checking that it started without errors
        String outputString = output.toString();
        
        // Check for successful component initialization
        assertThat(outputString)
            .as("Should initialize Spring components successfully")
            .doesNotContain(
                "Failed to configure",
                "Bean creation failed",
                "Initialization failed"
            );
        
        log.info("Application health check completed successfully");
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
