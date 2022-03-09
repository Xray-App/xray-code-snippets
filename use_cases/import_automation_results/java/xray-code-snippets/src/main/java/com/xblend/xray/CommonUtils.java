package com.xblend.xray;

import java.net.InetSocketAddress;
import java.net.Proxy;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class CommonUtils {

    public static boolean isTrue(Boolean bool) {
        return (bool!=null && bool);
    }

    public static OkHttpClient getHttpClient(Boolean useInternalTestProxy, Boolean ignoreSslErrors) throws Exception {
        OkHttpClient client;
        OkHttpClient.Builder newBuilder = new OkHttpClient.Builder();

        if (isTrue(ignoreSslErrors) || isTrue(useInternalTestProxy)) {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }
            
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }
            
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            newBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            newBuilder.hostnameVerifier((host, session) -> true);
        }

        if (isTrue(useInternalTestProxy)) {
            String hostname = "localhost"/*127.0.0.1*/;
            int port = 18080;
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
            new InetSocketAddress(hostname, port));
            client = newBuilder.proxy(proxy).build();
        } else {
            client = newBuilder.build();
        }
        return client;
    }

}
