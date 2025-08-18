package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityPaymentMethod;
import java.util.List;
import java.util.UUID;

public interface DomainOutputPortPaymentMethodRepository {
    DomainEntityPaymentMethod save(DomainEntityPaymentMethod paymentMethod);
    DomainEntityPaymentMethod findById(UUID paymentMethodId);
    List<DomainEntityPaymentMethod> findByAccountId(UUID accountId);
    DomainEntityPaymentMethod findDefaultByAccountId(UUID accountId);
}