package ai.shreds.gateway.security;

import ai.shreds.application.ports.ApplicationOutputPortJwksClient;
import ai.shreds.infrastructure.external_services.InfrastructureRedisClient;
import ai.shreds.shared.dtos.SharedJwksDTO;
import ai.shreds.shared.dtos.SharedPublicKeyDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
@SpringJUnitConfig
public class JwksClientIntegrationTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort())
            .withReuse(true);

    @Container
    static final RabbitMQContainer rabbitMQ = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.12-management"))
            .withExposedPorts(5672, 15672)
            .waitingFor(Wait.forListeningPort())
            .withReuse(true);

    @Container
    static final MockServerContainer mockServer = new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.15.0"))
            .withExposedPorts(1080)
            .waitingFor(Wait.forListeningPort())
            .withReuse(true);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ApplicationOutputPortJwksClient jwksClient;

    @Autowired
    private InfrastructureRedisClient redisClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private ObjectMapper objectMapper;
    private MockServerClient mockServerClient;

    @TestConfiguration
    static class TestConfig {
        // This configuration will be used for testing
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Redis configuration
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
        
        // RabbitMQ configuration
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQ::getAdminPassword);
        
        // Set the security properties directly to use our MockServer
        String mockUrl = "http://" + mockServer.getHost() + ":" + mockServer.getFirstMappedPort();
        registry.add("security.oauth2.resourceserver.jwt.jwk-set-uri", () -> mockUrl + "/.well-known/jwks.json");
        registry.add("security.account-service-url", () -> mockUrl);
        
        // Override the expected issuer and audience to match our test JWT
        registry.add("security.jwt.issuer", () -> "expected-issuer");
        registry.add("security.jwt.audience", () -> "expected-audience");
        
        // Set shorter cache TTL for testing
        registry.add("security.jwks-cache-ttl", () -> "30"); // 30 seconds for testing
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockServerClient = new MockServerClient(mockServer.getHost(), mockServer.getFirstMappedPort());
        
        // Clear Redis cache before each test
        try {
            redisClient.delete("JWKS_CACHE_KEY");
        } catch (Exception e) {
            // Ignore errors during cleanup
        }
        
        // Clear any existing mock expectations
        mockServerClient.reset();
    }

    /**
     * Test that JWKS are fetched from Account Service, cached in Redis, and subsequent requests retrieve from cache
     */
    @Test
    public void When_Fetching_JWKS_Then_Public_Keys_Cached_And_Retrieved(CapturedOutput output) throws Exception {
        System.out.println("=== Starting JWKS Fetching and Caching Test ===");
        
        // Wait for application to fully start
        Thread.sleep(2000);
        
        // Setup mock JWKS endpoint that returns valid JWKS
        String jwksResponse = createValidJwksResponse();
        setupMockJwksEndpoint(jwksResponse);
        
        System.out.println("=== Mock JWKS endpoint configured ===");
        
        // Verify cache is initially empty
        String cacheKey = "JWKS_CACHE_KEY";
        String cachedData = redisClient.get(cacheKey);
        assertThat(cachedData).isNull();
        System.out.println("=== Verified cache initially empty ===");
        
        // First call - should fetch from Account Service and cache
        System.out.println("=== First JWKS fetch - should call Account Service ===");
        SharedJwksDTO firstResult = jwksClient.fetchPublicKeys();
        
        // Verify the result
        assertThat(firstResult).isNotNull();
        assertThat(firstResult.getKeys()).isNotEmpty();
        assertThat(firstResult.getKeys()).hasSize(1);
        
        SharedPublicKeyDTO key = firstResult.getKeys().get(0);
        assertThat(key.getKty()).isEqualTo("RSA");
        assertThat(key.getKid()).isEqualTo("test-key-1");
        assertThat(key.getUse()).isEqualTo("sig");
        assertThat(key.getAlg()).isEqualTo("RS256");
        assertThat(key.getN()).isNotNull();
        assertThat(key.getE()).isEqualTo("AQAB");
        
        System.out.println("=== First JWKS fetch successful ===");
        System.out.println("Key ID: " + key.getKid());
        System.out.println("Algorithm: " + key.getAlg());
        System.out.println("Key Type: " + key.getKty());
        
        // Verify the data is now cached in Redis
        cachedData = redisClient.get(cacheKey);
        assertThat(cachedData).isNotNull();
        assertThat(cachedData).contains("test-key-1");
        assertThat(cachedData).contains("RSA");
        System.out.println("=== Verified JWKS cached in Redis ===");
        
        // Verify mockServer was called exactly once
        mockServerClient.verify(
            request()
                .withMethod("GET")
                .withPath("/.well-known/jwks.json"),
            org.mockserver.verify.VerificationTimes.exactly(1)
        );
        System.out.println("=== Verified Account Service was called exactly once ===");
        
        // Second call - should retrieve from cache (no HTTP call)
        System.out.println("=== Second JWKS fetch - should retrieve from cache ===");
        SharedJwksDTO secondResult = jwksClient.fetchPublicKeys();
        
        // Verify second result is identical to first (from cache)
        assertThat(secondResult).isNotNull();
        assertThat(secondResult.getKeys()).hasSize(1);
        
        SharedPublicKeyDTO cachedKey = secondResult.getKeys().get(0);
        assertThat(cachedKey.getKid()).isEqualTo(key.getKid());
        assertThat(cachedKey.getAlg()).isEqualTo(key.getAlg());
        assertThat(cachedKey.getKty()).isEqualTo(key.getKty());
        assertThat(cachedKey.getN()).isEqualTo(key.getN());
        assertThat(cachedKey.getE()).isEqualTo(key.getE());
        
        System.out.println("=== Second JWKS fetch retrieved from cache ===");
        
        // Verify mockServer was still called exactly once (no additional calls)
        mockServerClient.verify(
            request()
                .withMethod("GET")
                .withPath("/.well-known/jwks.json"),
            org.mockserver.verify.VerificationTimes.exactly(1)
        );
        System.out.println("=== Verified no additional Account Service calls ===");
        
        // Test getCachedPublicKeys method
        List<SharedPublicKeyDTO> cachedKeys = jwksClient.getCachedPublicKeys();
        assertThat(cachedKeys).isNotNull();
        assertThat(cachedKeys).hasSize(1);
        assertThat(cachedKeys.get(0).getKid()).isEqualTo("test-key-1");
        System.out.println("=== getCachedPublicKeys method working correctly ===");
        
        // Verify logs show caching behavior
        String logs = output.getOut();
        System.out.println("=== Test Logs ===");
        System.out.println(logs);
        
        // Verify no critical errors occurred
        assertThat(logs).doesNotContain("ERROR");
        assertThat(logs).doesNotContain("FATAL");
        
        System.out.println("=== JWKS Fetching and Caching Test Completed Successfully ===");
    }

    /**
     * Test that when Account Service is unavailable, cached JWKS are used for JWT validation
     */
    @Test
    public void When_Account_Service_Unavailable_Then_Cached_Keys_Used(CapturedOutput output) throws Exception {
        System.out.println("=== Starting Account Service Unavailable Test ===");
        
        // Wait for application to fully start
        Thread.sleep(2000);
        
        // Step 1: First, populate the cache with valid JWKS
        System.out.println("=== Step 1: Populating cache with valid JWKS ===");
        String jwksResponse = createValidJwksResponse();
        setupMockJwksEndpoint(jwksResponse);
        
        // Make initial request to populate cache
        SharedJwksDTO initialResult = jwksClient.fetchPublicKeys();
        assertThat(initialResult).isNotNull();
        assertThat(initialResult.getKeys()).hasSize(1);
        
        // Verify the cache is populated
        String cacheKey = "JWKS_CACHE_KEY";
        String cachedData = redisClient.get(cacheKey);
        assertThat(cachedData).isNotNull();
        assertThat(cachedData).contains("test-key-1");
        System.out.println("=== Cache populated successfully ===");
        
        // Step 2: Make Account Service unavailable
        System.out.println("=== Step 2: Making Account Service unavailable ===");
        mockServerClient.reset(); // Clear all mock expectations
        
        // Setup mock to return 503 Service Unavailable
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withPath("/.well-known/jwks.json"))
                .respond(response()
                        .withStatusCode(503)
                        .withBody("Service Unavailable"));
        
        System.out.println("=== Account Service configured to return 503 ===");
        
        // Step 3: Verify that cached keys are still returned
        System.out.println("=== Step 3: Attempting to fetch JWKS with unavailable service ===");
        
        // The current implementation should still return cached keys
        SharedJwksDTO cachedResult = jwksClient.fetchPublicKeys();
        
        // Verify cached result is returned
        assertThat(cachedResult).isNotNull();
        assertThat(cachedResult.getKeys()).hasSize(1);
        
        SharedPublicKeyDTO cachedKey = cachedResult.getKeys().get(0);
        assertThat(cachedKey.getKid()).isEqualTo("test-key-1");
        assertThat(cachedKey.getAlg()).isEqualTo("RS256");
        assertThat(cachedKey.getKty()).isEqualTo("RSA");
        assertThat(cachedKey.getE()).isEqualTo("AQAB");
        
        System.out.println("=== Cached JWKS returned successfully ===");
        System.out.println("Cached Key ID: " + cachedKey.getKid());
        System.out.println("Cached Algorithm: " + cachedKey.getAlg());
        
        // Step 4: Test getCachedPublicKeys method also works
        List<SharedPublicKeyDTO> cachedKeys = jwksClient.getCachedPublicKeys();
        assertThat(cachedKeys).isNotNull();
        assertThat(cachedKeys).hasSize(1);
        assertThat(cachedKeys.get(0).getKid()).isEqualTo("test-key-1");
        System.out.println("=== getCachedPublicKeys method working with cached data ===");
        
        // Step 5: Verify logs show appropriate behavior
        String logs = output.getOut();
        System.out.println("=== Test Logs ===");
        System.out.println(logs);
        
        // Should not contain fatal errors since cached data is used
        assertThat(logs).doesNotContain("FATAL");
        
        System.out.println("=== Account Service Unavailable Test Completed Successfully ===");
    }

    /**
     * Test that when Account Service is unavailable AND no cached keys exist, appropriate error is thrown
     */
    @Test
    public void When_Account_Service_Unavailable_And_No_Cache_Then_Error_Thrown(CapturedOutput output) throws Exception {
        System.out.println("=== Starting Account Service Unavailable Without Cache Test ===");
        
        // Wait for application to fully start
        Thread.sleep(2000);
        
        // Ensure cache is empty
        String cacheKey = "JWKS_CACHE_KEY";
        redisClient.delete(cacheKey);
        String cachedData = redisClient.get(cacheKey);
        assertThat(cachedData).isNull();
        System.out.println("=== Verified cache is empty ===");
        
        // Setup mock to return 503 Service Unavailable
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withPath("/.well-known/jwks.json"))
                .respond(response()
                        .withStatusCode(503)
                        .withBody("Service Unavailable"));
        
        System.out.println("=== Account Service configured to return 503 ===");
        
        // Verify that appropriate error is thrown when no cache exists
        assertThatThrownBy(() -> jwksClient.fetchPublicKeys())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch JWKS");
        
        System.out.println("=== Appropriate error thrown for unavailable service with no cache ===");
        
        // Verify logs show the error
        String logs = output.getOut();
        System.out.println("=== Test Logs ===");
        System.out.println(logs);
        
        System.out.println("=== Account Service Unavailable Without Cache Test Completed Successfully ===");
    }

    private String createValidJwksResponse() {
        return "{" +
                "\"keys\": [" +
                "{" +
                "\"kty\": \"RSA\"," +
                "\"kid\": \"test-key-1\"," +
                "\"use\": \"sig\"," +
                "\"alg\": \"RS256\"," +
                "\"n\": \"1bVuS5zGx_Gtcm3Gco9nEkwMrR4qbIXcs36PliMIW75U3yYYaAiM0PvTPjDAxp-67ZZiOuGjGnqILaw4OkJmnCIJbS0NFQQ5-PZYeK0lhCrjrXe0m7LWIGNXLMJz3Z0t3g_GFQrmaWzKJK-QQWxVV_Y2qNsV7Z7OJqY0ECmv8CCbVupoV5ue9K2H9DVrLqC9YTGcnNnLjDI9sZ0pmU4ltOOj01GYfYWFdLMmQUzGbQvbH1jRE3Rjy4hDxWgLJbgwwLLYuXG6aPRqKw1lu-2HBeimcHgaPfhAyz1eJGDeArQLJVjhqHlCbUgrEHG-M7O8WJTE5b9KPg7pJNsDUg7IAw\"," +
                "\"e\": \"AQAB\"" +
                "}" +
                "]" +
                "}";
    }

    private void setupMockJwksEndpoint(String jwksResponse) {
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withPath("/.well-known/jwks.json"))
                .respond(response()
                        .withStatusCode(200)
                        .withContentType(APPLICATION_JSON)
                        .withBody(jwksResponse));
    }
}