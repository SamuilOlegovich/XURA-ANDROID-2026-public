package com.samuilolegovich.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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



public class Referral extends AppCompatActivity {
    public static final String REFERRAL_CLASS = ".Referral";
    public static Boolean FLAG = true;

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private Animation animTranslate;

    private EditText referralCode;
    private TextView next;
    private TextView skip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.referral);
        setLanguage();
        setButtons();
        listeners();
    }

    private void setButtons() {
        referralCode = (EditText) findViewById(R.id.referral_code);
        next = (TextView) findViewById(R.id.next);
        skip = (TextView) findViewById(R.id.skip);
    }

    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        String code = referralCode.getText().toString();

                        if (code.length() > 0
                                && Long.parseLong(code) < Long.parseLong(StringEnum.MAX_REFERRALS.getValue())) {
                            setReferralForApp(code);
                            if (FLAG) {
                                closeThisPage();
                            } else {
                                onBackPressed();
                            }
                        } else {
                            referralCode.setText("");
                            makeToast(StringEnum.REFERRAL_DOES_NOT_MATCH.getValue());
                        }
                    }
                }
        );

        skip.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        setReferralForApp(StringEnum.APP_PREFERENCES_REFERRAL_NOT_INSTALLED.getValue());
                        if (FLAG) {
                            closeThisPage();
                        } else {
                            onBackPressed();
                        }
                    }
                }
        );
    }

    private void setReferralForApp(String referral) {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), referral);
        editor.apply();
    }

    private void makeToast(String massage) {
        Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0,110);   // import android.view.Gravity;
        toast.show();
    }

    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    // для закрытие этой активити и попадания на главную активити
    public void closeThisPage() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
