package com.example.cxfdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
public class MySslContextConfig /*implements InitializingBean*/ {

    private static final Logger LOG = LoggerFactory.getLogger(MySslContextConfig.class);

    @Autowired
    private Environment environment;

    @Value("${https.truststore.jksfile.password:changeit}")
    private char[] trustStorePassword;

    @Value("${https.truststore.only.additional.truststore:false}")
    boolean onlyAdditionalTruststore;

    public X509TrustManager getTrustManagerWithMergedTruststore(String trustStoreFileName, char[] trustStorePassword) {
        try {
            KeyStore trustStoreWithAdditionalCAs = TrustStoreUtil.getKeyStoreFromFile(trustStoreFileName, trustStorePassword);

            final TrustManagerFactory additionalCAsTrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            additionalCAsTrustManagerFactory.init(trustStoreWithAdditionalCAs);
            final X509TrustManager additionalCAsTrustManager = (X509TrustManager) additionalCAsTrustManagerFactory.getTrustManagers()[0];

            KeyStore ks = TrustStoreUtil.createEmptyKeyStoreInitialized();

            if(onlyAdditionalTruststore)     {
                LOG.info("using only additional truststore without merging Java default trusted certs");
            } else {
                LOG.info("merging Java default trusted certs with additional truststore");
                final TrustManagerFactory javaDefaultTrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                javaDefaultTrustManagerFactory.init((KeyStore) null);
                final X509TrustManager javaDefaultTrustManager = (X509TrustManager) javaDefaultTrustManagerFactory.getTrustManagers()[0];
                for(X509Certificate x509Certificate : javaDefaultTrustManager.getAcceptedIssuers()) {
                    ks.setCertificateEntry(x509Certificate.getSubjectX500Principal().getName(), x509Certificate);
                }
                LOG.info("merged truststore size after merging system's default trusted certs " + ks.size());
            }
            for(X509Certificate x509Certificate : additionalCAsTrustManager.getAcceptedIssuers()) {
                ks.setCertificateEntry(x509Certificate.getSubjectX500Principal().getName(), x509Certificate);
            }
            LOG.info("merged truststore size after merging additional trusted certs " + ks.size());

            final TrustManagerFactory mergedTrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            mergedTrustManagerFactory.init(ks);


            X509TrustManager trustManager = (X509TrustManager)mergedTrustManagerFactory.getTrustManagers()[0];

            return trustManager;
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean(name = "mySslContext")
    public SSLContext createRnySslContext() throws NoSuchAlgorithmException, KeyManagementException {
        String trustStoreFileName = environment.getProperty("https.truststore.jksfile.path");
        if(trustStoreFileName == null) {
            LOG.info("https.truststore.jksfile.path is not set, using Java default SSLContext");
            return SSLContext.getDefault();
        }
        LOG.info("https.truststore.jksfile.path is set to {}, merging it with Java default truststore", trustStoreFileName);

        X509TrustManager trustManager = getTrustManagerWithMergedTruststore(trustStoreFileName, trustStorePassword);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
                null,
                new TrustManager[]{
                        trustManager
                },
                null
        );
        LOG.info("going to set rnySslContext as default SSLContext for code using SSLContext.getDefault() or using simply HttpClient without custom SSLContext");
        SSLContext.setDefault(sslContext);
        LOG.info("rnySslContext has been set as default SSLContext");
        return sslContext;
    }
}
