package ai.shreds.shared.dtos;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class SharedPublicKeyDTO {
    private String kty;
    private String kid;
    private String use;
    private String alg;
    private String n;  // modulus
    private String e;  // exponent

    public SharedPublicKeyDTO() {
    }

    public String getKty() {
        return kty;
    }

    public void setKty(String kty) {
        this.kty = kty;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

    public PublicKey toPublicKey() {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
            byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create public key from JWK", ex);
        }
    }

    @Override
    public String toString() {
        return "SharedPublicKeyDTO{" +
                "kty='" + kty + '\'' +
                ", kid='" + kid + '\'' +
                ", use='" + use + '\'' +
                ", alg='" + alg + '\'' +
                '}';
    }
}
