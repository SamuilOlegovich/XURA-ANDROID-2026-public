package com.samuilolegovich;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.provider.Settings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.samuilolegovich.asyncAndRun.asyncTask.GetBalanceAsync;
import com.samuilolegovich.asyncAndRun.asyncTask.RestoreWalletAsync;
import com.samuilolegovich.asyncAndRun.runnable.SubscriberRun;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.utils.Cipher;
import com.samuilolegovich.view.Lost;
import com.samuilolegovich.view.Win;
import com.samuilolegovich.view.YourReferral;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.samuilolegovich.view.EnterApplicationPassword.ENTER_APPLICATION_PASSWORD_CLASS;
import static com.samuilolegovich.view.Lost.LOST_CLASS;
import static com.samuilolegovich.view.RestoreOrCreateNewWallet.RESTORE_OR_NEW_WALLET_CLASS;
import static com.samuilolegovich.view.SetAnAppPassword.SET_AN_APP_PASSWORD_CLASS;
import static com.samuilolegovich.view.ReceivePayment.RECEIVE_PAYMENT_CLASS;
import static com.samuilolegovich.view.SendPayment.SEND_PAYMENT_CLASS;
import static com.samuilolegovich.view.SelectGame.SELECT_GAME_CLASS;
import static com.samuilolegovich.view.InfoMain.INFO_MAIN_CLASS;
import static com.samuilolegovich.view.Settings.SETTINGS_CLASS;
import static com.samuilolegovich.view.TransactionHistory.TRANSACTION_HISTORY_CLASS;
import static com.samuilolegovich.view.Win.WIN_CLASS;
import static com.samuilolegovich.view.YourReferral.YOUR_REFERRAL_CLASS;



// тут мы запросим придумать пароль к приложению либо оставим без пароля
public class MainActivity extends AppCompatActivity {
    public static final String MAIN_ACTIVITY_CLASS = ".MainActivity";
    public static final long ONE_XRP_IN_DROPS = 1_000_000L;

    public static volatile boolean VISIBLE_ON_SCREEN = false;
    public static volatile boolean START_FLAG = true;

    @SuppressLint("StaticFieldLeak")
    public static volatile MainActivity MAIN_ACTIVITY;

    private SharedPreferences preferences;
    private Animation animTranslate;
    private BigDecimal balanceXRP;
    private Locale newLocale;
    private String lottoNow;

    private TextView transactionHistory;
    private TextView yourBalanceText;
    private TextView lottoTextGo;
    private TextView settings;
    private TextView balance;
    private TextView request;
    private TextView send;
    private TextView info;

    private ImageButton logoButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        start();
        setLocale();
        setContentView(R.layout.activity_main);

        lottoNow = Lotto.genLotto() + "";
        MAIN_ACTIVITY = this;

