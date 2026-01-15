package com.example.cxfdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class TrustStoreUtil {

    public static KeyStore getKeyStoreFromFile(String fileName, char[] trustStorePassword) {
        try {
            KeyStore ks = createEmptyKeyStore();
            InputStream is = new FileInputStream(fileName);
            ks.load(is, trustStorePassword);
            is.close();
            return ks;
        } catch (CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static KeyStore createEmptyKeyStoreInitialized() {
        try {
            KeyStore ks = createEmptyKeyStore();
            ks.load(null);
            return ks;
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    public static KeyStore createEmptyKeyStore() {
        try {
            return KeyStore.getInstance("JKS");
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

}
