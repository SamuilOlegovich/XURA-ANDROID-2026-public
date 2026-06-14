package com.samuilolegovich.wallet.myClient;

import android.annotation.SuppressLint;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * SSL helpers for XRPL testnet nodes whose certificates are not trusted
 * by the Android emulator's system store.
 *
 * Use only for DEV/testnet connections — never in production payment flows.
 */
public final class SslUtil {

    private SslUtil() {}

    @SuppressLint("TrustAllX509TrustManager")
    public static final X509TrustManager TRUST_ALL_MANAGER = new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] c, String a) {}
        public void checkServerTrusted(X509Certificate[] c, String a) {}
        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
    };

    public static SSLContext trustAllSslContext() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{TRUST_ALL_MANAGER}, new SecureRandom());
            return ctx;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build trust-all SSLContext", e);
        }
    }

    public static SSLSocketFactory trustAllSocketFactory() {
        return trustAllSslContext().getSocketFactory();
    }

    public static OkHttpClient trustAllOkHttpClient() {
        SSLSocketFactory sf = trustAllSocketFactory();
        return new OkHttpClient.Builder()
                .sslSocketFactory(sf, TRUST_ALL_MANAGER)
                .hostnameVerifier((h, s) -> true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
