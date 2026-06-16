package com.samuilolegovich.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Единственный способ получить SharedPreferences в приложении.
 * Все значения (и ключи) зашифрованы AES-256-GCM через Android Keystore,
 * чтобы пароль, seed-фраза и другие чувствительные данные не хранились в открытом виде.
 */
public class PrefsHelper {

    private static final String FILE_NAME = "xura_secure_prefs";
    private static volatile SharedPreferences instance;

    /** Возвращает единственный (Singleton, с double-checked locking) экземпляр зашифрованных SharedPreferences приложения. */
    public static SharedPreferences get(Context context) {
        if (instance == null) {
            synchronized (PrefsHelper.class) {
                if (instance == null) {
                    instance = create(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /** Создаёт EncryptedSharedPreferences с мастер-ключом AES-256-GCM, сгенерированным и хранимым в Android Keystore. */
    private static SharedPreferences create(Context ctx) {
        try {
            MasterKey masterKey = new MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    ctx,
                    FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("PrefsHelper: cannot create encrypted prefs", e);
        }
    }
}