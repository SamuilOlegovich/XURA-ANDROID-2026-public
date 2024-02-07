package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;



public class Win extends AppCompatActivity {
    public static final String WIN_CLASS = ".Win";
    public static String MASSAGE = "";

    private MediaPlayer winMediaPlayer;
    private TextView info3;
    private TextView info2;
    private TextView info;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.win);
        setButtons();
        setLanguage();
        goText();
    }



    private void setButtons() {
        winMediaPlayer = MediaPlayer.create(this, R.raw.win);
        info3 = (TextView) findViewById(R.id.last_text_view_tree);
        info2 = (TextView) findViewById(R.id.last_text_view_two);
        info = (TextView) findViewById(R.id.last_text_view);

        winMediaPlayer.start();
    }



    // настройка для бегущей строки
    @SuppressLint("SetTextI18n")
    private void goText() {
        info2.setText(MASSAGE);
        info2.setSelected(true);
    }



    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        winMediaPlayer.stop();
        super.onBackPressed();
    }
}
