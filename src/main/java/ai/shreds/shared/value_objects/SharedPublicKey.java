package ai.shreds.shared.value_objects;

import java.security.PublicKey;

public class SharedPublicKey {
    private final String keyId;
    private final String algorithm;
    private final PublicKey publicKey;

    public SharedPublicKey(String keyId, String algorithm, PublicKey publicKey) {
        if (keyId == null || keyId.isEmpty()) {
            throw new IllegalArgumentException("KeyId cannot be null or empty");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new IllegalArgumentException("Algorithm cannot be null or empty");
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("PublicKey cannot be null");
        }
        this.keyId = keyId;
        this.algorithm = algorithm;
        this.publicKey = publicKey;
        validate();
    }

    public String getKeyId() {
        return keyId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void validate() {
        if (!algorithm.matches("^(RS256|RS384|RS512|ES256|ES384|ES512)$")) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }

    @Override
    public String toString() {
        return "SharedPublicKey{" +
                "keyId='" + keyId + '\'' +
                ", algorithm='" + algorithm + '\'' +
                '}';
    }
}
