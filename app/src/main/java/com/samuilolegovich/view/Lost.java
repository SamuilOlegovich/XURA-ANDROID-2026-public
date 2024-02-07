package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;



public class Lost extends AppCompatActivity {
    public static final String LOST_CLASS = ".Lost";
    public static String MASSAGE = "";

    private MediaPlayer lostMediaPlayer;
    private TextView lastTextViewTree;
    private TextView lastTextViewTow;
    private TextView lastTextView;

    private String TICKER;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.lost);
        setButtons();
        setLanguage();
        goText();
    }



    private void setButtons() {
        lastTextViewTree = (TextView) findViewById(R.id.last_text_view_tree);
        lastTextViewTow = (TextView) findViewById(R.id.last_text_view_two);
        lastTextView = (TextView) findViewById(R.id.last_text_view);

        lostMediaPlayer = MediaPlayer.create(this, R.raw.lost);
        lostMediaPlayer.start();
    }


    private void setLanguage() {
        lastTextViewTree.setText(R.string.good_luck);
        lastTextView.setText(R.string.bet_lost);

        TICKER = getString(R.string.last_text_two);
    }


    // настройка для бегущей строки
    @SuppressLint("SetTextI18n")
    private void goText() {
        lastTextViewTow.setText(TICKER);
        lastTextViewTow.setSelected(true);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        lostMediaPlayer.stop();
        super.onBackPressed();
    }
}
