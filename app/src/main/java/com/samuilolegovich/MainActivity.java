package com.samuilolegovich;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.RootDetector;
import com.samuilolegovich.utils.SecureSeedStorage;
import com.samuilolegovich.utils.SessionPin;
import com.samuilolegovich.utils.SignatureVerifier;
import com.samuilolegovich.view.Lost;
import com.samuilolegovich.view.Win;
import com.samuilolegovich.view.YourReferral;
import com.samuilolegovich.viewmodel.MainViewModel;
import com.samuilolegovich.viewmodel.NavigationEvent;

import java.util.Locale;

import static com.samuilolegovich.view.OnboardingActivity.ONBOARDING_CLASS;
import static com.samuilolegovich.view.EnterApplicationPassword.ENTER_APPLICATION_PASSWORD_CLASS;
import static com.samuilolegovich.view.RestoreOrCreateNewWallet.RESTORE_OR_NEW_WALLET_CLASS;
import static com.samuilolegovich.view.TransactionHistory.TRANSACTION_HISTORY_CLASS;
import static com.samuilolegovich.view.SetAnAppPassword.SET_AN_APP_PASSWORD_CLASS;
import static com.samuilolegovich.view.ReceivePayment.RECEIVE_PAYMENT_CLASS;
import static com.samuilolegovich.view.YourReferral.YOUR_REFERRAL_CLASS;
import static com.samuilolegovich.view.SendPayment.SEND_PAYMENT_CLASS;
import com.samuilolegovich.view.Settings;
import static com.samuilolegovich.view.Settings.SETTINGS_CLASS;
import static com.samuilolegovich.view.Lost.LOST_CLASS;
import static com.samuilolegovich.view.Win.WIN_CLASS;
import com.samuilolegovich.config.NetworkConfig;
import dagger.hilt.android.AndroidEntryPoint;




/**
 * Главный экран приложения — отображает баланс кошелька и точки входа в отправку/приём платежей и историю.
 * Также отвечает за стартовую логику запуска: проверку подписи APK, root-детект,
 * определение, нужно ли показать создание пароля, восстановление кошелька или ввод пароля,
 * и запуск фонового сервиса подписки на XRPL-сокет.
 */
@AndroidEntryPoint
public class MainActivity extends BaseActivity {
    public static final String MAIN_ACTIVITY_CLASS = ".MainActivity";
    public static final long ONE_XRP_IN_DROPS = 1_000_000L;

    public static volatile Boolean IS_REAL_GAME_MODE = false;
    public static volatile boolean VISIBLE_ON_SCREEN = false;
    public static volatile boolean START_FLAG = true;

    public static Locale newLocale;

    @SuppressLint("StaticFieldLeak")
    public static volatile MainActivity MAIN_ACTIVITY;

    private static boolean rootWarningShown = false;

    private static final long INTRO_STAGGER_MS    = 110L;
    private static final long INTRO_CARD_DURATION = 380L;
    private static final long INTRO_ELEM_DURATION = 350L;

    private MainViewModel viewModel;
    private SharedPreferences preferences;

    private View logoWallet;
    private View transactionHistory;
    private TextView yourBalanceText;
    private TextView balance;
    private View request;
    private View send;

    private Handler introHandler;
    private com.google.android.material.progressindicator.CircularProgressIndicator balanceLoading;



