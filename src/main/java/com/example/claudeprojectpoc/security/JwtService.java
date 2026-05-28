package com.example.claudeprojectpoc.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;

/**
 * Creates and validates JWTs signed with ML-DSA-87 (NIST FIPS 204).
 *
 * <p>The token format follows the standard JWT structure:
 * {@code base64url(header) . base64url(payload) . base64url(signature)}
 *
 * <ul>
 *   <li><b>Header:</b> {@code {"alg":"ML-DSA-87","typ":"JWT"}}</li>
 *   <li><b>Payload:</b> standard claims — {@code sub} (username), {@code iat} (issued-at),
 *       {@code exp} (expiry)</li>
 *   <li><b>Signature:</b> ML-DSA-87 signature over {@code header.payload} bytes. At ~4600 bytes
 *       this is significantly larger than RSA or ECDSA signatures, which is an inherent
 *       trade-off of lattice-based post-quantum algorithms.</li>
 * </ul>
 *
 * <p>Note: ML-DSA-87 is not yet part of the JOSE/JWT IANA registry (RFC 7518). The {@code alg}
 * header value used here is a custom identifier. Interoperability with third-party JWT
 * libraries will require explicit support for this algorithm.
 */
@Service
public class JwtService {

    private static final long EXPIRATION_SECONDS = 86400;
    private static final String HEADER = base64url("{\"alg\":\"ML-DSA-87\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PqcKeyManager keyManager;

    public JwtService(PqcKeyManager keyManager) {
        this.keyManager = keyManager;
    }

    /**
     * Generates a signed JWT for the given username.
     * The token is valid for {@value EXPIRATION_SECONDS} seconds (24 hours).
     *
     * @param username the subject claim ({@code sub}) to embed in the token
     * @return a signed JWT string
     */
    public String generateToken(String username) {
        long now = System.currentTimeMillis() / 1000;
        String payloadJson = String.format("{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d}", username, now, now + EXPIRATION_SECONDS);
        String payload = base64url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = HEADER + "." + payload;
        return signingInput + "." + base64url(sign(signingInput.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Validates a JWT and extracts the username from the {@code sub} claim.
     *
     * <p>Validation steps:
     * <ol>
     *   <li>Structural check — must have exactly three dot-separated parts.</li>
     *   <li>Signature verification — ML-DSA-87 signature over {@code header.payload}.</li>
     *   <li>Expiry check — {@code exp} claim must be in the future.</li>
     * </ol>
     *
     * @param token the JWT string from the {@code Authorization: Bearer} header
     * @return a {@code Mono} emitting the username, or empty if the token is invalid or expired
     */
    public Mono<String> validateTokenAndGetUsername(String token) {
        return Mono.fromCallable(() -> token.split("\\."))
                .filter(parts -> parts.length == 3)
                .filter(parts -> verify(
                        (parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8),
                        Base64.getUrlDecoder().decode(parts[2])
                ))
                .flatMap(parts -> Mono.fromCallable(() ->
                        (Map<?, ?>) MAPPER.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class)
                ))
                .filter(claims -> System.currentTimeMillis() / 1000 <= ((Number) claims.get("exp")).longValue())
                .map(claims -> (String) claims.get("sub"))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Signs {@code data} using the ML-DSA-87 private key from {@link PqcKeyManager}.
     * The Bouncy Castle provider ("BC") must be registered before this is called,
     * which {@link PqcKeyManager#init()} handles at startup.
     */
    private byte[] sign(byte[] data) {
        try {
            Signature signer = Signature.getInstance("ML-DSA", "BC");
            signer.initSign(keyManager.getPrivateKey());
            signer.update(data);
            return signer.sign();
        } catch (Exception e) {
            throw new RuntimeException("Token signing failed", e);
        }
    }

    /**
     * Verifies an ML-DSA-87 signature against {@code data} using the public key
     * from {@link PqcKeyManager}. Returns {@code false} on any failure rather than
     * throwing, so callers receive a clean empty {@code Mono} on invalid tokens.
     */
    private boolean verify(byte[] data, byte[] signature) {
        try {
            Signature verifier = Signature.getInstance("ML-DSA", "BC");
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
