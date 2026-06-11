package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;
import androidx.core.content.ContextCompat;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.runnable.FlasherRun;
import com.samuilolegovich.asyncAndRun.runnable.NotifierRunForTrialGame;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.Lotto;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class Flasher extends BaseActivity {
    public static final String FLASHER_CLASS = ".Flasher";

    public static volatile boolean VISIBLE_ON_SCREEN = false;

    public static volatile TestModeEnum TEST_MODE_ENUM;
    @SuppressLint("StaticFieldLeak")
    public static volatile Flasher FLASHER;

    public static String TEST_SAND_AMOUNT;
    public static String NUMBER_BET;

    public static Boolean COLOR_BET;

    private volatile boolean FLAG;

    private MediaPlayer rouletteSpinMediaPlayer;
    private MediaPlayer winMediaPlayer;
    private MediaPlayer lostMediaPlayer;

    private RouletteWheelView wheelView;
    private TextView numberInfo;
    private TextView infoThree;
    private TextView infoTwo;
    private TextView winInfo;

    private String CONGRATULATIONS;
    private String GOOD_LUCK;
    private String BET_LOST;
    private String BET_WON;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flasher);
        FLASHER = this;
        FLAG = true;
        setButtons();
        setLanguage();
        setSound();
        goThread();

        if (!MainActivity.IS_REAL_GAME_MODE) {
            goThreadTest();
        }
    }



    private void setSound() {
        rouletteSpinMediaPlayer = MediaPlayer.create(this, R.raw.roulette_spin);
        winMediaPlayer = MediaPlayer.create(this, R.raw.win);
        lostMediaPlayer = MediaPlayer.create(this, R.raw.lost);

        rouletteSpinMediaPlayer.setLooping(true);
        rouletteSpinMediaPlayer.start();
        wheelView.setCenterColor(0xFF000000);
        wheelView.startSpinning();
    }


    private void setButtons() {
        wheelView  = (RouletteWheelView) findViewById(R.id.roulette_wheel);
        infoThree  = (TextView) findViewById(R.id.last_text_view_tree);
        numberInfo = (TextView) findViewById(R.id.number_info_text);
        infoTwo    = (TextView) findViewById(R.id.last_text_view_two);
        winInfo    = (TextView) findViewById(R.id.last_text_view);
    }


    private void setLanguage() {
        CONGRATULATIONS = getString(R.string.congratulations);
        GOOD_LUCK = getString(R.string.good_luck);
        BET_LOST = getString(R.string.bet_lost);
        BET_WON = getString(R.string.bet_won);
    }


    // Called from FlasherRun every 300 ms — wheel handles the visual, nothing to do here.
    public void setColorAndText(String text, boolean b) { }


    public void stopGame(String text, boolean win) {
        runOnUiThread(() -> {
            FLAG = false;
            FlasherRun.FLAG = false;
            gameStop(text, win);
        });
    }


    @SuppressLint("SetTextI18n")
    private void gameStop(String text, boolean win) {
        rouletteSpinMediaPlayer.stop();

        int displayNumber = resolveDisplayNumber(win);

        wheelView.stopAtNumber(displayNumber, () -> {
            wheelView.setCenterColor(sectorColor(displayNumber));
            numberInfo.setText(win ? NUMBER_BET : String.valueOf(displayNumber));
            numberInfo.setTextColor(0xFFFFB000); // gold
            numberInfo.setVisibility(View.VISIBLE);

            winInfo.setText(win ? BET_WON : BET_LOST);
            winInfo.setVisibility(View.VISIBLE);
            winInfo.post(() -> applyTextGradient(winInfo, win));

            infoThree.setText(win ? CONGRATULATIONS : GOOD_LUCK);
            infoThree.setVisibility(View.VISIBLE);

            infoTwo.setText(text);
            infoTwo.setSelected(true);
            infoTwo.setVisibility(View.VISIBLE);

            if (win) {
                winMediaPlayer.start();
            } else {
                lostMediaPlayer.start();
            }
        });
    }


    private void applyTextGradient(TextView tv, boolean win) {
        float w = tv.getWidth();
        if (w == 0) return;
        int[] colors = win
                ? new int[]{0xFFFFE040, 0xFFFFB000, 0xFFFF6A00, 0xFFFFB000, 0xFFFFE040}
                : new int[]{0xFFFF3060, 0xFF990022, 0xFF666688, 0xFF333355};
        tv.getPaint().setShader(
                new LinearGradient(0, 0, w, 0, colors, null, Shader.TileMode.CLAMP));
        tv.invalidate();
    }


    private int sectorColor(int n) {
        if (n == 0)                                          return 0xFF007040; // green
        if (Lotto.learnTheColorOfNumber(String.valueOf(n))) return 0xFF111111; // black
        return 0xFFC81030;                                                      // red
    }


    private int resolveDisplayNumber(boolean win) {
        if (win) {
            if (NUMBER_BET.equals("00") || NUMBER_BET.equals("0")) return 0;
            return Integer.parseInt(NUMBER_BET);
        }
        if (NUMBER_BET.equals("00") || NUMBER_BET.equals("0")) {
            int i = Lotto.getRandomNumberForColor(true);
            return i == 0 ? i + 1 : i;
        } else if (COLOR_BET) {
            // bet was red — wheel shows a black number
            int i = Lotto.getRandomNumberForColor(false);
            return i == 0 ? i + 1 : i;
        } else {
            // bet was black — wheel shows a red number
            int i = Lotto.getRandomNumberForColor(true);
            return i == 0 ? i + 2 : i;
        }
    }


    private void setColorNavigation(int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            switch (color) {
                case 0:
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.yellow_6));
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.yellow_6));
                    break;
                case 1:
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
                    break;
            }
        }
    }


    private void goThread() {
        AppExecutors.io().execute(new FlasherRun());
    }


    private void goThreadTest() {
        AppExecutors.io().execute(new NotifierRunForTrialGame(TEST_MODE_ENUM));
    }


    @Override
    protected void onPause() {
        super.onPause();
        VISIBLE_ON_SCREEN = false;
        FlasherRun.FLAG = false;
        if (wheelView != null) wheelView.stopSpinning();
    }


    @Override
    protected void onResume() {
        super.onResume();
        VISIBLE_ON_SCREEN = true;
        if (FLAG) {
            FlasherRun.FLAG = true;
            goThread();
            if (wheelView != null) wheelView.startSpinning();
        }
    }


    @Override
    public void onBackPressed() {
        if (wheelView != null) wheelView.stopSpinning();
        rouletteSpinMediaPlayer.stop();
        lostMediaPlayer.stop();
        winMediaPlayer.stop();
        setColorNavigation(0);
        FlasherRun.FLAG = false;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FLASHER = null;
    }
}