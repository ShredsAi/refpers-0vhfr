package ai.shreds.shared.dtos;

import ai.shreds.shared.value_objects.SharedPublicKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class SharedJwksDTO {
    private List<SharedPublicKeyDTO> keys;

    public SharedJwksDTO() {
        this.keys = new ArrayList<>();
    }

    public SharedJwksDTO(List<SharedPublicKeyDTO> keys) {
        this.keys = keys;
    }

    public List<SharedPublicKeyDTO> getKeys() {
        return keys;
    }

    public void setKeys(List<SharedPublicKeyDTO> keys) {
        this.keys = keys;
    }

    public List<SharedPublicKey> toPublicKeys() {
        List<SharedPublicKey> publicKeys = new ArrayList<>();
        for (SharedPublicKeyDTO keyDTO : keys) {
            PublicKey publicKey = keyDTO.toPublicKey();
            publicKeys.add(new SharedPublicKey(keyDTO.getKid(), keyDTO.getAlg(), publicKey));
        }
        return publicKeys;
    }

    @Override
    public String toString() {
        return "SharedJwksDTO{" +
                "keys=" + keys +
                '}';
    }
}
