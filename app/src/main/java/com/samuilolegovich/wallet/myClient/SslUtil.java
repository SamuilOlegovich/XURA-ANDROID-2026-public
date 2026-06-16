package com.samuilolegovich.wallet.myClient;

import android.annotation.SuppressLint;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;

/**
 * SSL helpers for XRPL nodes.
 *
 * Trust-all sockets must only be used for TESTNET endpoints, whose
 * certificates may not be present in the system trust store. Mainnet
 * (real-money) connections always go through standard system certificate
 * validation plus public-key pinning — see {@link #RPC_MAINNET_PINS} and
 * {@link #WSS_MAINNET_PINS}.
 */
public final class SslUtil {
    private SslUtil() {}

    // ─── Trust-all (TESTNET ONLY — never use for mainnet/payment flows) ───

    @SuppressLint("TrustAllX509TrustManager")
    private static final X509TrustManager TRUST_ALL_MANAGER = new X509TrustManager() {
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

    // ─── Certificate pinning (MAINNET) ─────────────────────────────────────
    // Pins cover the leaf certificate plus its issuing CA, so a routine leaf
    // renewal does not break the app — only a switch to a different CA does.
    // Must be refreshed if the node providers below ever change.

    /** RPC mainnet endpoint — must match StringEnum.NET_REAL_POST_URL_ONE host. */
    public static final String RPC_MAINNET_HOST = "s1.ripple.com";
    public static final String[] RPC_MAINNET_PINS = {
            "sha256/rI2qvMp2NJkdt/hTFi4hEUWIZBnXkMDY4qx6t8rShcM=", // *.ripple.com leaf
            "sha256/0dflgFofXiuLoZvgRpP8N9xrpDTgZ7c1xbmTjIxym7o=", // GandiCert issuing CA
    };

    /** WSS mainnet endpoint — must match StringEnum.WSS_REAL host. */
    public static final String WSS_MAINNET_HOST = "xrplcluster.com";
    public static final String[] WSS_MAINNET_PINS = {
            "sha256/5519bXodYphe/ErrqMqkQ6OVXJW4Frv1ZNDG7MbPWnE=", // xrplcluster.com leaf
            "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=", // Google Trust Services WE1 issuing CA
    };

    public static OkHttpClient pinnedOkHttpClient(String host, String[] pins) {
        CertificatePinner pinner = new CertificatePinner.Builder()
                .add(host, pins)
                .build();
        return new OkHttpClient.Builder()
                .certificatePinner(pinner)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /** Raw SSLSocketFactory with the same pin check, for the WebSocket client. */
    public static SSLSocketFactory pinnedSocketFactory(String[] pins) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            X509TrustManager systemTrustManager = null;
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    systemTrustManager = (X509TrustManager) tm;
                    break;
                }
            }
            if (systemTrustManager == null) {
                throw new IllegalStateException("No system X509TrustManager available");
            }
            X509TrustManager pinningManager = new PinningTrustManager(systemTrustManager, pins);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{pinningManager}, new SecureRandom());
            return ctx.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build pinned SSLSocketFactory", e);
        }
    }

    /** Standard system trust validation, then requires a pinned key in the chain. */
    private static final class PinningTrustManager implements X509TrustManager {
        private final X509TrustManager delegate;
        private final Set<String> pins;

        PinningTrustManager(X509TrustManager delegate, String[] pins) {
            this.delegate = delegate;
            this.pins = new HashSet<>(Arrays.asList(pins));
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            delegate.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            delegate.checkServerTrusted(chain, authType);
            for (X509Certificate cert : chain) {
                if (pins.contains("sha256/" + sha256Spki(cert))) {
                    return;
                }
            }
            throw new CertificateException("Certificate pinning failure: no pinned public key found in chain");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegate.getAcceptedIssuers();
        }

        private static String sha256Spki(X509Certificate cert) throws CertificateException {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(cert.getPublicKey().getEncoded());
                return Base64.getEncoder().encodeToString(hash);
            } catch (Exception e) {
                throw new CertificateException("Failed to hash certificate public key", e);
            }
        }
    }
}
