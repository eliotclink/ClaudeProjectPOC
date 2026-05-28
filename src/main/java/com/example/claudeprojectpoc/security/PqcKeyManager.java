package com.example.claudeprojectpoc.security;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.springframework.stereotype.Component;

import java.security.*;

@Component
public class PqcKeyManager {

    private KeyPair keyPair;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Dilithium", "BC");
        kpg.initialize(DilithiumParameterSpec.dilithium5);
        this.keyPair = kpg.generateKeyPair();
    }

    public PrivateKey getPrivateKey() { return keyPair.getPrivate(); }
    public PublicKey getPublicKey() { return keyPair.getPublic(); }
}
