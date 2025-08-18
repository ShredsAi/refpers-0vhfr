package ai.shreds.domain.value_objects;

import ai.shreds.domain.enums.DomainPaymentTypeEnum;
import ai.shreds.domain.enums.DomainCardBrandEnum;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Objects;

@Builder
@Getter
public class DomainPaymentMethodDataValue {
    private final DomainPaymentTypeEnum paymentType;
    private final String token;
    private final String lastFourDigits;
    private final Integer expiryMonth;
    private final Integer expiryYear;
    private final DomainCardBrandEnum cardBrand;

    public DomainPaymentMethodDataValue(DomainPaymentTypeEnum paymentType,
                                       String token,
                                       String lastFourDigits,
                                       Integer expiryMonth,
                                       Integer expiryYear,
                                       DomainCardBrandEnum cardBrand) {
        this.paymentType = paymentType;
        this.token = token;
        this.lastFourDigits = lastFourDigits;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cardBrand = cardBrand;
    }

    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        
        if (expiryYear < currentYear) {
            return true;
        }
        if (expiryYear.equals(currentYear) && expiryMonth < currentMonth) {
            return true;
        }
        return false;
    }

    public boolean validate() {
        if (paymentType == null) return false;
        if (token == null || token.trim().isEmpty()) return false;
        if (lastFourDigits == null || !lastFourDigits.matches("\\d{4}")) return false;
        if (expiryMonth == null || expiryMonth < 1 || expiryMonth > 12) return false;
        if (expiryYear == null || expiryYear < Year.now().getValue()) return false;
        if (cardBrand == null) return false;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainPaymentMethodDataValue)) return false;
        DomainPaymentMethodDataValue that = (DomainPaymentMethodDataValue) o;
        return paymentType == that.paymentType &&
               Objects.equals(token, that.token) &&
               Objects.equals(lastFourDigits, that.lastFourDigits) &&
               Objects.equals(expiryMonth, that.expiryMonth) &&
               Objects.equals(expiryYear, that.expiryYear) &&
               cardBrand == that.cardBrand;
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentType, token, lastFourDigits, expiryMonth, expiryYear, cardBrand);
    }
}