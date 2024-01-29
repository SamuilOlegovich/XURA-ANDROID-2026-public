package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.runnable.FlasherRun;
import com.samuilolegovich.utils.Lotto;



public class Flasher extends AppCompatActivity {
    public static final String FLASHER_CLASS = ".Flasher";

    public static volatile boolean VISIBLE_ON_SCREEN = false;
    @SuppressLint("StaticFieldLeak")
    public static volatile Flasher FLASHER;
    private volatile boolean FLAG;

    public static Boolean COLOR_BET;
    public static String NUMBER_BET;

    private MediaPlayer rouletteSpinMediaPlayer;
    private MediaPlayer winMediaPlayer;
    private MediaPlayer lostMediaPlayer;

    private ConstraintLayout constraintLayout;
    private TextView numberInfo;
    private TextView infoThree;
    private TextView infoTwo;
    private TextView winInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flasher);
        FLASHER = this;
        FLAG = true;
        setButtons();
        setSound();
        goThread();
    }


    private void soundPlay(MediaPlayer mediaPlayer) {
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }


    private void setSound() {
        rouletteSpinMediaPlayer = MediaPlayer.create(this, R.raw.roulette_spin);
        winMediaPlayer = MediaPlayer.create(this, R.raw.win);
        lostMediaPlayer = MediaPlayer.create(this, R.raw.lost);

        soundPlay(rouletteSpinMediaPlayer);
    }



    private void setButtons() {
        constraintLayout = (ConstraintLayout) findViewById(R.id.flasher);
        numberInfo = (TextView) findViewById(R.id.number_info);
        infoThree = (TextView) findViewById(R.id.info3);
        infoTwo = (TextView) findViewById(R.id.info2);
        winInfo = (TextView) findViewById(R.id.info_link);
    }



    public void setColorAndText(String text, boolean b) {
        // goToAnotherPage
        new Thread() {
            public void run() {
                FLASHER.runOnUiThread(new Runnable() {
                    @SuppressLint("ResourceAsColor")
                    public void run() {
                        if (FLAG) {
                            gameRun(text, b);
                        }
                    }
                });
            }
        }.start();
    }



    public void stopGame(String text, boolean win) {
        // goToAnotherPage
        new Thread() {
            public void run() {
                FLASHER.runOnUiThread(new Runnable() {
                    @SuppressLint("ResourceAsColor")
                    public void run() {
                        FLAG =false;
                        FlasherRun.FLAG = false;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        gameStop(text, win);
                    }
                });
            }
        }.start();
    }



    private void gameRun(String text, boolean b) {
        if (b) {
            numberInfo.setText(text);
            numberInfo.setTextColor(Color.BLACK);
            constraintLayout.setBackgroundColor(Color.RED);
            setColorNavigation(2);
        } else if (text.equals("00") || text.equals("0")) {
            numberInfo.setText(text);
            numberInfo.setTextColor(Color.parseColor("#007143"));
            constraintLayout.setBackgroundColor(Color.BLACK);
            setColorNavigation(1);
        } else {
            numberInfo.setText(text);
            numberInfo.setTextColor(Color.RED);
            constraintLayout.setBackgroundColor(Color.BLACK);
            setColorNavigation(1);
        }
    }



    @SuppressLint("SetTextI18n")
    private void gameStop(String text, boolean win) {
        if (win) {
            // в случаи выигрыша ставки
            if (NUMBER_BET.equals("00") || NUMBER_BET.equals("0")) {
                numberInfo.setTextColor(Color.GREEN);
                constraintLayout.setBackgroundColor(Color.BLACK);
                setColorNavigation(1);
            } else if (COLOR_BET) {
                numberInfo.setTextColor(Color.BLACK);
                constraintLayout.setBackgroundColor(Color.RED);
                setColorNavigation(2);
            } else {
                numberInfo.setTextColor(Color.RED);
                constraintLayout.setBackgroundColor(Color.BLACK);
                setColorNavigation(1);
            }

            rouletteSpinMediaPlayer.stop();
            winMediaPlayer.start();

            infoThree.setText("CONGRATULATIONS");
            numberInfo.setText(NUMBER_BET + "");
            winInfo.setText("BET WON");
            infoTwo.setText(text);
            infoTwo.setSelected(true);

        } else {
            // в случаи проигрыша ставки
            if (NUMBER_BET.equals("00") || NUMBER_BET.equals("0")) {
                int i = Lotto.getRandomNumberForColor(true);
                int b = i == 0 ? i + 1 : i;
                boolean col = Lotto.learnTheColorOfNumber(b + "");
                numberInfo.setText(b + "");
                numberInfo.setTextColor(col ? Color.BLACK : Color.RED);
                constraintLayout.setBackgroundColor(col ? Color.RED : Color.BLACK);
                setColorNavigation(col ? 2 : 1);
            } else if (COLOR_BET) {
                int i = Lotto.getRandomNumberForColor(false);
                numberInfo.setText((i == 0 ? i + 1 : i) + "");
                numberInfo.setTextColor(Color.RED);
                constraintLayout.setBackgroundColor(Color.BLACK);
                setColorNavigation(1);
            } else {
                int i = Lotto.getRandomNumberForColor(true);
                numberInfo.setText((i == 0 ? i + 2 : i) + "");
                numberInfo.setTextColor(Color.BLACK);
                constraintLayout.setBackgroundColor(Color.RED);
                setColorNavigation(2);
            }

            rouletteSpinMediaPlayer.stop();
            lostMediaPlayer.start();

            infoThree.setText("GOOD LUCK");
            winInfo.setText("BET LOST");
            infoTwo.setText(text);
            infoTwo.setSelected(true);
        }
    }



    private void setColorNavigation(int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            switch (color) {
                case 0 :
                    // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.yellow_6));
                    //status bar or the time bar at the top
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.yellow_6));
                    break;
                case 1 :
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
                    break;
                case 2 :
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.rad_1));
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.rad_1));
                    break;
            }
        }
    }



    private void goThread() {
        Runnable runnable = new FlasherRun();
        Thread thread = new Thread(runnable);
        thread.start();
    }



    @Override
    protected void onPause() {
        super.onPause();
        VISIBLE_ON_SCREEN = false;
        FlasherRun.FLAG =  false;
    }



    @Override
    protected void onResume() {
        super.onResume();
        FLAG = true;
        VISIBLE_ON_SCREEN = true;
        FlasherRun.FLAG =  true;
        goThread();
    }



    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        numberInfo.setText("");
        infoThree.setText("");
        infoTwo.setText("");
        winInfo.setText("");
        rouletteSpinMediaPlayer.stop();
        lostMediaPlayer.stop();
        winMediaPlayer.stop();
        setColorNavigation(0);
        FlasherRun.FLAG = false;
        super.onBackPressed();
    }
}
