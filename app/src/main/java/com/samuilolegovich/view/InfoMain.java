package com.samuilolegovich.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.R;



public class InfoMain extends AppCompatActivity {
    public static final String INFO_MAIN_CLASS = ".InfoMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_main);
    }

    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
