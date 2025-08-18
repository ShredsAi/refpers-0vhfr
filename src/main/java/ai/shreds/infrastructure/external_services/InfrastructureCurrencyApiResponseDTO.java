package ai.shreds.infrastructure.external_services;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfrastructureCurrencyApiResponseDTO {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("result")
    private BigDecimal result;

    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("rate")
    private BigDecimal rate;

    public Boolean getSuccess() {
        return success != null ? success : false;
    }

    public BigDecimal getRate() {
        return rate != null ? rate : result;
    }
}