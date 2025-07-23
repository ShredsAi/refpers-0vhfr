package ai.shreds.domain.dtos;

import ai.shreds.domain.value_objects.DomainPaymentMethodDataValue;
import ai.shreds.domain.value_objects.DomainAddressValue;
import ai.shreds.application.dtos.ApplicationPaymentMethodResponseDTO;

public class DomainPaymentMethod {
    private final String paymentMethodId;
    private final String accountId;
    private final DomainPaymentMethodDataValue paymentData;
    private final DomainAddressValue billingAddress;
    private final boolean isDefault;
    private final boolean isActive;

    public DomainPaymentMethod(String paymentMethodId,
                              String accountId,
                              DomainPaymentMethodDataValue paymentData,
                              DomainAddressValue billingAddress,
                              boolean isDefault,
                              boolean isActive) {
        this.paymentMethodId = paymentMethodId;
        this.accountId = accountId;
        this.paymentData = paymentData;
        this.billingAddress = billingAddress;
        this.isDefault = isDefault;
        this.isActive = isActive;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getAccountId() {
        return accountId;
    }

    public DomainPaymentMethodDataValue getPaymentData() {
        return paymentData;
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

    public ApplicationPaymentMethodResponseDTO toApplicationDTO() {
        return ApplicationPaymentMethodResponseDTO.fromDomainPaymentMethod(this);
    }
}