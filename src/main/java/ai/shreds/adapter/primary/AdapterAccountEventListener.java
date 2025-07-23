package ai.shreds.adapter.primary;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ai.shreds.application.ports.ApplicationInputPortFinancialAccount;
import ai.shreds.shared.dtos.SharedAccountCreatedEventDTO;
import ai.shreds.shared.dtos.SharedAccountSuspendedEventDTO;
import ai.shreds.shared.dtos.SharedAccountClosedEventDTO;

@Component
public class AdapterAccountEventListener {

    private final ApplicationInputPortFinancialAccount financialAccountService;

    public AdapterAccountEventListener(ApplicationInputPortFinancialAccount financialAccountService) {
        this.financialAccountService = financialAccountService;
    }

    @EventListener
    @Transactional
    public void handleAccountCreated(SharedAccountCreatedEventDTO event) {
        financialAccountService.createFinancialAccount(event.getAccountId());
    }

    @EventListener
    @Transactional
    public void handleAccountSuspended(SharedAccountSuspendedEventDTO event) {
        financialAccountService.suspendAccount(event.getAccountId(), event.getReason());
    }

    @EventListener
    @Transactional
    public void handleAccountClosed(SharedAccountClosedEventDTO event) {
        financialAccountService.closeAccount(event.getAccountId(), event.getReason());
    }
}