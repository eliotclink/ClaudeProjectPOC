package com.example.claudeprojectpoc.security;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jcajce.spec.MLDSAParameterSpec;
import org.springframework.stereotype.Component;

import java.security.*;

@Component
public class PqcKeyManager {

    private KeyPair keyPair;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-DSA", "BC");
        kpg.initialize(MLDSAParameterSpec.ml_dsa_87);
        this.keyPair = kpg.generateKeyPair();
    }

    public PrivateKey getPrivateKey() { return keyPair.getPrivate(); }
    public PublicKey getPublicKey() { return keyPair.getPublic(); }
}
