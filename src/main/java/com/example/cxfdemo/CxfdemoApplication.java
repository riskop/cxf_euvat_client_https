package com.example.cxfdemo;

import eu.europa.ec.taxud.vies.services.checkvat.CheckVatPortType;
import eu.europa.ec.taxud.vies.services.checkvat.CheckVatService;
import jakarta.xml.ws.BindingProvider;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootApplication
public class CxfdemoApplication implements CommandLineRunner {

    private static final QName SERVICE_NAME = new QName("urn:ec.europa.eu:taxud:vies:services:checkVat", "checkVatService");

    public static void main(String[] args) {
        //System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Slf4jLogger");

        // with this it works
        //System.setProperty("javax.net.ssl.trustStore", "truststore_with_globalsign.jks");

        // with this it does not work (trusststore without GlobalSign CA)
        //System.setProperty("javax.net.ssl.trustStore", "truststore.jks");

        //System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

		SpringApplication.run(CxfdemoApplication.class, args);
	}

    @Autowired
    @Qualifier("mySslContext")
    SSLContext mySslContext;

    @Override
    public void run(String... args) throws Exception {

        String endpointUrl = "https://ec.europa.eu/taxation_customs/vies/services/checkVatService";

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        //factory.setServiceClass(CheckVatService.class);
        factory.setServiceClass(CheckVatPortType.class);
        factory.setAddress(endpointUrl);
        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());
        CheckVatPortType port = (CheckVatPortType) factory.create();

        BindingProvider provider = (BindingProvider) port;
        provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);

        //SSLContext sslContext = SSLContext.getDefault();
        //SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

        //provider.getRequestContext().put("com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory", sslsf);


        //tlsCP.setDisableCNCheck(true);
        //tlsCP.setHostnameVerifier(hostNameVerifier);
        Client cxfClient = ClientProxy.getClient(port);
        HTTPConduit conduit = (HTTPConduit) cxfClient.getConduit();

        TLSClientParameters tlsCP = new TLSClientParameters();
        tlsCP.setSslContext( mySslContext ); //setTrustManagers(new TrustManager[] { new TrustAllX509TrustManager() });
        conduit.setTlsClientParameters(tlsCP);

        //CheckVatService ss = new CheckVatService(wsdlURL, SERVICE_NAME);
        //CheckVatPortType port = ss.getCheckVatPort();

        {
            System.out.println("Invoking checkVat...");
            java.lang.String _checkVat_countryCodeVal = "HU";
            jakarta.xml.ws.Holder<java.lang.String> _checkVat_countryCode = new jakarta.xml.ws.Holder<java.lang.String>(_checkVat_countryCodeVal);
            java.lang.String _checkVat_vatNumberVal = "10585560";
            jakarta.xml.ws.Holder<java.lang.String> _checkVat_vatNumber = new jakarta.xml.ws.Holder<java.lang.String>(_checkVat_vatNumberVal);
            jakarta.xml.ws.Holder<javax.xml.datatype.XMLGregorianCalendar> _checkVat_requestDate = new jakarta.xml.ws.Holder<javax.xml.datatype.XMLGregorianCalendar>();
            jakarta.xml.ws.Holder<java.lang.Boolean> _checkVat_valid = new jakarta.xml.ws.Holder<java.lang.Boolean>();
            jakarta.xml.ws.Holder<java.lang.String> _checkVat_name = new jakarta.xml.ws.Holder<java.lang.String>();
            jakarta.xml.ws.Holder<java.lang.String> _checkVat_address = new jakarta.xml.ws.Holder<java.lang.String>();
            port.checkVat(_checkVat_countryCode, _checkVat_vatNumber, _checkVat_requestDate, _checkVat_valid, _checkVat_name, _checkVat_address);

            System.out.println("checkVat._checkVat_countryCode=" + _checkVat_countryCode.value);
            System.out.println("checkVat._checkVat_vatNumber=" + _checkVat_vatNumber.value);
            System.out.println("checkVat._checkVat_requestDate=" + _checkVat_requestDate.value);
            System.out.println("checkVat._checkVat_valid=" + _checkVat_valid.value);
            System.out.println("checkVat._checkVat_name=" + _checkVat_name.value);
            System.out.println("checkVat._checkVat_address=" + _checkVat_address.value);

        }
    }
}