    /**
     * Инициализирует экран: поднимает режим игры и сеть из настроек, настраивает ViewModel,
     * подписывается на баланс/навигационные события/готовность кошелька, после чего
     * проверяет подпись APK и root перед тем как продолжить обычный запуск приложения.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MAIN_ACTIVITY = this;

        preferences = PrefsHelper.get(this);
        // При первом запуске явно фиксируем trial-режим — безопаснее полагаться на явную запись, а не на дефолт
        if (!preferences.contains("first_run_done")) {
            preferences.edit()
                    .putString(StringEnum.APP_GAME_MODE.getValue(), StringEnum.APP_GAME_MODE_TEST.getValue())
                    .putBoolean("first_run_done", true)
                    .apply();
        }
        IS_REAL_GAME_MODE = preferences.getString(StringEnum.APP_GAME_MODE.getValue(), "false")
                .equalsIgnoreCase("true");
        NetworkConfig.load(preferences);
        // newLocale уже установлен BaseActivity.applyLocale() до super.onCreate()
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        introHandler = new Handler(Looper.getMainLooper());
        setButtons();
        setLanguage();
        listeners();
        setupBottomNav();

        viewModel.getBalance().observe(this, b -> {
            if (b == null) return;
            balance.setText(String.format(Locale.US, "%.2f XRP", b.doubleValue()));
            balance.setVisibility(View.VISIBLE);
            balanceLoading.setVisibility(View.GONE);
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        });



        viewModel.getNavigationEvent().observe(this, event -> {
            if (event == null) return;
            switch (event.type) {
                case NavigationEvent.LOST:
                    Lost.MASSAGE = event.message;
                    goToAnotherPage(LOST_CLASS);
                    break;
                case NavigationEvent.WIN:
                    Win.MASSAGE = event.message;
                    goToAnotherPage(WIN_CLASS);
                    break;
                case NavigationEvent.YOUR_REFERRAL:
                    YourReferral.MASSAGE = event.message;
                    goToAnotherPage(YOUR_REFERRAL_CLASS);
                    break;
            }
            viewModel.clearNavigationEvent();
        });

        viewModel.getWalletReady().observe(this, ready -> {
            if (Boolean.TRUE.equals(ready)) startXrplSocketService();
        });

        if (!SignatureVerifier.isSignatureValid(this)) {
            showTamperedWarning();
        } else if (RootDetector.isRooted(this) && !rootWarningShown) {
            showRootWarning();
        } else {
            handleStartup();
        }
    }



    /** Показывает блокирующий диалог о несовпадении подписи APK — признак подделанной/клонированной сборки. */
    private void showTamperedWarning() {
        new AlertDialog.Builder(this)
                .setTitle("Целостность приложения нарушена")
                .setMessage(
                        "Это приложение подписано не тем сертификатом, с которым оно собиралось.\n\n" +
                        "Скорее всего, это изменённая или клонированная копия XURA — она может " +
                        "содержать вредоносный код, ворующий seed-фразу и средства.\n\n" +
                        "Установите XURA из официального источника.")
                .setCancelable(false)
                .setPositiveButton("Выйти", (d, w) -> finishAffinity())
                .show();
    }



    /** Показывает предупреждение о root-доступе с возможностью продолжить на свой риск или выйти из приложения. */
    private void showRootWarning() {
        rootWarningShown = true;
        new AlertDialog.Builder(this)
                .setTitle("Обнаружен root-доступ")
                .setMessage(
                        "На этом устройстве обнаружен root-доступ или неофициальная прошивка.\n\n" +
                        "Root открывает другим приложениям доступ к защищённым данным, " +
                        "включая seed-фразу вашего кошелька.\n\n" +
                        "Рекомендуем использовать XURA только на устройствах без root.")
                .setCancelable(false)
                .setPositiveButton("Продолжить на свой риск", (d, w) -> handleStartup())
                .setNegativeButton("Выйти", (d, w) -> {
                    rootWarningShown = false;
                    finishAffinity();
                })
                .show();
    }



    /** Применяет сохранённую локаль (newLocale) к ресурсам Activity или сбрасывает на английский по умолчанию. */
    public void setLocale() {
        if (newLocale != null) {
            Resources resources = getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocale(newLocale);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        } else {
            Locale.setDefault(Locale.ENGLISH);
        }
    }



    /**
     * Определяет, на какой экран направить пользователя при запуске:
     * установка пароля приложения (если его нет), восстановление/создание кошелька
     * (если нет сохранённого seed) либо ввод пароля (если сессия не разблокирована).
     * Если seed сохранён в старом формате или повреждён — он удаляется и запрашивается восстановление заново.
     */
    private void handleStartup() {
        boolean onboardingSeen = preferences.getBoolean(StringEnum.ONBOARDING_SEEN.getValue(), false);
        if (!onboardingSeen) {
            goToAnotherPage(ONBOARDING_CLASS);
            return;
        }

        boolean isSetPassword = preferences.getString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "")
                .equalsIgnoreCase(StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue());
        boolean isPassword = preferences.contains(StringEnum.APP_PREFERENCES_PASSWORD.getValue());
        boolean isSeed = preferences.contains(StringEnum.APP_PREFERENCES_SEED.getValue());

        if (!isPassword) {
            goToAnotherPage(SET_AN_APP_PASSWORD_CLASS);
            return;
        }

        if (!isSeed) {
            goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
            return;
        }

