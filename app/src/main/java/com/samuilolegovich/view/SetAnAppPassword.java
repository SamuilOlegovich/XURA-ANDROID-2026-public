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

import java.util.UUID;

import static com.samuilolegovich.view.RestoreOrCreateNewWallet.RESTORE_OR_NEW_WALLET_CLASS;



// тут устанавливаем пароль на приложение
public class SetAnAppPassword extends AppCompatActivity {
    public static final String SET_AN_APP_PASSWORD_CLASS = ".SetAnAppPassword";

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private Animation animTranslate;

    private EditText passwordOne;
    private EditText passwordTwo;

    private TextView confirm;
    private TextView skip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.set_password_for_app);
        setButtons();
        setLanguage();
        listeners();
    }

    private void setButtons() {
        passwordTwo = (EditText) findViewById(R.id.edit_text_passport_tow);
        passwordOne = (EditText) findViewById(R.id.password_field);
        confirm = (TextView) findViewById(R.id.next_link);
        skip = (TextView) findViewById(R.id.link_footer_info);
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
                            goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
                        } else {
                            passwordOne.setText("");
                            passwordTwo.setText("");
                            makeToast(StringEnum.PASSWORD_DOES_NOT_MATCH.getValue());
                        }
                    }
                }
        );

        skip.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        setPasswordForApp(StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue(), false);
                        goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
                    }
                }
        );
    }

    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }

    private void makeToast(String massage) {
        Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0,110);   // import android.view.Gravity;
        toast.show();
    }

    private void setPasswordForApp(String password, boolean b) {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);

        @SuppressLint("HardwareIds")
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
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
        // оставляем пустым чтобы не работал возврат обратно
        // и не попадали на главную страницу кошелька
    }
}
