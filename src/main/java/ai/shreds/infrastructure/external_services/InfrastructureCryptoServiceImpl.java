package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.ports.DomainOutputPortCryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class InfrastructureCryptoServiceImpl implements DomainOutputPortCryptoService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    @Autowired
    public InfrastructureCryptoServiceImpl(BCryptPasswordEncoder passwordEncoder, SecureRandom secureRandom) {
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = secureRandom;
    }

    @Override
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean verifyPassword(String password, String hash) {
        return passwordEncoder.matches(password, hash);
    }

    @Override
    public String generateSecureToken() {
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    @Override
    public String hashToken(String token) {
        return passwordEncoder.encode(token);
    }
}
