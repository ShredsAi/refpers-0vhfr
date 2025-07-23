package ai.shreds.infrastructure.external_services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfrastructurePaymentGatewayRequestDTO {

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("expiry_month")
    private Integer expiryMonth;

    @JsonProperty("expiry_year")
    private Integer expiryYear;

    @JsonProperty("cvv")
    private String cvv;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payment gateway request", e);
        }
    }
}