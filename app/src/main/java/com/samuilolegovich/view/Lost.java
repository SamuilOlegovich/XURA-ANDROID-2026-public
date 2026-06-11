package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;



public class Lost extends BaseActivity {
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

        // BET LOST — purple → magenta → red
        lastTextView.post(() -> {
            LinearGradient gradient = new LinearGradient(
                0f, 0f, lastTextView.getWidth(), 0f,
                new int[]{0xFF9020D0, 0xFFD02090, 0xFFFF2040},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
            );
            lastTextView.getPaint().setShader(gradient);
            lastTextView.invalidate();
        });

        // GOOD LUCK — cyan → purple → gold
        lastTextViewTree.post(() -> {
            LinearGradient gradient = new LinearGradient(
                0f, 0f, lastTextViewTree.getWidth(), 0f,
                new int[]{0xFF00D4FF, 0xFF4040F0, 0xFF9020D0, 0xFFD02090, 0xFFFFB000},
                new float[]{0f, 0.25f, 0.5f, 0.75f, 1f},
                Shader.TileMode.CLAMP
            );
            lastTextViewTree.getPaint().setShader(gradient);
            lastTextViewTree.invalidate();
        });
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
