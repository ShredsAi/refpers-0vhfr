package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainAccountEntity;
import ai.shreds.domain.entities.DomainSecuritySettingsEntity;
import ai.shreds.domain.exceptions.DomainInvalidCredentialsException;
import ai.shreds.domain.exceptions.DomainInvalidTokenException;
import ai.shreds.domain.ports.*;
import ai.shreds.domain.value_objects.DomainPasswordPolicyValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DomainPasswordService implements DomainInputPortPasswordManagement {

    private final DomainOutputPortAccountRepository accountRepository;
    private final DomainOutputPortSecurityRepository securityRepository;
    private final DomainOutputPortCryptoService cryptoService;
    private final DomainOutputPortSessionRepository sessionRepository;
    private final DomainPasswordPolicyValidator passwordPolicyValidator;

    // Temporary storage for reset tokens (in real implementation, use cache)
    private final Map<String, PasswordResetTokenData> resetTokens = new HashMap<>();

    public DomainPasswordService(
            DomainOutputPortAccountRepository accountRepository,
            DomainOutputPortSecurityRepository securityRepository,
            DomainOutputPortCryptoService cryptoService,
            DomainOutputPortSessionRepository sessionRepository,
            DomainPasswordPolicyValidator passwordPolicyValidator) {
        this.accountRepository = accountRepository;
        this.securityRepository = securityRepository;
        this.cryptoService = cryptoService;
        this.sessionRepository = sessionRepository;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    @Override
    public boolean validatePasswordPolicy(String password) {
        return passwordPolicyValidator.validatePasswordComplexity(password);
    }

    @Override
    public String generatePasswordResetToken(String email) {
        // Find account by email
        DomainAccountEntity account = accountRepository.findByEmail(email);
        if (account == null) {
            throw new DomainInvalidTokenException("Email address not found");
        }

        // Check if account is active
        if (!account.isActive()) {
            throw new DomainInvalidTokenException("Account is not active");
        }

        // Generate secure reset token
        String resetToken = cryptoService.generateSecureToken();
        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS); // 24 hours expiration

        // Store reset token data (in real implementation, use cache with TTL)
        PasswordResetTokenData tokenData = new PasswordResetTokenData(
            resetToken, account.getAccountId().toString(), email, expiresAt
        );
        resetTokens.put(resetToken, tokenData);

        return resetToken;
    }

    @Override
    public void changePassword(String accountId, String currentPassword, String newPassword) {
        // Find account
        DomainAccountEntity account = accountRepository.findById(accountId);
        if (account == null) {
            throw new DomainInvalidTokenException("Account not found");
        }

        // Verify current password if provided
        if (currentPassword != null && !cryptoService.verifyPassword(currentPassword, account.getPasswordHash())) {
            throw new DomainInvalidCredentialsException("Current password is incorrect");
        }

        // Validate new password policy
        if (!validatePasswordPolicy(newPassword)) {
            throw new IllegalArgumentException("New password does not meet policy requirements: " + 
                passwordPolicyValidator.getPasswordRequirements());
        }

        // Hash new password
        String newPasswordHash = cryptoService.hashPassword(newPassword);

        // Update password in account (this would normally update the database)
        // For now, we just validate the operation

        // Update security settings with password change timestamp
        DomainSecuritySettingsEntity securitySettings = securityRepository.findByAccountId(accountId);
        if (securitySettings == null) {
            securitySettings = new DomainSecuritySettingsEntity(
                UUID.randomUUID(),
                UUID.fromString(accountId),
                false,
                null,
                0,
                null,
                Instant.now()
            );
            securityRepository.save(securitySettings);
        } else {
            // Update password changed timestamp
            securityRepository.update(securitySettings);
        }

        // Revoke all existing sessions to force re-authentication
        sessionRepository.revokeAllSessionsForAccount(accountId);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Validate reset token
        PasswordResetTokenData tokenData = resetTokens.get(token);
        if (tokenData == null || tokenData.isExpired()) {
            throw new DomainInvalidTokenException("Invalid or expired reset token");
        }

        // Validate new password policy
        if (!validatePasswordPolicy(newPassword)) {
            throw new IllegalArgumentException("New password does not meet policy requirements: " + 
                passwordPolicyValidator.getPasswordRequirements());
        }

        // Remove used token
        resetTokens.remove(token);

        // Change password (no current password verification needed for reset)
        changePassword(tokenData.getAccountId(), null, newPassword);
    }

    // Inner class to hold password reset token data
    private static class PasswordResetTokenData {
        private final String token;
        private final String accountId;
        private final String email;
        private final Instant expiresAt;

        public PasswordResetTokenData(String token, String accountId, String email, Instant expiresAt) {
            this.token = token;
            this.accountId = accountId;
            this.email = email;
            this.expiresAt = expiresAt;
        }

        public boolean isExpired() {
            return expiresAt.isBefore(Instant.now());
        }

        public String getAccountId() { return accountId; }
        public String getEmail() { return email; }
    }
}
