package com.samuilolegovich.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Cipher;
import com.samuilolegovich.utils.PrefsHelper;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class SettingsSetPasswordForApp extends BaseActivity {
    public static final String SETTINGS_SET_PASSWORD_FOR_APP_CLASS = ".SettingsSetPasswordForApp";


    private Animation animTranslate;

    private EditText passwordOne;
    private EditText passwordTwo;
    private TextView textView;
    private TextView confirm;



    @Override
    protected boolean isLockExempt() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.settings_set_password_for_app);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        textView = (TextView) findViewById(R.id.settings_set_password_app_text_view);
        passwordTwo = (EditText) findViewById(R.id.edit_text_passport_tow);
        passwordOne = (EditText) findViewById(R.id.password_field);
        confirm = (TextView) findViewById(R.id.confirm_link);
    }


    private void setLanguage() {
        textView.setText(R.string.set_password_to_enter_application);
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
        SharedPreferences.Editor edit = PrefsHelper.get(this).edit();
        if (b) {
            String salt = Cipher.generateSalt();
            edit.putString(StringEnum.APP_PREFERENCES_SALT.getValue(), salt);
            edit.putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(),
                    Cipher.hashPassword(password, salt));
        } else {
            edit.putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(),
                    StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue());
        }
        edit.apply();
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
