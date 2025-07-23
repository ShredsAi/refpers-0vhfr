package ai.shreds.infrastructure.repositories.mappers;

import ai.shreds.domain.entities.DomainEntityPaymentMethod;
import ai.shreds.domain.enums.DomainCardBrandEnum;
import ai.shreds.domain.enums.DomainPaymentTypeEnum;
import ai.shreds.domain.value_objects.DomainAddressValue;
import ai.shreds.infrastructure.repositories.InfrastructurePaymentMethodJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class InfrastructurePaymentMethodMapper {

    public DomainEntityPaymentMethod toDomainEntity(InfrastructurePaymentMethodJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        DomainPaymentTypeEnum paymentTypeEnum = DomainPaymentTypeEnum.valueOf(jpa.getPaymentType());
        DomainCardBrandEnum cardBrandEnum = jpa.getCardBrand() != null ? 
            DomainCardBrandEnum.valueOf(jpa.getCardBrand()) : null;
        
        DomainAddressValue billingAddress = toDomainAddressValue(jpa);
        
        return new DomainEntityPaymentMethod(
                jpa.getPaymentMethodId(),
                jpa.getAccountId(),
                paymentTypeEnum,
                jpa.getToken(),
                jpa.getLastFourDigits(),
                jpa.getExpiryMonth(),
                jpa.getExpiryYear(),
                cardBrandEnum,
                billingAddress,
                jpa.getIsDefault() != null ? jpa.getIsDefault().booleanValue() : false,
                jpa.getIsActive() != null ? jpa.getIsActive().booleanValue() : true,
                jpa.getAddedAt(),
                jpa.getUpdatedAt()
        );
    }

    public InfrastructurePaymentMethodJpaEntity toJpaEntity(DomainEntityPaymentMethod domain) {
        if (domain == null) {
            return null;
        }

        InfrastructurePaymentMethodJpaEntity jpa = new InfrastructurePaymentMethodJpaEntity();
        jpa.setPaymentMethodId(domain.getPaymentMethodId());
        jpa.setAccountId(domain.getAccountId());
        jpa.setPaymentType(domain.getPaymentType().name());
        jpa.setToken(domain.getToken());
        jpa.setLastFourDigits(domain.getLastFourDigits());
        jpa.setExpiryMonth(domain.getExpiryMonth());
        jpa.setExpiryYear(domain.getExpiryYear());
        jpa.setCardBrand(domain.getCardBrand() != null ? domain.getCardBrand().name() : null);
        jpa.setBillingAddressLine1(domain.getBillingAddress().getLine1());
        jpa.setBillingAddressLine2(domain.getBillingAddress().getLine2());
        jpa.setBillingCity(domain.getBillingAddress().getCity());
        jpa.setBillingState(domain.getBillingAddress().getState());
        jpa.setBillingPostalCode(domain.getBillingAddress().getPostalCode());
        jpa.setBillingCountry(domain.getBillingAddress().getCountry());
        jpa.setIsDefault(Boolean.valueOf(domain.isDefault()));
        jpa.setIsActive(Boolean.valueOf(domain.isActive()));
        jpa.setAddedAt(domain.getAddedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());

        return jpa;
    }

    public DomainAddressValue toDomainAddressValue(InfrastructurePaymentMethodJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }
        
        return new DomainAddressValue(
                jpa.getBillingAddressLine1(),
                jpa.getBillingAddressLine2(),
                jpa.getBillingCity(),
                jpa.getBillingState(),
                jpa.getBillingPostalCode(),
                jpa.getBillingCountry()
        );
    }
}