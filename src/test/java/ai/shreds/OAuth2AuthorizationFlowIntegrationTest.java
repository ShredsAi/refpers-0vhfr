package ai.shreds;

import ai.shreds.application.ports.ApplicationNotificationOutputPort;
import ai.shreds.shared.dtos.SharedOAuth2TokenResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
public class OAuth2AuthorizationFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("test_auth_db")
            .withUsername("test_user")
            .withPassword("test_password")
            .withInitScript("schema.sql");

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @MockBean
    private ApplicationNotificationOutputPort notificationOutputPort;

    private String baseUrl;
    private String codeVerifier;
    private String codeChallenge;
    private final String clientId = "test-client";
    private final String redirectUri = "http://localhost:3000/callback";
    private final String state = "test-state-123";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Database configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        // Redis configuration - Set specific host/port properties to match InfrastructureRedisConfiguration
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
        
        // Disable external services for testing
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "1025");
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
        
        // Mock SMS gateway
        registry.add("sms.gateway.url", () -> "http://localhost:8080/mock-sms");
        registry.add("sms.gateway.api-key", () -> "test-api-key");
        
        // Override JWT settings for testing
        registry.add("jwt.secret", () -> "test-secret-key-here-must-be-at-least-256-bits-long-for-hs256-algorithm-testing");
        registry.add("jwt.expiration", () -> "3600000");
        registry.add("refresh.token.expiration", () -> "86400000");
        
        // OAuth2 client settings for testing
        registry.add("oauth2.authorization.server.issuer", () -> "http://localhost:8080");
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        generatePkceParameters();
    }

    @Test
    void When_Valid_Authorization_Request_With_PKCE_Then_Code_Generated_And_Tokens_Exchanged(CapturedOutput output) {
        System.out.println("====== STARTING OAUTH2 AUTHORIZATION FLOW TEST ======");
        
        // Step 1: Initiate Authorization Request
        String authorizationUrl = baseUrl + "/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=read write" +
                "&state=" + state +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256";
        
        ResponseEntity<String> authResponse = restTemplate.getForEntity(authorizationUrl, String.class);
        
        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        
        String locationHeader = authResponse.getHeaders().getFirst("Location");
        assertThat(locationHeader).isNotNull();
        assertThat(locationHeader).startsWith(redirectUri);
        
        String authorizationCode = extractAuthorizationCodeFromUrl(locationHeader);
        assertThat(authorizationCode).isNotNull().isNotBlank();
        
        // Step 2: Exchange Authorization Code for Tokens
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
        tokenRequestBody.add("grant_type", "authorization_code");
        tokenRequestBody.add("code", authorizationCode);
        tokenRequestBody.add("redirect_uri", redirectUri);
        tokenRequestBody.add("client_id", clientId);
        tokenRequestBody.add("code_verifier", codeVerifier);
        
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenRequestBody, headers);
        
        ResponseEntity<SharedOAuth2TokenResponseDTO> tokenResponse = restTemplate.postForEntity(
                baseUrl + "/oauth/token",
                tokenRequest,
                SharedOAuth2TokenResponseDTO.class
        );
        
        assertThat(tokenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(tokenResponse.getBody()).isNotNull();
        
        SharedOAuth2TokenResponseDTO tokens = tokenResponse.getBody();
        
        assertThat(tokens.getAccessToken()).isNotNull().isNotBlank();
        assertThat(tokens.getRefreshToken()).isNotNull().isNotBlank();
        assertThat(tokens.getTokenType()).isEqualTo("Bearer");
        assertThat(tokens.getExpiresIn()).isNotNull().isPositive();
        
        // Step 3: Test Token Refresh Flow
        testTokenRefresh(tokens.getRefreshToken());
    }
    
    @Test
    void When_Valid_Refresh_Token_Then_New_Access_Token_Generated(CapturedOutput output) {
        System.out.println("====== STARTING REFRESH TOKEN TEST ======");
        
        // Step 1: Obtain initial tokens through OAuth2 flow
        SharedOAuth2TokenResponseDTO initialTokens = performCompleteOAuth2Flow();
        
        assertThat(initialTokens).isNotNull();
        assertThat(initialTokens.getRefreshToken()).isNotNull().isNotBlank();
        
        String originalRefreshToken = initialTokens.getRefreshToken();
        String originalAccessToken = initialTokens.getAccessToken();
        
        System.out.println("Initial tokens obtained successfully");
        System.out.println("Original Access Token (first 50 chars): " + originalAccessToken.substring(0, Math.min(50, originalAccessToken.length())));
        System.out.println("Original Refresh Token (first 50 chars): " + originalRefreshToken.substring(0, Math.min(50, originalRefreshToken.length())));
        
        // Step 2: Use refresh token to get new access token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> refreshRequestBody = new LinkedMultiValueMap<>();
        refreshRequestBody.add("grant_type", "refresh_token");
        refreshRequestBody.add("refresh_token", originalRefreshToken);
        
        HttpEntity<MultiValueMap<String, String>> refreshRequest = new HttpEntity<>(refreshRequestBody, headers);
        
        System.out.println("Sending refresh token request to: " + baseUrl + "/oauth/refresh");
        
        ResponseEntity<SharedOAuth2TokenResponseDTO> refreshResponse = restTemplate.postForEntity(
                baseUrl + "/oauth/refresh",
                refreshRequest,
                SharedOAuth2TokenResponseDTO.class
        );
        
        // Step 3: Verify response
        System.out.println("Refresh response status: " + refreshResponse.getStatusCode());
        
        if (!refreshResponse.getStatusCode().equals(HttpStatus.OK)) {
            System.err.println("Refresh token request failed. Response body: " + refreshResponse.getBody());
            System.err.println("Captured output: " + output.getOut());
        }
        
        assertThat(refreshResponse.getStatusCode())
            .as("Refresh token request should return HTTP 200")
            .isEqualTo(HttpStatus.OK);
        
        assertThat(refreshResponse.getBody())
            .as("Response body should not be null")
            .isNotNull();
        
        SharedOAuth2TokenResponseDTO newTokens = refreshResponse.getBody();
        
        // Step 4: Verify new tokens
        assertThat(newTokens.getAccessToken())
            .as("New access token should be present")
            .isNotNull()
            .isNotBlank();
            
        assertThat(newTokens.getRefreshToken())
            .as("New refresh token should be present")
            .isNotNull()
            .isNotBlank();
            
        assertThat(newTokens.getTokenType())
            .as("Token type should be Bearer")
            .isEqualTo("Bearer");
            
        assertThat(newTokens.getExpiresIn())
            .as("Expiration time should be positive")
            .isNotNull()
            .isPositive();
        
        // Step 5: Verify token rotation (new tokens should be different from original)
        assertThat(newTokens.getAccessToken())
            .as("New access token should be different from original")
            .isNotEqualTo(originalAccessToken);
            
        assertThat(newTokens.getRefreshToken())
            .as("New refresh token should be different from original (token rotation)")
            .isNotEqualTo(originalRefreshToken);
        
        System.out.println("New Access Token (first 50 chars): " + newTokens.getAccessToken().substring(0, Math.min(50, newTokens.getAccessToken().length())));
        System.out.println("New Refresh Token (first 50 chars): " + newTokens.getRefreshToken().substring(0, Math.min(50, newTokens.getRefreshToken().length())));
        
        System.out.println("====== REFRESH TOKEN TEST COMPLETED SUCCESSFULLY ======");
    }
    
    /**
     * Helper method to perform complete OAuth2 flow and return tokens
     * This is used by the refresh token test to obtain initial tokens
     */
    private SharedOAuth2TokenResponseDTO performCompleteOAuth2Flow() {
        // Generate new PKCE parameters for this flow
        generatePkceParameters();
        
        // Step 1: Initiate Authorization Request
        String authorizationUrl = baseUrl + "/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=read write" +
                "&state=" + state +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256";
        
        ResponseEntity<String> authResponse = restTemplate.getForEntity(authorizationUrl, String.class);
        
        if (!authResponse.getStatusCode().equals(HttpStatus.FOUND)) {
            fail("Authorization request failed with status: " + authResponse.getStatusCode());
        }
        
        String locationHeader = authResponse.getHeaders().getFirst("Location");
        if (locationHeader == null || !locationHeader.startsWith(redirectUri)) {
            fail("Invalid redirect response: " + locationHeader);
        }
        
        String authorizationCode = extractAuthorizationCodeFromUrl(locationHeader);
        if (authorizationCode == null || authorizationCode.isBlank()) {
            fail("Failed to extract authorization code from: " + locationHeader);
        }
        
        // Step 2: Exchange Authorization Code for Tokens
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
        tokenRequestBody.add("grant_type", "authorization_code");
        tokenRequestBody.add("code", authorizationCode);
        tokenRequestBody.add("redirect_uri", redirectUri);
        tokenRequestBody.add("client_id", clientId);
        tokenRequestBody.add("code_verifier", codeVerifier);
        
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenRequestBody, headers);
        
        ResponseEntity<SharedOAuth2TokenResponseDTO> tokenResponse = restTemplate.postForEntity(
                baseUrl + "/oauth/token",
                tokenRequest,
                SharedOAuth2TokenResponseDTO.class
        );
        
        if (!tokenResponse.getStatusCode().equals(HttpStatus.OK)) {
            fail("Token exchange failed with status: " + tokenResponse.getStatusCode());
        }
        
        return tokenResponse.getBody();
    }
    
    private void testTokenRefresh(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> refreshRequestBody = new LinkedMultiValueMap<>();
        refreshRequestBody.add("grant_type", "refresh_token");
        refreshRequestBody.add("refresh_token", refreshToken);
        
        HttpEntity<MultiValueMap<String, String>> refreshRequest = new HttpEntity<>(refreshRequestBody, headers);
        
        ResponseEntity<SharedOAuth2TokenResponseDTO> refreshResponse = restTemplate.postForEntity(
                baseUrl + "/oauth/refresh",
                refreshRequest,
                SharedOAuth2TokenResponseDTO.class
        );
        
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResponse.getBody()).isNotNull();
        
        SharedOAuth2TokenResponseDTO newTokens = refreshResponse.getBody();
        
        assertThat(newTokens.getAccessToken()).isNotNull().isNotBlank();
        assertThat(newTokens.getRefreshToken()).isNotNull().isNotBlank();
        assertThat(newTokens.getTokenType()).isEqualTo("Bearer");
        assertThat(newTokens.getExpiresIn()).isNotNull().isPositive();
        
        assertThat(newTokens.getAccessToken()).isNotEqualTo(refreshToken);
        assertThat(newTokens.getRefreshToken()).isNotEqualTo(refreshToken);
    }
    
    private String extractAuthorizationCodeFromUrl(String url) {
        try {
            if (url.contains("error=")) {
                System.err.println("Redirect URL contains an error: " + url);
                return null;
            }
            URI uri = URI.create(url);
            String query = uri.getQuery();
            
            if (query != null) {
                Pattern codePattern = Pattern.compile("code=([^&]+)");
                Matcher matcher = codePattern.matcher(query);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to extract authorization code: " + e.getMessage());
        }
        
        return null;
    }
    
    private void generatePkceParameters() {
        try {
            SecureRandom random = new SecureRandom();
            byte[] codeVerifierBytes = new byte[32];
            random.nextBytes(codeVerifierBytes);
            codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] challengeBytes = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);
            
        } catch (Exception e) {
            fail("Failed to generate PKCE parameters: " + e.getMessage());
        }
    }
}