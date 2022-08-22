package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Cipher;

import static com.samuilolegovich.MainActivity.MAIN_ACTIVITY;
import static com.samuilolegovich.view.Referral.REFERRAL_CLASS;

// тут мы сверим информаци о новом кошельке, правильно ли ее записал юзер
public class CheckingNewWallet extends AppCompatActivity {
    public static final String CHECKING_NEW_WALLET_CLASS = ".CheckingNewWallet";

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private Animation animTranslate;

    private EditText seed;
    private TextView next;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checking_new_wallet);
        setButtons();
        listeners();
    }

    private void setButtons() {
        next = (TextView) findViewById(R.id.linkConfirmNext);
        seed = (EditText) findViewById(R.id.editTextSeed);
    }

    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);
        next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String seedOne = getPreSeed();
                        String seedTwo = seed.getText().toString();
                        v.startAnimation(animTranslate);

                        if (seedOne.equals(seedTwo)) {
                            setSeed(seedOne);
                            MainActivity.START_FLAG = false;
                            MAIN_ACTIVITY.updateWallet();
                            goToAnotherPage(REFERRAL_CLASS);
                        } else {
                            seed.setText("");
                            makeToast(StringEnum.SEED_DOES_NOT_MATCH.getValue());
                        }
                    }
                }
        );
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



    @SuppressLint("HardwareIds")
    private String getPreSeed() {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);
        return Cipher.decryptString(preferences.getString(
                StringEnum.APP_PREFERENCES_PRE_SEED.getValue(), ""),
                preferences.getString(StringEnum.APP_PREFERENCES_SALT.getValue(), ""),
                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
    }

    @SuppressLint("HardwareIds")
    private void setSeed(String newSeed) {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString(StringEnum.APP_PREFERENCES_SEED.getValue(),
                Cipher.encryptString(newSeed,
                        preferences.getString(StringEnum.APP_PREFERENCES_SALT.getValue(), ""),
                        Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)));
        editor.apply();
    }

    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
