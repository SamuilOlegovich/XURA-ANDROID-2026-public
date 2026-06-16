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
 * Вспомогательные SSL-инструменты для работы с нодами XRPL.
 *
 * Доверяющие всем сертификатам соединения должны использоваться только для
 * TESTNET-эндпоинтов, чьи сертификаты могут отсутствовать в системном хранилище
 * доверия. Mainnet-соединения (с настоящими деньгами) всегда проходят через
 * стандартную системную проверку сертификата плюс пиннинг публичного ключа —
 * см. {@link #RPC_MAINNET_PINS} и {@link #WSS_MAINNET_PINS}.
 */
public final class SslUtil {
    private SslUtil() {}

    // ─── Доверие всем сертификатам (ТОЛЬКО TESTNET — никогда для mainnet/платежей) ───

    @SuppressLint("TrustAllX509TrustManager")
    private static final X509TrustManager TRUST_ALL_MANAGER = new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] c, String a) {}
        public void checkServerTrusted(X509Certificate[] c, String a) {}
        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
    };

    /** Создаёт SSLContext, доверяющий любым сертификатам (только для тестовой сети). */
    public static SSLContext trustAllSslContext() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{TRUST_ALL_MANAGER}, new SecureRandom());
            return ctx;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build trust-all SSLContext", e);
        }
    }

    /** Возвращает SSLSocketFactory, доверяющую любым сертификатам (только для тестовой сети). */
    public static SSLSocketFactory trustAllSocketFactory() {
        return trustAllSslContext().getSocketFactory();
    }

    /** Создаёт OkHttpClient, не проверяющий сертификат и hostname сервера (только для тестовой сети). */
    public static OkHttpClient trustAllOkHttpClient() {
        SSLSocketFactory sf = trustAllSocketFactory();
        return new OkHttpClient.Builder()
                .sslSocketFactory(sf, TRUST_ALL_MANAGER)
                .hostnameVerifier((h, s) -> true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    // ─── Пиннинг сертификата (MAINNET) ──────────────────────────────────────
    // Пины покрывают листовой сертификат и его выпускающий CA, поэтому обычное
    // продление листового сертификата не ломает приложение — ломает только
    // смена другого CA. Должны быть обновлены, если провайдеры нод ниже изменятся.

    /** RPC mainnet-эндпоинт — должен совпадать с хостом StringEnum.NET_REAL_POST_URL_ONE. */
    public static final String RPC_MAINNET_HOST = "s1.ripple.com";
    public static final String[] RPC_MAINNET_PINS = {
            "sha256/rI2qvMp2NJkdt/hTFi4hEUWIZBnXkMDY4qx6t8rShcM=", // листовой *.ripple.com
            "sha256/0dflgFofXiuLoZvgRpP8N9xrpDTgZ7c1xbmTjIxym7o=", // выпускающий CA GandiCert
    };

    /** WSS mainnet-эндпоинт — должен совпадать с хостом StringEnum.WSS_REAL. */
    public static final String WSS_MAINNET_HOST = "xrplcluster.com";
    public static final String[] WSS_MAINNET_PINS = {
            "sha256/5519bXodYphe/ErrqMqkQ6OVXJW4Frv1ZNDG7MbPWnE=", // листовой xrplcluster.com
            "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=", // выпускающий CA Google Trust Services WE1
    };

    /** Создаёт OkHttpClient со стандартной системной проверкой сертификата плюс дополнительный пиннинг публичного ключа для указанного хоста. */
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

    /** Базовая SSLSocketFactory с такой же проверкой пиннинга, для WebSocket-клиента. */
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

    /** Сначала выполняет стандартную системную проверку доверия, затем дополнительно требует наличие хотя бы одного из закреплённых ключей в цепочке сертификатов. */
    private static final class PinningTrustManager implements X509TrustManager {
        private final X509TrustManager delegate;
        private final Set<String> pins;

        /** Создаёт менеджер пиннинга, оборачивающий системный TrustManager и набор разрешённых SHA-256 отпечатков публичного ключа. */
        PinningTrustManager(X509TrustManager delegate, String[] pins) {
            this.delegate = delegate;
            this.pins = new HashSet<>(Arrays.asList(pins));
        }

        /** Делегирует проверку клиентского сертификата системному TrustManager. */
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            delegate.checkClientTrusted(chain, authType);
        }

        /** Выполняет стандартную системную проверку сертификата сервера и дополнительно проверяет, что хотя бы один сертификат цепочки закреплён (pinned). */
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

        /** Делегирует получение списка доверенных издателей системному TrustManager. */
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegate.getAcceptedIssuers();
        }

        /** Вычисляет SHA-256 хеш публичного ключа сертификата (SPKI), закодированный в base64. */
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
