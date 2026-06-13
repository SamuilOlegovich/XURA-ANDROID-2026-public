package com.samuilolegovich.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;

import static com.samuilolegovich.view.CreateNewWallet.CREATE_NEW_WALLET_CLASS;
import static com.samuilolegovich.view.RestoreWallet.RESTORE_WALLET_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




// тут определяем что нам надо востановить или сгенирировать новый кошелек
@AndroidEntryPoint
public class RestoreOrCreateNewWallet extends BaseActivity {
    public static final String RESTORE_OR_NEW_WALLET_CLASS = ".RestoreOrCreateNewWallet";

    private View createNewWallet;
    private View restoreWallet;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore_or_create_new_wallet_page);
        setButtons();
        listeners();
    }



    private void setButtons() {
        createNewWallet = findViewById(R.id.create_new_wallet_linc);
        restoreWallet = findViewById(R.id.restore_wallet_linc);
    }


    private void listeners() {
        createNewWallet.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(CREATE_NEW_WALLET_CLASS);
        });

        restoreWallet.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(RESTORE_WALLET_CLASS);
        });
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
