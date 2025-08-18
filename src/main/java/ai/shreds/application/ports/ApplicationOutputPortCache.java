package ai.shreds.application.ports;

import ai.shreds.application.dtos.ApplicationMoneyValue;

public interface ApplicationOutputPortCache {
    /**
     * Retrieves cached balance for the given account ID
     *
     * @param accountId The account ID to get balance for
     * @return The cached balance or null if not found
     */
    ApplicationMoneyValue getBalance(String accountId);

    /**
     * Stores the balance in cache for the given account ID
     *
     * @param accountId The account ID to cache balance for
     * @param balance The balance to cache
     */
    void putBalance(String accountId, ApplicationMoneyValue balance);

    /**
     * Invalidates the cached balance for the given account ID
     *
     * @param accountId The account ID to invalidate cache for
     */
    void invalidateBalance(String accountId);
}