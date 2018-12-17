package com.alperez.geekbooks.crowler.utils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class HttpUtils {

    public static HttpURLConnection makeConnection(URL url) throws IOException {
        switch (url.getProtocol()) {
            case "https":
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setSSLSocketFactory(makeAllTrustedSSLContext().getSocketFactory());
                https.setHostnameVerifier((s, sslSession) -> true);
                return https;
            case "http":
                return (HttpURLConnection) url.openConnection();
            default:
                throw new IOException("Not-supported protocol - "+url.getProtocol());
        }
    }

    public static SSLContext makeAllTrustedSSLContext() throws IOException {

        TrustManager tm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{tm}, new java.security.SecureRandom());
            return sc;
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("TLS algorithm is not supported");
        } catch (KeyManagementException e) {
            throw new IOException("KeyManagementException - "+e.getMessage(), e);
        }
    }



    private HttpUtils() {}
}
