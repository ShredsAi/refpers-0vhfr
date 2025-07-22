package ai.shreds.infrastructure.external_services;

import ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for sending SMS messages via external gateway.
 */
@Service
public class InfrastructureSmsGatewayClient {

    private final RestTemplate restTemplate;
    private final String smsGatewayUrl;
    private final String apiKey;

    @Autowired
    public InfrastructureSmsGatewayClient(RestTemplate restTemplate,
                                          @Value("${mfa.sms.gateway-url:http://localhost:8080/mock-sms}") String smsGatewayUrl,
                                          @Value("${mfa.sms.api-key:test-api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.smsGatewayUrl = smsGatewayUrl;
        this.apiKey = apiKey;
    }

    public void sendSms(String phoneNumber, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> payload = buildSmsRequest(phoneNumber, message);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(smsGatewayUrl, request, String.class);
        } catch (Exception e) {
            throw new InfrastructureExternalServiceException(
                "Failed to send SMS: " + e.getMessage(),
                "InfrastructureSmsGatewayClient",
                0
            );
        }
    }

    private Map<String, Object> buildSmsRequest(String phoneNumber, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("to", phoneNumber);
        payload.put("body", message);
        payload.put("apiKey", apiKey);
        return payload;
    }
}