        // Если seed зашифрован PIN-слоем, а PIN в сессии ещё нет — сначала идём на аутентификацию.
        // После ввода PIN SessionPin.set() будет вызван в EnterApplicationPassword, затем
        // handleStartup() запустится снова (уже с PIN) и seed будет загружен.
        boolean seedPinEnabled = "true".equals(preferences.getString(
                StringEnum.APP_PREFERENCES_SEED_PIN_ENABLED.getValue(), "false"));
        if (seedPinEnabled && !SessionPin.isAvailable()) {
            if (!isSetPassword && START_FLAG) {
                goToAnotherPage(ENTER_APPLICATION_PASSWORD_CLASS);
            }
            return;
        }

        String seed = SecureSeedStorage.loadSeed(preferences, SessionPin.get());
        if (seed == null) {
            SecureSeedStorage.delete(preferences, StringEnum.APP_PREFERENCES_SEED.getValue());
            preferences.edit()
                    .remove(StringEnum.APP_PREFERENCES_SEED_PIN_ENABLED.getValue())
                    .remove(StringEnum.APP_PREFERENCES_SEED_PIN_SALT.getValue())
                    .apply();
            goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
            return;
        }

        // Тихая миграция: если seed был без PIN-слоя и PIN теперь известен — перешифровываем.
        if (!seedPinEnabled && SessionPin.isAvailable()) {
            SecureSeedStorage.saveSeed(preferences, seed, SessionPin.get());
        }

        viewModel.restoreAndInit(seed);

