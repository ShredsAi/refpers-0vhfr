package ai.shreds.domain.entities;

import ai.shreds.domain.enums.DomainPaymentTypeEnum;
import ai.shreds.domain.enums.DomainCardBrandEnum;
import ai.shreds.domain.value_objects.DomainPaymentMethodDataValue;
import ai.shreds.domain.value_objects.DomainAddressValue;
import java.time.LocalDateTime;
import java.util.UUID;

public class DomainEntityPaymentMethod {
    private UUID paymentMethodId;
    private UUID accountId;
    private DomainPaymentTypeEnum paymentType;
    private String token;
    private String lastFourDigits;
    private Integer expiryMonth;
    private Integer expiryYear;
    private DomainCardBrandEnum cardBrand;
    private DomainAddressValue billingAddress;
    private boolean isDefault;
    private boolean isActive;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;

    public DomainEntityPaymentMethod(UUID paymentMethodId,
                                     UUID accountId,
                                     DomainPaymentTypeEnum paymentType,
                                     String token,
                                     String lastFourDigits,
                                     Integer expiryMonth,
                                     Integer expiryYear,
                                     DomainCardBrandEnum cardBrand,
                                     DomainAddressValue billingAddress,
                                     boolean isDefault,
                                     boolean isActive,
                                     LocalDateTime addedAt,
                                     LocalDateTime updatedAt) {
        this.paymentMethodId = paymentMethodId;
        this.accountId = accountId;
        this.paymentType = paymentType;
        this.token = token;
        this.lastFourDigits = lastFourDigits;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cardBrand = cardBrand;
        this.billingAddress = billingAddress;
        this.isDefault = isDefault;
        this.isActive = isActive;
        this.addedAt = addedAt;
        this.updatedAt = updatedAt;
    }

    public UUID getPaymentMethodId() {
        return paymentMethodId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public DomainPaymentTypeEnum getPaymentType() {
        return paymentType;
    }

    public String getToken() {
        return token;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    public Integer getExpiryYear() {
        return expiryYear;
    }

    public DomainCardBrandEnum getCardBrand() {
        return cardBrand;
    }

    public DomainAddressValue getBillingAddress() {
        return billingAddress;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAsDefault() {
        this.isDefault = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void unsetDefault() {
        this.isDefault = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        int currentYear = LocalDateTime.now().getYear();
        int currentMonth = LocalDateTime.now().getMonthValue();
        if (expiryYear < currentYear) {
            return true;
        }
        if (expiryYear == currentYear && expiryMonth < currentMonth) {
            return true;
        }
        return false;
    }

    public ai.shreds.domain.dtos.DomainPaymentMethod toDomainModel() {
        DomainPaymentMethodDataValue data = new DomainPaymentMethodDataValue(paymentType, token, lastFourDigits, expiryMonth, expiryYear, cardBrand);
        return new ai.shreds.domain.dtos.DomainPaymentMethod(
                paymentMethodId.toString(),
                accountId.toString(),
                data,
                billingAddress,
                isDefault,
                isActive
        );
    }
}