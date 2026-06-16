package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;

import android.view.View;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class Win extends BaseActivity {
    public static final String WIN_CLASS = ".Win";
    public static String MASSAGE = "";

    private MediaPlayer winMediaPlayer;

    private TextView winPageTextViewTree;
    private TextView winPageTextViewTwo;
    private TextView winPageTextView;
    private View btnPlayAgain;
    private View btnBackToGames;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.win_page);
        setButtons();
        setLanguage();
        goText();
    }



    private void setButtons() {
        winPageTextViewTree = (TextView) findViewById(R.id.win_page_text_view_tree);
        winPageTextViewTwo = (TextView) findViewById(R.id.win_page_text_view_tow);
        winPageTextView = (TextView) findViewById(R.id.win_page_text_view);
        btnPlayAgain = findViewById(R.id.btn_play_again);
        btnBackToGames = findViewById(R.id.btn_back_to_games);

        winMediaPlayer = MediaPlayer.create(this, R.raw.win);
        winMediaPlayer.start();

        winPageTextView.post(() -> {
            LinearGradient gradient = new LinearGradient(
                0f, 0f, winPageTextView.getWidth(), 0f,
                new int[]{0xFF00D4FF, 0xFF4040F0, 0xFF9020D0, 0xFFD02090, 0xFFFFB000},
                new float[]{0f, 0.25f, 0.5f, 0.75f, 1f},
                Shader.TileMode.CLAMP
            );
            winPageTextView.getPaint().setShader(gradient);
            winPageTextView.invalidate();
        });

        btnPlayAgain.setOnClickListener(v -> {
            winMediaPlayer.stop();
            finish();
        });

        btnBackToGames.setOnClickListener(v -> {
            winMediaPlayer.stop();
            Intent intent = new Intent(SelectGame.SELECT_GAME_CLASS);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }


    private void setLanguage() {
        winPageTextViewTree.setText(R.string.congratulations);
        winPageTextView.setText(R.string.bet_won);
    }



    // настройка для бегущей строки
    @SuppressLint("SetTextI18n")
    private void goText() {
        winPageTextViewTwo.setText(MASSAGE);
        winPageTextViewTwo.setSelected(true);
    }



    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        winMediaPlayer.stop();
        super.onBackPressed();
    }
}
