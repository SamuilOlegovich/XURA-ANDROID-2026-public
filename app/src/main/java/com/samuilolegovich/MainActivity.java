package com.samuilolegovich;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

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

        if (RootDetector.isRooted(this) && !rootWarningShown) {
            showRootWarning();
        } else {
            handleStartup();
        }
    }



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



    private TextView tvTestnetBadge;

    private void setButtons() {
        transactionHistory = findViewById(R.id.transaction_history_link);
        yourBalanceText    = findViewById(R.id.your_balance_text);
        request            = findViewById(R.id.request_link);
        balance            = findViewById(R.id.balance_linc);
        balanceLoading     = findViewById(R.id.balance_loading);
        send               = findViewById(R.id.next_link);
        tvTestnetBadge     = findViewById(R.id.tv_testnet_badge);
    }


    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        yourBalanceText.setText(R.string.your_balance);
        updateTestnetBadge();
    }

    private void updateTestnetBadge() {
        if (tvTestnetBadge == null) return;
        tvTestnetBadge.setVisibility(NetworkConfig.IS_TESTNET ? View.VISIBLE : View.GONE);
    }



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





    // Вызывается из других Activity после wallet-операций
    public void updateWallet() {
        viewModel.loadBalance();
    }



    public void setLanguageThread() {
        runOnUiThread(this::recreate);
    }



    @Override
    protected void onPause() {
        super.onPause();
        VISIBLE_ON_SCREEN = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        VISIBLE_ON_SCREEN = true;
        updateTestnetBadge();
        if (Boolean.TRUE.equals(viewModel.getWalletReady().getValue())) {
            viewModel.loadBalance();
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, XrplSocketService.class));
        super.onDestroy();
        MAIN_ACTIVITY = null;
    }

    private void startXrplSocketService() {
        Intent intent = new Intent(this, XrplSocketService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }



    private void goToAnotherPage(String namePage) {
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }
}