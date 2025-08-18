package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.ports.DomainOutputPortPaymentGateway;
import ai.shreds.domain.value_objects.DomainPaymentMethodDataValue;
import ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InfrastructurePaymentGatewayClient implements DomainOutputPortPaymentGateway {

    private final String apiUrl;
    private final String apiKey;
    private final RestTemplate restTemplate;

    public InfrastructurePaymentGatewayClient(
            @Value("${financial.payment-gateway.url}") String apiUrl,
            @Value("${financial.payment-gateway.api-key}") String apiKey,
            RestTemplate restTemplate) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.restTemplate = restTemplate;
    }

    @Override
    public DomainPaymentMethodDataValue tokenizeCard(String cardNumber, Integer expiryMonth, Integer expiryYear, String cvv) {
        try {
            InfrastructurePaymentGatewayRequestDTO request = new InfrastructurePaymentGatewayRequestDTO(
                    cardNumber, expiryMonth, expiryYear, cvv);

            InfrastructurePaymentGatewayResponseDTO response = callTokenizationApi(request);

            return response.toDomainPaymentMethodData();
        } catch (Exception e) {
            throw new InfrastructureExternalServiceException(
                    "PaymentGateway", 
                    "Failed to tokenize card", 
                    e);
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            String validationUrl = apiUrl + "/validate";
            HttpHeaders headers = createHeaders();

            HttpEntity<String> request = new HttpEntity<>(
                    "{\"token\":\"" + token + "\"}", headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    validationUrl,
                    HttpMethod.POST,
                    request,
                    String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            handleApiError(e);
            return false;
        }
    }

    private InfrastructurePaymentGatewayResponseDTO callTokenizationApi(InfrastructurePaymentGatewayRequestDTO request) {
        try {
            String tokenizeUrl = apiUrl + "/tokenize";
            HttpHeaders headers = createHeaders();

            HttpEntity<String> httpRequest = new HttpEntity<>(request.toJson(), headers);

            ResponseEntity<InfrastructurePaymentGatewayResponseDTO> response = restTemplate.exchange(
                    tokenizeUrl,
                    HttpMethod.POST,
                    httpRequest,
                    InfrastructurePaymentGatewayResponseDTO.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new InfrastructureExternalServiceException(
                        "PaymentGateway", 
                        "Invalid response from payment gateway", 
                        response.getStatusCode().value(), 
                        response.toString());
            }
        } catch (Exception e) {
            handleApiError(e);
            throw e;
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private void handleApiError(Exception error) {
        if (error instanceof InfrastructureExternalServiceException) {
            throw (InfrastructureExternalServiceException) error;
        }

        throw new InfrastructureExternalServiceException(
                "PaymentGateway", 
                "Payment gateway API error: " + error.getMessage(), 
                error);
    }
}