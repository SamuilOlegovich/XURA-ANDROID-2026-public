package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.R;

public class Lost extends AppCompatActivity {
    public static final String LOST_CLASS = ".Lost";
    public static String MASSAGE = "";

    private MediaPlayer lostMediaPlayer;
    private TextView textInfo;
    private TextView text;
    private TextView info;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lost);
        setButtons();
        goText();
    }



    private void setButtons() {
        lostMediaPlayer = MediaPlayer.create(this, R.raw.lost);
        textInfo = (TextView) findViewById(R.id.info2);
        text = (TextView) findViewById(R.id.info3);
        info = (TextView) findViewById(R.id.info_link);

        lostMediaPlayer.start();
    }



    // настройка для бегущей строки
    @SuppressLint("SetTextI18n")
    private void goText() {
        textInfo.setText("DON'T WORRY - PLAY FURTHER AND YOU WILL BE LUCKY");
        textInfo.setSelected(true);
    }



    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        lostMediaPlayer.stop();
        super.onBackPressed();
    }
}
