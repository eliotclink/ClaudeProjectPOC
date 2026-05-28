package com.example.claudeprojectpoc.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;

@Service
public class JwtService {

    private static final long EXPIRATION_SECONDS = 86400;
    private static final String HEADER = base64url("{\"alg\":\"Dilithium5\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PqcKeyManager keyManager;

    public JwtService(PqcKeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public String generateToken(String username) {
        long now = System.currentTimeMillis() / 1000;
        String payloadJson = String.format("{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d}", username, now, now + EXPIRATION_SECONDS);
        String payload = base64url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = HEADER + "." + payload;
        return signingInput + "." + base64url(sign(signingInput.getBytes(StandardCharsets.UTF_8)));
    }

    public Mono<String> validateTokenAndGetUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return Mono.empty();

            String signingInput = parts[0] + "." + parts[1];
            byte[] signature = Base64.getUrlDecoder().decode(parts[2]);

            if (!verify(signingInput.getBytes(StandardCharsets.UTF_8), signature)) return Mono.empty();

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<?, ?> claims = MAPPER.readValue(payloadBytes, Map.class);

            long exp = ((Number) claims.get("exp")).longValue();
            if (System.currentTimeMillis() / 1000 > exp) return Mono.empty();

            return Mono.just((String) claims.get("sub"));
        } catch (Exception e) {
            return Mono.empty();
        }
    }

    private byte[] sign(byte[] data) {
        try {
            Signature signer = Signature.getInstance("Dilithium", "BC");
            signer.initSign(keyManager.getPrivateKey());
            signer.update(data);
            return signer.sign();
        } catch (Exception e) {
            throw new RuntimeException("Token signing failed", e);
        }
    }

    private boolean verify(byte[] data, byte[] signature) {
        try {
            Signature verifier = Signature.getInstance("Dilithium", "BC");
            verifier.initVerify(keyManager.getPublicKey());
            verifier.update(data);
            return verifier.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private static String base64url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
