package ai.shreds.application.ports;

import ai.shreds.application.dtos.ApplicationTransactionDTO;

public interface ApplicationOutputPortEventPublisher {
    /**
     * Publishes an event when a financial account is created
     *
     * @param accountId The ID of the created account
     */
    void publishFinancialAccountCreated(String accountId);

    /**
     * Publishes an event when a transaction is processed
     *
     * @param transaction The transaction that was processed
     */
    void publishTransactionProcessed(ApplicationTransactionDTO transaction);

    /**
     * Publishes an event when a payment method is added
     *
     * @param paymentMethodId The ID of the added payment method
     * @param accountId The ID of the account the payment method was added to
     */
    void publishPaymentMethodAdded(String paymentMethodId, String accountId);
}