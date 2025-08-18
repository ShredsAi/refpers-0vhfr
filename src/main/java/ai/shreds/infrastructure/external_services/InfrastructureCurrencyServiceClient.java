package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.ports.DomainOutputPortCurrencyService;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import ai.shreds.domain.value_objects.DomainCurrencyValue;
import ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InfrastructureCurrencyServiceClient implements DomainOutputPortCurrencyService {

    private final String apiUrl;
    private final RestTemplate restTemplate;
    private final Map<String, BigDecimal> exchangeRateCache;

    public InfrastructureCurrencyServiceClient(
            @Value("${financial.currency-service.url}") String apiUrl,
            RestTemplate restTemplate) {
        this.apiUrl = apiUrl;
        this.restTemplate = restTemplate;
        this.exchangeRateCache = new ConcurrentHashMap<>();
    }

    @Override
    public DomainMoneyValue convertCurrency(DomainMoneyValue amount, String targetCurrency) {
        try {
            String fromCurrency = amount.getCurrency().getCode();
            
            if (fromCurrency.equals(targetCurrency)) {
                return amount;
            }

            BigDecimal exchangeRate = getExchangeRate(fromCurrency, targetCurrency);
            BigDecimal convertedAmount = amount.getAmount().multiply(exchangeRate);
            
            DomainCurrencyValue targetCurrencyValue = new DomainCurrencyValue(
                    targetCurrency, 
                    getCurrencySymbol(targetCurrency));
            
            return new DomainMoneyValue(convertedAmount, targetCurrencyValue);
        } catch (Exception e) {
            throw new InfrastructureExternalServiceException(
                    "CurrencyService", 
                    "Failed to convert currency from " + amount.getCurrency().getCode() + " to " + targetCurrency, 
                    e);
        }
    }

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        String cacheKey = fromCurrency + "_" + toCurrency;
        
        // Check cache first
        BigDecimal cachedRate = exchangeRateCache.get(cacheKey);
        if (cachedRate != null) {
            return cachedRate;
        }

        try {
            InfrastructureCurrencyApiResponseDTO response = fetchExchangeRate(fromCurrency, toCurrency);
            
            if (response.getSuccess() && response.getRate() != null) {
                BigDecimal rate = response.getRate();
                cacheExchangeRate(cacheKey, rate);
                return rate;
            } else {
                throw new InfrastructureExternalServiceException(
                        "CurrencyService", 
                        "Invalid response from currency API");
            }
        } catch (Exception e) {
            throw new InfrastructureExternalServiceException(
                    "CurrencyService", 
                    "Failed to fetch exchange rate from " + fromCurrency + " to " + toCurrency, 
                    e);
        }
    }

    private InfrastructureCurrencyApiResponseDTO fetchExchangeRate(String from, String to) {
        String url = String.format("%s/latest?from=%s&to=%s", apiUrl, from, to);
        
        try {
            return restTemplate.getForObject(url, InfrastructureCurrencyApiResponseDTO.class);
        } catch (Exception e) {
            throw new InfrastructureExternalServiceException(
                    "CurrencyService", 
                    "HTTP request failed for currency conversion", 
                    e);
        }
    }

    private void cacheExchangeRate(String key, BigDecimal rate) {
        exchangeRateCache.put(key, rate);
    }

    private String getCurrencySymbol(String currencyCode) {
        return switch (currencyCode) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "JPY" -> "¥";
            case "CAD" -> "C$";
            case "AUD" -> "A$";
            default -> currencyCode;
        };
    }
}