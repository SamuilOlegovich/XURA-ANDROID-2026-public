package com.samuilolegovich.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

/**
 * Обёртка над androidx.biometric для входа в приложение/подтверждения действий
 * отпечатком пальца или лицом, с понятным callback-интерфейсом (успех/откат на пароль/ошибка).
 */
public class BiometricHelper {

    public interface Callback {
        void onSuccess();
        void onFallback();
        void onError(String message);
    }

    /** true — устройство поддерживает биометрию (BIOMETRIC_STRONG) и она настроена пользователем. */
    public static boolean isAvailable(Context context) {
        BiometricManager manager = BiometricManager.from(context);
        int result = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Показывает системный диалог биометрии. Activity должна быть FragmentActivity.
     * При отмене пользователем или нажатии "USE PASSWORD" вызывает onFallback (переход к паролю),
     * при иной ошибке — onError, при успехе — onSuccess.
     */
    public static void prompt(FragmentActivity activity, String title, String subtitle, Callback callback) {
        BiometricPrompt.AuthenticationCallback authCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                callback.onSuccess();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    callback.onFallback();
                } else {
                    callback.onError(errString.toString());
                }
            }

            @Override
            public void onAuthenticationFailed() {
                // палец не распознан — диалог сам покажет ошибку, ничего не делаем
            }
        };

        BiometricPrompt prompt = new BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                authCallback
        );

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText("USE PASSWORD")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();

        prompt.authenticate(info);
    }
}
