package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.R;
import dagger.hilt.android.AndroidEntryPoint;



@AndroidEntryPoint
public class Lost extends BaseActivity {
    public static final String LOST_CLASS = ".Lost";
    public static String MASSAGE = "";

    private MediaPlayer lostMediaPlayer;

    private TextView lastTextViewTwo;
    private TextView lastTextView;
    private View btnPlayAgain;
    private View btnBackToGames;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lost);
        setButtons();
        setLanguage();
        goText();
    }



    private void setButtons() {
        lastTextViewTwo = (TextView) findViewById(R.id.last_text_view_two);
        lastTextView    = (TextView) findViewById(R.id.last_text_view);
        btnPlayAgain    = findViewById(R.id.btn_play_again);
        btnBackToGames  = findViewById(R.id.btn_back_to_games);

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

        btnPlayAgain.setOnClickListener(v -> {
            lostMediaPlayer.stop();
            finish();
        });

        btnBackToGames.setOnClickListener(v -> {
            lostMediaPlayer.stop();
            Intent intent = new Intent(SelectGame.SELECT_GAME_CLASS);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }


    private void setLanguage() {
        lastTextView.setText(R.string.bet_lost);
    }


    @SuppressLint("SetTextI18n")
    private void goText() {
        lastTextViewTwo.setText(MASSAGE);
    }


    @Override
    public void onBackPressed() {
        lostMediaPlayer.stop();
        super.onBackPressed();
    }
}
