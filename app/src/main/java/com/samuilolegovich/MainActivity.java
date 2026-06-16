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
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.RootDetector;
import com.samuilolegovich.utils.SecureSeedStorage;
import com.samuilolegovich.utils.SignatureVerifier;
import com.samuilolegovich.view.Lost;
import com.samuilolegovich.view.Win;
import com.samuilolegovich.view.YourReferral;
import com.samuilolegovich.viewmodel.MainViewModel;
import com.samuilolegovich.viewmodel.NavigationEvent;

import java.util.Locale;

import static com.samuilolegovich.view.EnterApplicationPassword.ENTER_APPLICATION_PASSWORD_CLASS;
import static com.samuilolegovich.view.RestoreOrCreateNewWallet.RESTORE_OR_NEW_WALLET_CLASS;
import static com.samuilolegovich.view.TransactionHistory.TRANSACTION_HISTORY_CLASS;
import static com.samuilolegovich.view.SetAnAppPassword.SET_AN_APP_PASSWORD_CLASS;
import static com.samuilolegovich.view.ReceivePayment.RECEIVE_PAYMENT_CLASS;
import static com.samuilolegovich.view.YourReferral.YOUR_REFERRAL_CLASS;
import static com.samuilolegovich.view.SendPayment.SEND_PAYMENT_CLASS;
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

    private MainViewModel viewModel;
    private SharedPreferences preferences;

    private View transactionHistory;
    private TextView yourBalanceText;
    private TextView balance;
    private View request;
    private View send;

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
        IS_REAL_GAME_MODE = preferences.getString(StringEnum.APP_GAME_MODE.getValue(), "false")
                .equalsIgnoreCase("true");
        NetworkConfig.load(preferences);
        // newLocale уже установлен BaseActivity.applyLocale() до super.onCreate()
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setButtons();
        setLanguage();
        listeners();
        setupBottomNav();

        viewModel.getBalance().observe(this, b -> {
            if (b == null) return;
            balance.setText(b.stripTrailingZeros().toPlainString() + " XRP");
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

        // Пробуем расшифровать через Keystore; null = старый формат или повреждено
        String seed = SecureSeedStorage.load(preferences, StringEnum.APP_PREFERENCES_SEED.getValue());
        if (seed == null) {
            SecureSeedStorage.delete(preferences, StringEnum.APP_PREFERENCES_SEED.getValue());
            goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
            return;
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
        swipeRefresh.setOnRefreshListener(() -> {
            if (com.samuilolegovich.config.NetworkConfig.IS_TESTNET) {
                com.samuilolegovich.wallet.repository.WalletRepository.getInstance().loadNetworkBalance();
            } else {
                viewModel.loadBalance();
            }
        });
    }


    /** Выставляет текст подписи баланса согласно текущей локали и обновляет видимость бейджа тестовой сети. */
    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        yourBalanceText.setText(R.string.your_balance);
        updateTestnetBadge();
    }

    /** Показывает или скрывает бейдж "тестовая сеть" в зависимости от текущего режима сети (testnet/mainnet). */
    private void updateTestnetBadge() {
        if (tvTestnetBadge == null) return;
        tvTestnetBadge.setVisibility(NetworkConfig.IS_TESTNET ? View.VISIBLE : View.GONE);
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
    }

    /** При возврате на экран отмечает его видимым, обновляет бейдж сети и подгружает баланс, если кошелёк готов. */
    @Override
    protected void onResume() {
        super.onResume();
        VISIBLE_ON_SCREEN = true;
        updateTestnetBadge();
        if (Boolean.TRUE.equals(viewModel.getWalletReady().getValue())) {
            viewModel.loadBalance();
        }
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