package com.samuilolegovich;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Cipher;
import com.samuilolegovich.utils.Lotto;
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
import static com.samuilolegovich.view.SelectGame.SELECT_GAME_CLASS;
import static com.samuilolegovich.view.InfoMain.INFO_MAIN_CLASS;
import static com.samuilolegovich.view.Settings.SETTINGS_CLASS;
import static com.samuilolegovich.view.Lost.LOST_CLASS;
import static com.samuilolegovich.view.Win.WIN_CLASS;



public class MainActivity extends BaseActivity {
    public static final String MAIN_ACTIVITY_CLASS = ".MainActivity";
    public static final long ONE_XRP_IN_DROPS = 1_000_000L;

    public static volatile Boolean IS_REAL_GAME_MODE = false;
    public static volatile boolean VISIBLE_ON_SCREEN = false;
    public static volatile boolean START_FLAG = true;

    public static Locale newLocale;

    @SuppressLint("StaticFieldLeak")
    public static volatile MainActivity MAIN_ACTIVITY;

    private String GO_TO_THE_DARK_SIDE_FIND_THE_SECRET_BUTTON;

    private MainViewModel viewModel;
    private SharedPreferences preferences;
    private Animation animTranslate;
    private String lottoNow;

    private TextView transactionHistory;
    private TextView yourBalanceText;
    private TextView lottoTextGo;
    private TextView settings;
    private TextView balance;
    private TextView request;
    private TextView send;
    private TextView info;

    private View logoButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MAIN_ACTIVITY = this;

        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);
        IS_REAL_GAME_MODE = preferences.getString(StringEnum.APP_GAME_MODE.getValue(), "false")
                .equalsIgnoreCase("true");
        // newLocale уже установлен BaseActivity.applyLocale() до super.onCreate()
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        lottoNow = Lotto.genLotto() + "";

        setButtons();
        setLanguage();
        listeners();
        goText();

        viewModel.getBalance().observe(this, b ->
                balance.setText(b.toString() + " XRP"));

        viewModel.getLottoText().observe(this, lotto -> {
            lottoNow = lotto;
            goText();
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

        handleStartup();
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



    @SuppressLint("HardwareIds")
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

        // Кошелёк есть — восстанавливаем и запускаем сокет асинхронно
        String seed = Cipher.decryptString(
                preferences.getString(StringEnum.APP_PREFERENCES_SEED.getValue(), ""),
                preferences.getString(StringEnum.APP_PREFERENCES_SALT.getValue(), ""),
                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

        viewModel.restoreAndInit(seed);

        if (!isSetPassword && START_FLAG) {
            goToAnotherPage(ENTER_APPLICATION_PASSWORD_CLASS);
        }
    }



    private void setButtons() {
        transactionHistory = findViewById(R.id.transaction_history_link);
        yourBalanceText = findViewById(R.id.your_balance_text);
        lottoTextGo = findViewById(R.id.lotto_text_go_link);
        logoButton = findViewById(R.id.logo_button_link);
        settings = findViewById(R.id.settings_linc);
        request = findViewById(R.id.request_link);
        balance = findViewById(R.id.balance_linc);
        info = findViewById(R.id.last_text_view);
        send = findViewById(R.id.next_link);
    }


    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        GO_TO_THE_DARK_SIDE_FIND_THE_SECRET_BUTTON = getString(R.string.go_to_the_dark_side_find_the_secret_button);
        transactionHistory.setText(R.string.transaction_history);
        yourBalanceText.setText(R.string.your_balance);
        lottoTextGo.setText(R.string.want_to_win);
        settings.setText(R.string.settings_main);
        request.setText(R.string.request);
        send.setText(R.string.send);
        info.setText(R.string.info);
    }



    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        settings.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            goToAnotherPage(SETTINGS_CLASS);
        });

        request.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            goToAnotherPage(RECEIVE_PAYMENT_CLASS);
        });

        send.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            goToAnotherPage(SEND_PAYMENT_CLASS);
        });

        info.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            goToAnotherPage(INFO_MAIN_CLASS);
        });

        logoButton.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            goToAnotherPage(SELECT_GAME_CLASS);
        });

        transactionHistory.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            goToAnotherPage(TRANSACTION_HISTORY_CLASS);
        });
    }



    @SuppressLint("SetTextI18n")
    private void goText() {
        lottoTextGo.setText(GO_TO_THE_DARK_SIDE_FIND_THE_SECRET_BUTTON);
        lottoTextGo.setSelected(true);
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
    }



    private void goToAnotherPage(String namePage) {
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }
}