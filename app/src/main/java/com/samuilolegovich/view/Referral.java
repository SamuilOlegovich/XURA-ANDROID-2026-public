package com.samuilolegovich.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class Referral extends BaseActivity {
    public static final String REFERRAL_CLASS = ".Referral";
    public static Boolean FLAG = true;

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private EditText enterReferralCode;
    private TextView referralTextView;
    private TextView skip;
    private TextView set;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.referral);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        referralTextView = (TextView) findViewById(R.id.referral_text_view);
        enterReferralCode = (EditText) findViewById(R.id.referral_code_field);
        skip = (TextView) findViewById(R.id.referral_skip_linc);
        set = (TextView) findViewById(R.id.referral_set_linc);
    }


    private void setLanguage() {
        referralTextView.setText(R.string.referral_text);
        skip.setText(R.string.skip);
        set.setText(R.string.set);
    }


    private void listeners() {
        set.setOnClickListener(v -> {
            pulse(v);
            String code = enterReferralCode.getText().toString();

            if (code.length() > 0
                    && Long.parseLong(code) < Long.parseLong(StringEnum.MAX_REFERRALS.getValue())) {
                setReferralForApp(code);
                if (FLAG) {
                    closeThisPage();
                } else {
                    onBackPressed();
                }
            } else {
                enterReferralCode.setText("");
                makeToast(StringEnum.REFERRAL_DOES_NOT_MATCH.getValue());
            }
        });

        skip.setOnClickListener(v -> {
            pulse(v);
            setReferralForApp(StringEnum.APP_PREFERENCES_REFERRAL_NOT_INSTALLED.getValue());
            if (FLAG) {
                closeThisPage();
            } else {
                onBackPressed();
            }
        });
    }


    private void setReferralForApp(String referral) {
        preferences = PrefsHelper.get(this);
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