        setButtons();
        setLanguage();
        setBalance();
        listeners();
        goText();
    }



    private void start() {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);
        newLocale = new Locale(preferences.getString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), "en"));

        boolean isSetPassword = preferences.getString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "")
                .equalsIgnoreCase(StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue());
        boolean isPassword = preferences.contains(StringEnum.APP_PREFERENCES_PASSWORD.getValue());
        boolean isSeed = preferences.contains(StringEnum.APP_PREFERENCES_SEED.getValue());

        restoreWallet(isSeed);

        // если пароль вообще ни разу не устанавливался
        if (!isPassword) {
            goToAnotherPage(SET_AN_APP_PASSWORD_CLASS);
        }

        // если установка пароля пропущена и нет ни каого кошелька
        else if (isPassword && isSetPassword && !isSeed) {
            goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
        }

        // если пароль установлен и нет ни каого кошелька
        else if (isPassword && !isSetPassword && !isSeed) {
            goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
        }

        // если установка пароля пропущена и кошелек есть
        else if (isPassword && isSetPassword && isSeed) {
//            goToAnotherPage(WALLET_CLASS);
            startSocket();
        }

        // если пароль установлен
        else if (isPassword && !isSetPassword && isSeed) {
            if (START_FLAG) {
                startSocket();
                goToAnotherPage(ENTER_APPLICATION_PASSWORD_CLASS);
            }
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void setLocale() {
        if (newLocale != null) {
            Resources resources = getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocale(newLocale);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        } else {
            Locale.setDefault(Locale.ENGLISH);
        }
    }


    private void startSocket() {
        Runnable runnable = new SubscriberRun();
        Thread thread = new Thread(runnable);
        thread.start();
    }


    private void setButtons() {
        transactionHistory = (TextView) findViewById(R.id.transaction_history_link);
        yourBalanceText = (TextView) findViewById(R.id.your_balance_text);
        lottoTextGo = (TextView) findViewById(R.id.lotto_text_go_link);
        logoButton = (ImageButton) findViewById(R.id.logo_button_link);
        settings = (TextView) findViewById(R.id.settings_linc);
        request = (TextView) findViewById(R.id.request_link);
        balance = (TextView) findViewById(R.id.balance_linc);
        send = (TextView) findViewById(R.id.send_link);
        info = (TextView) findViewById(R.id.info_link);
    }


    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        transactionHistory.setText(R.string.transaction_history);
        yourBalanceText.setText(R.string.your_balance);
        lottoTextGo.setText(R.string.want_to_win);
        settings.setText(R.string.settings_main);
        request.setText(R.string.request);
        send.setText(R.string.send);
        info.setText(R.string.info);
    }



    @SuppressLint("SetTextI18n")
    private void setBalance() {
        balance.setText(balanceXRP.toString() + " XRP");
    }

    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);
        lottoTextGo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(SELECT_GAME_CLASS);
                    }
                }
        );

        settings.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(SETTINGS_CLASS);
                    }
                }
        );

        request.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(RECEIVE_PAYMENT_CLASS);
                    }
                }
        );

        send.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(SEND_PAYMENT_CLASS);
                    }
                }
        );

        info.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(INFO_MAIN_CLASS);
                    }
                }
        );

        logoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(SELECT_GAME_CLASS);
                    }
                }
        );

        transactionHistory.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(TRANSACTION_HISTORY_CLASS);
                    }
                }
        );
    }



    // настройка для бегущей строки
    @SuppressLint("SetTextI18n")
    private void goText() {
//        lottoTextGo.setText("WANT TO WIN LOTTO - "
//                +  lottoNow
//                + " XRP - CLICK ON THE LOGO AND START PLAYING!!!");
        lottoTextGo.setText("GO TO THE DARK SIDE - FIND THE SECRET BUTTON!");
        lottoTextGo.setSelected(true);
    }



    @SuppressLint("HardwareIds")
    private void restoreWallet(boolean isSeed) {
        if (isSeed) {
            AsyncTask<String, Void, Map<String, String>> asyncTask = new RestoreWalletAsync()
                    .execute(Cipher.decryptString(preferences.getString(StringEnum.APP_PREFERENCES_SEED.getValue(), ""),
                    preferences.getString(StringEnum.APP_PREFERENCES_SALT.getValue(), ""),
                    Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)));
            getBalance();
        } else {
            balanceXRP = new BigDecimal("0.000000");
        }
    }



    public void updateWallet() {
        getBalance();
        setBalance();
    }

    public void updateBalance(BigDecimal balance) {
        balanceXRP = balance;
        setBalance();
    }

    public void setLottoNow(String lotto) {
        new Thread() {
            public void run() {
                MAIN_ACTIVITY.runOnUiThread(new Runnable() {
                    public void run() {
                        //Do your UI operations like dialog opening or Toast here
                        lottoNow = lotto;
                        goText();
                    }
                });
            }
        }.start();
    }

    public void notifyAboutAnEvent(String massage, String lotto, int i) {
        new Thread() {
            public void run() {
                MAIN_ACTIVITY.runOnUiThread(new Runnable() {
                    public void run() {
                        //Do your UI operations like dialog opening or Toast here
                        if (i == 1) {
                            Lost.MASSAGE = massage;
                            goToAnotherPage(LOST_CLASS);
                        } else if (i == 2) {
                            Win.MASSAGE = massage;
                            goToAnotherPage(WIN_CLASS);
                        } else if (i == 3) {
                            YourReferral.MASSAGE = massage;
                            goToAnotherPage(YOUR_REFERRAL_CLASS);
                        }
                    }
                });
            }
        }.start();
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



    private void getBalance() {
        AsyncTask<String, Void, BigDecimal>  getBalanceAsync = new GetBalanceAsync().execute("");
        try {
            balanceXRP = getBalanceAsync.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }
}