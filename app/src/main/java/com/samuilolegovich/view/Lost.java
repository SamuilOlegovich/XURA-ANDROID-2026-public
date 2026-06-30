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
import com.samuilolegovich.utils.AudioHelper;
import dagger.hilt.android.AndroidEntryPoint;



/**
 * Экран проигрыша ставки: показывает сообщение о проигрыше с градиентной анимацией текста
 * и звуковым эффектом, предлагает сыграть снова или вернуться к выбору игры.
 */
@AndroidEntryPoint
public class Lost extends BaseActivity {
    public static final String LOST_CLASS = ".Lost";
    public static String MASSAGE = "";

    private MediaPlayer lostMediaPlayer;

    private TextView lastTextViewTwo;
    private TextView lastTextView;
    private View btnBackToGames;



    /** Инициализирует экран: разметка, View, локализация, отображение переданного сообщения о проигрыше. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lost);
        setButtons();
        setLanguage();
        goText();
    }



    /** Находит View разметки, запускает звук проигрыша, применяет градиент к заголовку и назначает обработчики кнопок. */
    private void setButtons() {
        lastTextViewTwo = (TextView) findViewById(R.id.last_text_view_two);
        lastTextView    = (TextView) findViewById(R.id.last_text_view);
        btnBackToGames  = findViewById(R.id.btn_back_to_games);

        lostMediaPlayer = MediaPlayer.create(this, R.raw.lost);
        if (AudioHelper.isSoundEnabled(this)) lostMediaPlayer.start();

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

        btnBackToGames.setOnClickListener(v -> {
            lostMediaPlayer.stop();
            Intent intent = new Intent(SelectGame.SELECT_GAME_CLASS);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }


    /** Устанавливает локализованный заголовок "ставка проиграна". */
    private void setLanguage() {
        lastTextView.setText(R.string.bet_lost);
    }


    /** Выводит переданное через статическое поле MASSAGE детальное сообщение о результате игры. */
    @SuppressLint("SetTextI18n")
    private void goText() {
        lastTextViewTwo.setText(MASSAGE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (lostMediaPlayer != null && lostMediaPlayer.isPlaying()) lostMediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lostMediaPlayer != null) { lostMediaPlayer.release(); lostMediaPlayer = null; }
    }

    /** Останавливает звук проигрыша перед стандартной обработкой нажатия "назад". */
    @Override
    public void onBackPressed() {
        if (lostMediaPlayer != null) lostMediaPlayer.stop();
        super.onBackPressed();
    }
}
