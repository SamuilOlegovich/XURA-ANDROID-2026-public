package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.graphics.LinearGradient;
import android.graphics.Shader;
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

    private TextView winPageTextViewTree;
    private TextView winPageTextViewTwo;
    private TextView winPageTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.win_page);
        setButtons();
        setLanguage();
        goText();
    }



    private void setButtons() {
        winPageTextViewTree = (TextView) findViewById(R.id.win_page_text_view_tree);
        winPageTextViewTwo = (TextView) findViewById(R.id.win_page_text_view_tow);
        winPageTextView = (TextView) findViewById(R.id.win_page_text_view);

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
    }


    private void setLanguage() {
        winPageTextViewTree.setText(R.string.congratulations);
        winPageTextView.setText(R.string.bet_won);
//        MASSAGE = getString(R.string.);
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
