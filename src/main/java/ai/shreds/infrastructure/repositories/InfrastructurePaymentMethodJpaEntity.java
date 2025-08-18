package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityPaymentMethod;
import ai.shreds.domain.enums.DomainCardBrandEnum;
import ai.shreds.domain.enums.DomainPaymentTypeEnum;
import ai.shreds.domain.value_objects.DomainAddressValue;
import ai.shreds.domain.value_objects.DomainPaymentMethodDataValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfrastructurePaymentMethodJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payment_method_id")
    private UUID paymentMethodId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "payment_type", nullable = false, length = 20)
    private String paymentType;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;

    @Column(name = "expiry_month")
    private Integer expiryMonth;

    @Column(name = "expiry_year")
    private Integer expiryYear;

    @Column(name = "card_brand", length = 20)
    private String cardBrand;

    @Column(name = "billing_address_line1", nullable = false, length = 255)
    private String billingAddressLine1;

    @Column(name = "billing_address_line2", length = 255)
    private String billingAddressLine2;

    @Column(name = "billing_city", nullable = false, length = 100)
    private String billingCity;

    @Column(name = "billing_state", nullable = false, length = 100)
    private String billingState;

    @Column(name = "billing_postal_code", nullable = false, length = 20)
    private String billingPostalCode;

    @Column(name = "billing_country", nullable = false, length = 2)
    private String billingCountry;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        addedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public DomainEntityPaymentMethod toDomainEntity() {
        DomainPaymentTypeEnum paymentTypeEnum = DomainPaymentTypeEnum.valueOf(paymentType);
        DomainCardBrandEnum cardBrandEnum = cardBrand != null ? DomainCardBrandEnum.valueOf(cardBrand) : null;
        
        DomainPaymentMethodDataValue paymentData = new DomainPaymentMethodDataValue(
                paymentTypeEnum,
                token,
                lastFourDigits,
                expiryMonth,
                expiryYear,
                cardBrandEnum
        );
        
        DomainAddressValue billingAddress = new DomainAddressValue(
                billingAddressLine1,
                billingAddressLine2,
                billingCity,
                billingState,
                billingPostalCode,
                billingCountry
        );
        
        return new DomainEntityPaymentMethod(
                paymentMethodId,
                accountId,
                paymentTypeEnum,
                token,
                lastFourDigits,
                expiryMonth,
                expiryYear,
                cardBrandEnum,
                billingAddress,
                isDefault,
                isActive,
                addedAt,
                updatedAt
        );
    }

    public static InfrastructurePaymentMethodJpaEntity fromDomainEntity(DomainEntityPaymentMethod domain) {
        InfrastructurePaymentMethodJpaEntity entity = new InfrastructurePaymentMethodJpaEntity();
        entity.setPaymentMethodId(domain.getPaymentMethodId());
        entity.setAccountId(domain.getAccountId());
        entity.setPaymentType(domain.getPaymentType().name());
        entity.setToken(domain.getToken());
        entity.setLastFourDigits(domain.getLastFourDigits());
        entity.setExpiryMonth(domain.getExpiryMonth());
        entity.setExpiryYear(domain.getExpiryYear());
        entity.setCardBrand(domain.getCardBrand() != null ? domain.getCardBrand().name() : null);
        entity.setBillingAddressLine1(domain.getBillingAddress().getLine1());
        entity.setBillingAddressLine2(domain.getBillingAddress().getLine2());
        entity.setBillingCity(domain.getBillingAddress().getCity());
        entity.setBillingState(domain.getBillingAddress().getState());
        entity.setBillingPostalCode(domain.getBillingAddress().getPostalCode());
        entity.setBillingCountry(domain.getBillingAddress().getCountry());
        entity.setIsDefault(domain.isDefault());
        entity.setIsActive(domain.isActive());
        entity.setAddedAt(domain.getAddedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}