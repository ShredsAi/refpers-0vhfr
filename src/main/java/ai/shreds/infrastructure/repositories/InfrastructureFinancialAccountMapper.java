package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityFinancialAccount;
import ai.shreds.domain.enums.DomainAccountStatusEnum;
import ai.shreds.domain.value_objects.DomainCurrencyValue;
import ai.shreds.domain.value_objects.DomainMoneyValue;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InfrastructureFinancialAccountMapper {

    public DomainEntityFinancialAccount toDomainEntity(InfrastructureFinancialAccountJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        DomainCurrencyValue currency = new DomainCurrencyValue(jpa.getBalanceCurrency(), getCurrencySymbol(jpa.getBalanceCurrency()));
        DomainMoneyValue balance = new DomainMoneyValue(jpa.getBalanceAmount(), currency);
        DomainAccountStatusEnum status = DomainAccountStatusEnum.valueOf(jpa.getStatus());

        return new DomainEntityFinancialAccount(
                jpa.getFinancialAccountId(),
                jpa.getAccountId(),
                balance,
                status,
                jpa.getVersion(),
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }

    public InfrastructureFinancialAccountJpaEntity toJpaEntity(DomainEntityFinancialAccount domain) {
        if (domain == null) {
            return null;
        }

        InfrastructureFinancialAccountJpaEntity jpa = new InfrastructureFinancialAccountJpaEntity();
        jpa.setFinancialAccountId(domain.getFinancialAccountId());
        jpa.setAccountId(domain.getAccountId());
        jpa.setBalanceAmount(domain.getBalance().getAmount());
        jpa.setBalanceCurrency(domain.getBalance().getCurrency().getCode());
        jpa.setStatus(domain.getStatus().name());
        jpa.setVersion(domain.getVersion());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());

        return jpa;
    }

    public DomainMoneyValue toDomainMoneyValue(BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            return null;
        }

        DomainCurrencyValue currencyValue = new DomainCurrencyValue(currency, getCurrencySymbol(currency));
        return new DomainMoneyValue(amount, currencyValue);
    }

    private String getCurrencySymbol(String currencyCode) {
        return switch (currencyCode) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "JPY" -> "¥";
            case "CAD" -> "C$";
            case "AUD" -> "A$";
            case "CHF" -> "CHF";
            default -> currencyCode;
        };
    }
}