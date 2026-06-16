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




/**
 * Экран поздравления после удачной ставки: проигрывает звук победы, показывает
 * текст с градиентной заливкой и бегущей строкой, предлагает сыграть снова
 * или вернуться к выбору игры.
 */
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



    /** Инициализирует экран: разметка, View и звук, локализация, текст бегущей строки. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.win_page);
        setButtons();
        setLanguage();
        goText();
    }



    /** Находит View разметки, запускает звук победы, накладывает градиентную заливку на основной текст и назначает слушатели кнопок "сыграть снова"/"вернуться к играм". */
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


    /** Устанавливает локализованный текст поздравления. */
    private void setLanguage() {
        winPageTextViewTree.setText(R.string.congratulations);
        winPageTextView.setText(R.string.bet_won);
    }



    /** Настройка для бегущей строки: подставляет сообщение о выигрыше из статического поля MASSAGE и включает marquee-прокрутку. */
    @SuppressLint("SetTextI18n")
    private void goText() {
        winPageTextViewTwo.setText(MASSAGE);
        winPageTextViewTwo.setSelected(true);
    }



    /** При нажатии на кнопку "назад" останавливает звук победы и возвращается на предыдущий экран. */
    @Override
    public void onBackPressed() {
        winMediaPlayer.stop();
        super.onBackPressed();
    }
}
