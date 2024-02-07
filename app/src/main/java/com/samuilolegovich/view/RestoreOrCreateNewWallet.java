package com.samuilolegovich.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;

import static com.samuilolegovich.view.CreateNewWallet.CREATE_NEW_WALLET_CLASS;
import static com.samuilolegovich.view.RestoreWallet.RESTORE_WALLET_CLASS;



// тут определяем что нам надо востановить или сгенирировать новый кошелек
public class RestoreOrCreateNewWallet extends AppCompatActivity {
    public static final String RESTORE_OR_NEW_WALLET_CLASS = ".RestoreOrCreateNewWallet";

    private TextView createNewWallet;
    private Animation animTranslate;
    private TextView restoreWallet;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.restore_or_create_new_wallet_page);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        createNewWallet = (TextView) findViewById(R.id.create_new_wallet_linc);
        restoreWallet = (TextView) findViewById(R.id.restore_wallet_linc);
    }


    private void setLanguage() {
        createNewWallet.setText(R.string.create_new_wallet);
        restoreWallet.setText(R.string.restore_wallet);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        createNewWallet.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(CREATE_NEW_WALLET_CLASS);
                    }
                }
        );

        restoreWallet.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(RESTORE_WALLET_CLASS);
                    }
                }
        );
    }


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
    }
}
