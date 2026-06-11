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

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Cipher;


import static com.samuilolegovich.view.RestoreOrCreateNewWallet.RESTORE_OR_NEW_WALLET_CLASS;



// тут будет вводится и проверяться пароль от приложения для дальнейшего доступа к игре
public class EnterApplicationPassword extends BaseActivity {
    public static final String ENTER_APPLICATION_PASSWORD_CLASS = ".EnterApplicationPassword";

    private SharedPreferences preferences;
    private Animation animTranslate;

    private TextView settingsSetPasswordAppTextView;
    private EditText password;
    private TextView next;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.enter_application_password);
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);

        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        settingsSetPasswordAppTextView = (TextView) findViewById(R.id.settings_set_password_app_text_view);
        password = (EditText) findViewById(R.id.enter_application_password_field);
        next = (TextView) findViewById(R.id.enter_application_password_next_link);
    }


    private void setLanguage() {
        settingsSetPasswordAppTextView.setText(R.string.enter_password);
        next.setText(R.string.next);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String one = getPassword(password.getText().toString());
                        String two = getEncryptedPassword();
                        v.startAnimation(animTranslate);

                        if (one.equals(two)) {
                            MainActivity.START_FLAG = false;
                            if (!preferences.contains(StringEnum.APP_PREFERENCES_SEED.getValue())) {
                                // если нет ни какого кошелька
                                goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
                            } else {
                                closeThisPage();
                            }
                        } else {
                            makeToast(StringEnum.PASSWORD_DOES_NOT_MATCH.getValue());
                        }
                    }
                }
        );
    }


    @SuppressLint("HardwareIds")
    private String getPassword(String password) {
        return Cipher.encryptStringIrreversibly(password,
                preferences.getString(StringEnum.APP_PREFERENCES_SALT.getValue(), ""),
                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
    }


    private String getEncryptedPassword() {
        return preferences.getString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "");
    }


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        // оставляем пустым чтобы не работал возврат обратно
        // и не попадали на главную страницу кошелька
    }


    private void makeToast(String massage) {
        Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0,110);   // import android.view.Gravity;
        toast.show();
    }


    // для закрытие этой активити и попадания на главную активити
    public void closeThisPage() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
