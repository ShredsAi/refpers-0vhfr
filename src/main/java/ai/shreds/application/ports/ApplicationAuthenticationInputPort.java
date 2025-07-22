package ai.shreds.application.ports;

import ai.shreds.application.dtos.ApplicationSessionDTO;
import ai.shreds.shared.dtos.SharedLoginRequestDTO;
import ai.shreds.shared.dtos.SharedLoginResponseDTO;

/**
 * Input port for authentication operations.
 */
public interface ApplicationAuthenticationInputPort {

    /**
     * Authenticate user with credentials.
     * @param request login request containing username and password
     * @return login response with tokens or MFA requirement
     */
    SharedLoginResponseDTO authenticateUser(SharedLoginRequestDTO request);

    /**
     * Validate the status of an account by its ID.
     * @param accountId the account identifier
     */
    void validateAccountStatus(String accountId);

    /**
     * Handle a failed authentication attempt.
     * @param accountId the account identifier
     * @param reason failure reason
     */
    void handleFailedAuthentication(String accountId, String reason);

    /**
     * Complete a successful authentication by creating session tokens.
     * @param accountId the account identifier
     * @return session DTO containing tokens
     */
    ApplicationSessionDTO completeAuthentication(String accountId);

    /**
     * Logout a user by revoking the given token.
     * @param token the access token to revoke
     */
    void logout(String token);
}
