package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedJwksDTO;
import ai.shreds.shared.dtos.SharedPublicKeyDTO;
import java.util.List;

/**
 * Port for fetching and caching JSON Web Key Sets (JWKS).
 */
public interface ApplicationOutputPortJwksClient {

    /**
     * Fetch the latest JWKS from the Account Service.
     * @return DTO containing the key set
     */
    SharedJwksDTO fetchPublicKeys();

    /**
     * Retrieve cached public keys if available.
     * @return list of public key DTOs
     */
    List<SharedPublicKeyDTO> getCachedPublicKeys();
}