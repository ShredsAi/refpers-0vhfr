package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainAccountEntity;

/**
 * Outbound port for account repository operations.
 */
public interface DomainOutputPortAccountRepository {

    /**
     * Find account by username.
     */
    DomainAccountEntity findByUsername(String username);

    /**
     * Find account by email.
     */
    DomainAccountEntity findByEmail(String email);

    /**
     * Find account by account ID.
     */
    DomainAccountEntity findById(String accountId);
}
