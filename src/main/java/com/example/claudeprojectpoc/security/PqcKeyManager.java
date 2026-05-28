package com.example.claudeprojectpoc.security;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jcajce.spec.MLDSAParameterSpec;
import org.springframework.stereotype.Component;

import java.security.*;

/**
 * Generates and holds the ML-DSA-87 key pair used to sign and verify JWTs.
 *
 * <p>ML-DSA (Module-Lattice-Based Digital Signature Algorithm) is standardized by NIST
 * in FIPS 204. It is a post-quantum algorithm, meaning it remains secure against attacks
 * from both classical and quantum computers. The ML-DSA-87 parameter set targets NIST
 * security level 5 — equivalent strength to AES-256.
 *
 * <p>The key pair is generated once at application startup. In production this should be
 * replaced with keys loaded from a secure vault (e.g. HashiCorp Vault, AWS KMS) and
 * rotated on a defined schedule.
 */
@Component
public class PqcKeyManager {

    private KeyPair keyPair;

    /**
     * Registers the Bouncy Castle JCA provider and generates an ML-DSA-87 key pair.
     * Bouncy Castle 1.80+ is required for the {@code org.bouncycastle.jcajce.spec.MLDSAParameterSpec}
     * class, which uses the finalised NIST naming convention (earlier versions used
     * {@code DilithiumParameterSpec} under the pre-standardisation name).
     */
    @PostConstruct
    public void init() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-DSA", "BC");
        kpg.initialize(MLDSAParameterSpec.ml_dsa_87);
        this.keyPair = kpg.generateKeyPair();
    }

    /** Returns the private key used to sign tokens. Never expose this outside the security package. */
    public PrivateKey getPrivateKey() { return keyPair.getPrivate(); }

    /** Returns the public key used to verify token signatures. */
    public PublicKey getPublicKey() { return keyPair.getPublic(); }
}
