package com.samuilolegovich.view;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;



public class InfoReferral extends AppCompatActivity {
    public static final String INFO_REFERRAL_CLASS = ".InfoReferral";

    private TextView outInfo;
    private EditText bet;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.info_referral);
//        setButtons();
        setLanguage();
    }



    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
