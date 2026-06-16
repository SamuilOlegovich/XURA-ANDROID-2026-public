package com.samuilolegovich.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

// Сравнивает SHA-256 подписывающего сертификата текущего APK со списком
// разрешённых отпечатков — обнаруживает пересобранный/клонированный APK,
// подписанный чужим ключом.
public class SignatureVerifier {

    // TODO: добавить отпечаток релизного сертификата перед публикацией
    // (Play Console → Настройка → Целостность приложения → "Сертификат подписи приложения",
    // либо keytool -list -v -keystore <release.jks>, поле SHA256).
    private static final Set<String> ALLOWED_SHA256 = new HashSet<>(Arrays.asList(
            "A2E753A3C2AC8BDAEAB08C72DB5912E5FDCF330DE0771F63CC17B9B153C847CF" // локальный debug-keystore
    ));

    public static boolean isSignatureValid(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
            SigningInfo signingInfo = info.signingInfo;
            if (signingInfo == null) return false;

            Signature[] signatures = signingInfo.hasMultipleSigners()
                    ? signingInfo.getApkContentsSigners()
                    : signingInfo.getSigningCertificateHistory();
            if (signatures == null || signatures.length == 0) return false;

            for (Signature signature : signatures) {
                if (ALLOWED_SHA256.contains(sha256Hex(signature.toByteArray()))) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static String sha256Hex(byte[] data) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format(Locale.US, "%02X", b));
        }
        return sb.toString();
    }
}
