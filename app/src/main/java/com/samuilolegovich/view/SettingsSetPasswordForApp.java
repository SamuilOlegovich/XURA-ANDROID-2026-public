package com.samuilolegovich.view;

import static com.samuilolegovich.view.Settings.SETTINGS_CLASS;

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

import java.util.UUID;



public class SettingsSetPasswordForApp extends AppCompatActivity {
    public static final String SETTINGS_SET_PASSWORD_FOR_APP_CLASS = ".SettingsSetPasswordForApp";


    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private Animation animTranslate;

    private EditText passwordOne;
    private EditText passwordTwo;
    private TextView textView;
    private TextView confirm;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.settings_set_password_for_app);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        textView = (TextView) findViewById(R.id.settings_set_password_app_text_view);
        passwordTwo = (EditText) findViewById(R.id.edit_text_passport_tow);
        passwordOne = (EditText) findViewById(R.id.edit_text_passport_one);
        confirm = (TextView) findViewById(R.id.confirm_link);
    }


    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        textView.setText(R.string.set_password_to_enter_application);
        passwordOne.setText(R.string.enter_password_low_case);
        passwordTwo.setText(R.string.repeat_enter);
        confirm.setText(R.string.set_password);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        confirm.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        String one = passwordOne.getText().toString();
                        String two = passwordTwo.getText().toString();

                        if (one.length() > 3 && one.equals(two)) {
                            setPasswordForApp(one, true);
                        } else {
                            passwordOne.setText("");
                            passwordTwo.setText("");
                            makeToast(StringEnum.PASSWORD_DOES_NOT_MATCH.getValue());
                        }
                    }
                }
        );
    }


    private void makeToast(String massage) {
        // import android.view.Gravity
        Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0,110);
        toast.show();
    }


    private void setPasswordForApp(String password, boolean b) {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);

        @SuppressLint("HardwareIds")
        String androidId = android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String salt = UUID.randomUUID().toString();
        String saltEncrypt = Cipher.encryptStringForSalt(salt, androidId);

        editor = preferences.edit();
        editor.putString(StringEnum.APP_PREFERENCES_SALT.getValue(), saltEncrypt);

        if (b) {
            editor.putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(),
                    Cipher.encryptStringIrreversibly(password, saltEncrypt, androidId));
        } else {
            editor.putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(), password);
        }
        editor.apply();
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