        if (!isSetPassword && START_FLAG) {
            goToAnotherPage(ENTER_APPLICATION_PASSWORD_CLASS);
        }
    }



    private TextView           tvTestnetBadge;
    private SwipeRefreshLayout swipeRefresh;

    /** Находит View экрана по id и настраивает swipe-to-refresh для ручного обновления баланса. */
    private void setButtons() {
        logoWallet         = findViewById(R.id.logo_xura);
        transactionHistory = findViewById(R.id.transaction_history_link);
        yourBalanceText    = findViewById(R.id.your_balance_text);
        request            = findViewById(R.id.request_link);
        balance            = findViewById(R.id.balance_linc);
        balanceLoading     = findViewById(R.id.balance_loading);
        send               = findViewById(R.id.next_link);
        tvTestnetBadge     = findViewById(R.id.tv_testnet_badge);
        swipeRefresh       = findViewById(R.id.swipe_refresh);

        swipeRefresh.setColorSchemeResources(R.color.xura_purple, R.color.xura_cyan);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.xura_card);
        swipeRefresh.setOnRefreshListener(() -> viewModel.loadBalance());
    }


    /** Выставляет текст подписи баланса согласно текущей локали и обновляет видимость бейджа тестовой сети. */
    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        yourBalanceText.setText(R.string.your_balance);
        updateTestnetBadge();
    }

    /** Показывает бейдж над балансом: TESTNET (только если включён) → скрыт (mainnet). */
    private void updateTestnetBadge() {
        if (tvTestnetBadge == null) return;
        if (NetworkConfig.IS_TESTNET) {
            tvTestnetBadge.setText(R.string.settings_testnet_badge);
            tvTestnetBadge.setBackgroundResource(R.drawable.bg_chip_testnet);
            tvTestnetBadge.setVisibility(View.VISIBLE);
        } else {
            tvTestnetBadge.setVisibility(View.GONE);
        }
    }



    /** Подключает обработчики нажатий на кнопки "получить", "отправить" и "история транзакций". */
    private void listeners() {
        request.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(RECEIVE_PAYMENT_CLASS);
        });

        send.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(SEND_PAYMENT_CLASS);
        });

        transactionHistory.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(TRANSACTION_HISTORY_CLASS);
        });
    }





    /** Принудительно перезапрашивает баланс кошелька. Вызывается из других Activity после операций с кошельком. */
    public void updateWallet() {
        viewModel.loadBalance();
    }

    /** Пересоздаёт Activity в основном потоке, чтобы применить смену языка интерфейса. */
    public void setLanguageThread() {
        runOnUiThread(this::recreate);
    }

    /** Сбрасывает флаг видимости экрана при уходе в фон — используется для подавления лишних обновлений UI. */
    @Override
    protected void onPause() {
        super.onPause();
        VISIBLE_ON_SCREEN = false;
        resetWalletViews();
    }

    /** При возврате на экран отмечает его видимым, обновляет бейдж сети, подгружает баланс и запускает входную анимацию. */
    @Override
    protected void onResume() {
        super.onResume();
        VISIBLE_ON_SCREEN = true;
        updateTestnetBadge();
        if (Boolean.TRUE.equals(viewModel.getWalletReady().getValue())) {
            viewModel.loadBalance();
        }
        if (Settings.isAnimationsEnabled(this)) {
            playEntranceAnimation();
        }
    }

    // ─── Входная анимация ───────────────────────────────────────────────────

    /**
     * Карточки влетают снизу вверх (история → получить → отправить),
     * затем появляются надпись баланса и спиннер, последним — лого.
     */
    private void playEntranceAnimation() {
        View[] cards = { transactionHistory, request, send };
        float slideFromY = 150 * getResources().getDisplayMetrics().density;

        for (View c : cards) {
            if (c == null) continue;
            c.animate().cancel();
            c.setAlpha(0f);
            c.setTranslationY(slideFromY);
        }
        if (logoWallet    != null) { logoWallet   .animate().cancel(); logoWallet   .setAlpha(0f); }
        if (yourBalanceText!= null) { yourBalanceText.animate().cancel(); yourBalanceText.setAlpha(0f); }
        if (tvTestnetBadge != null) { tvTestnetBadge .animate().cancel(); tvTestnetBadge .setAlpha(0f); }
        if (balanceLoading != null) { balanceLoading .animate().cancel(); balanceLoading .setAlpha(0f); }
        if (balance        != null) { balance        .animate().cancel(); balance        .setAlpha(0f); }

        introHandler.post(() -> {
            for (int i = 0; i < cards.length; i++) {
                if (cards[i] == null) continue;
                ViewPropertyAnimator anim = cards[i].animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(INTRO_CARD_DURATION)
                        .setStartDelay((long) i * INTRO_STAGGER_MS)
                        .setInterpolator(new DecelerateInterpolator(2f));

                if (i == cards.length - 1) {
                    anim.withEndAction(() -> {
                        // Шаг 1: надпись баланса + плашка режима + спиннер
                        if (balanceLoading != null)
                            balanceLoading.animate().alpha(1f).setDuration(INTRO_ELEM_DURATION).setInterpolator(null);
                        if (balance != null)
                            balance.animate().alpha(1f).setDuration(INTRO_ELEM_DURATION).setInterpolator(null);
                        if (tvTestnetBadge != null && tvTestnetBadge.getVisibility() == View.VISIBLE)
                            tvTestnetBadge.animate().alpha(1f).setDuration(INTRO_ELEM_DURATION).setInterpolator(null);
                        if (yourBalanceText != null)
                            yourBalanceText.animate().alpha(1f).setDuration(INTRO_ELEM_DURATION).setInterpolator(null)
                                .withEndAction(() -> {
                                    // Шаг 2: лого — последним
                                    if (logoWallet != null)
                                        logoWallet.animate().alpha(1f).setDuration(INTRO_ELEM_DURATION).setInterpolator(null);
                                });
                    });
                }
            }
        });
    }

    /** Сбрасывает все View кошелька в нормальное состояние и отменяет анимации. */
    private void resetWalletViews() {
        View[] cards = { transactionHistory, request, send };
        for (View c : cards) {
            if (c == null) continue;
            c.animate().cancel();
            c.setAlpha(1f);
            c.setTranslationY(0f);
        }
        if (logoWallet    != null) { logoWallet   .animate().cancel(); logoWallet   .setAlpha(1f); logoWallet.setTranslationY(0f); }
        if (yourBalanceText!= null) { yourBalanceText.animate().cancel(); yourBalanceText.setAlpha(1f); }
        if (tvTestnetBadge != null) { tvTestnetBadge .animate().cancel(); tvTestnetBadge .setAlpha(1f); }
        if (balanceLoading != null) { balanceLoading .animate().cancel(); balanceLoading .setAlpha(1f); }
        if (balance        != null) { balance        .animate().cancel(); balance        .setAlpha(1f); }
    }

    /** Останавливает фоновый сервис подписки на XRPL-сокет и очищает статическую ссылку на Activity. */
    @Override
    protected void onDestroy() {
        stopService(new Intent(this, XrplSocketService.class));
        super.onDestroy();
        MAIN_ACTIVITY = null;
    }

    /** Запускает XrplSocketService как foreground-сервис (Android 8+) или обычный сервис на старых версиях. */
    private void startXrplSocketService() {
        Intent intent = new Intent(this, XrplSocketService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }



    /** Запускает другую Activity по её action-имени. */
    private void goToAnotherPage(String namePage) {
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }
}