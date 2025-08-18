package ai.shreds.application.ports;

import ai.shreds.application.dtos.ApplicationBalanceDTO;

public interface ApplicationInputPortFinancialAccount {
    /**
     * Creates a new financial account for the given account ID
     *
     * @param accountId The ID of the account to create a financial account for
     */
    void createFinancialAccount(String accountId);

    /**
     * Retrieves the current balance of a financial account
     *
     * @param accountId The ID of the account to get the balance for
     * @return The current balance and timestamp of when it was last updated
     */
    ApplicationBalanceDTO getAccountBalance(String accountId);

    /**
     * Suspends a financial account, preventing any further transactions
     *
     * @param accountId The ID of the account to suspend
     * @param reason The reason for suspending the account
     */
    void suspendAccount(String accountId, String reason);

    /**
     * Closes a financial account permanently
     *
     * @param accountId The ID of the account to close
     * @param reason The reason for closing the account
     */
    void closeAccount(String accountId, String reason);
}