package com.samuilolegovich.view;

import static com.samuilolegovich.view.BecomeReferral.BECOME_REFERRAL_CLASS;
import static com.samuilolegovich.view.GuessTheNumberGame.GUESS_THE_NUMBER_GAME_CLASS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class SelectGameMode  extends BaseActivity {
    public static final String SELECT_GAME_MODE_CLASS = ".SelectGameMode";

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private Animation animTranslate;

    private TextView selectGameModePageTextView;
    private TextView selectGameModeLinc;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_game_mode_page);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        selectGameModePageTextView = (TextView) findViewById(R.id.select_game_mode_page_text_view);
        selectGameModeLinc = (TextView) findViewById(R.id.select_game_mode_page_mode_linc);

    }


    private void setLanguage() {
        selectGameModePageTextView.setText(R.string.select_game_mode_text);
        selectGameModeLinc.setText(MainActivity.IS_REAL_GAME_MODE
                ? R.string.select_game_mode_text_test
                : R.string.select_game_mode_text_real);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        selectGameModeLinc.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        boolean isGameMode = MainActivity.IS_REAL_GAME_MODE.toString().equalsIgnoreCase("true");
                        makeStackThread(isGameMode
                                ? StringEnum.APP_GAME_MODE_TEST
                                : StringEnum.APP_GAME_MODE_REAL);

                        selectGameModeLinc.setText(isGameMode
                                ? R.string.select_game_mode_text_real
                                : R.string.select_game_mode_text_test);
                    }
                }
        );
    }


    private void makeStackThread(StringEnum stringEnum) {
        AppExecutors.io().execute(() -> makeStack(stringEnum));
    }


    private void makeStack(StringEnum stringEnum) {
        preferences = PrefsHelper.get(this);

        editor = preferences.edit();
        editor.putString(StringEnum.APP_GAME_MODE.getValue(), stringEnum.getValue());
        editor.apply();

        MainActivity.IS_REAL_GAME_MODE = stringEnum.getValue().equalsIgnoreCase("true");
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
