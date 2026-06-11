package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.asyncTask.RestoreWalletAsync;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Cipher;
import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.samuilolegovich.MainActivity.MAIN_ACTIVITY;
import static com.samuilolegovich.view.Referral.REFERRAL_CLASS;



// тут востанавливаем кошелек
public class RestoreWallet extends BaseActivity {
    public static final String RESTORE_WALLET_CLASS = ".RestoreWallet";

    private PaymentAndSocketManagerXRPL paymentAndSocketManagerXRPL;

    private String ERROR_CHECK_THE_SEED_AND_TRY_AGAIN;

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private Animation animTranslate;

    private TextView restoreWalletTextView;
    private EditText seed;
    private TextView next;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        paymentAndSocketManagerXRPL = PaymentAndSocketManagerXRPL.getInstances();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore_wallet_page);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        restoreWalletTextView = (TextView) findViewById(R.id.restore_wallet_text_view);
        seed = (EditText) findViewById(R.id.restore_wallet_seed_field);
        next = (TextView) findViewById(R.id.restore_wallet_next_link);
    }


    private void setLanguage() {
        ERROR_CHECK_THE_SEED_AND_TRY_AGAIN = getString(R.string.error_check_the_seed_and_try_again);

        restoreWalletTextView.setText(R.string.restore_from_backup_seed);
        next.setText(R.string.next);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        next.setOnClickListener(
                new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        String seedRestore = seed.getText().toString();

                        if (seedRestore.length() > 20) {
                            if (recoverWallet(seedRestore)) {
                                encryptAndWriteSeed(seedRestore);
                                MainActivity.START_FLAG = false;
                                MAIN_ACTIVITY.updateWallet();
                                goToAnotherPage(REFERRAL_CLASS);
                            } else {
                                makeToast(ERROR_CHECK_THE_SEED_AND_TRY_AGAIN);
                            }
                        } else {
                            makeToast(ERROR_CHECK_THE_SEED_AND_TRY_AGAIN);
                        }
                    }
                }
        );
    }


    @SuppressLint("HardwareIds")
    private void encryptAndWriteSeed(String seedRestore) {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(),
                Context.MODE_PRIVATE);

        editor = preferences.edit();
        editor.putString(StringEnum.APP_PREFERENCES_SEED.getValue(),
                Cipher.encryptString(seedRestore,
                        preferences.getString(StringEnum.APP_PREFERENCES_SALT.getValue(),
                                ""),
                        Settings.Secure.getString(getContentResolver(),
                                Settings.Secure.ANDROID_ID)));
        editor.apply();
    }


    // запустить менеджер и реализовать востановление кошелька
    private boolean recoverWallet(String seedRestore)  {
        AsyncTask<String, Void, Map<String, String>> asyncTask = new RestoreWalletAsync().execute(seedRestore);
        Map<String, String> map = null;

        try {
            map = asyncTask.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if (map != null && map.containsKey("Classic Address")) {
            return true;
        }

        return false;
    }


    private void makeToast(String massage) {
        Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0,110);   // import android.view.Gravity;
        toast.show();
    }


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
