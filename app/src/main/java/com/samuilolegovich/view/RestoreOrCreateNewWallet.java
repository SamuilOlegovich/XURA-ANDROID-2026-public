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




/**
 * Экран первого выбора для пользователя без кошелька: создать новый XRPL-кошелёк
 * или восстановить существующий по seed-фразе. Кнопка "назад" заблокирована —
 * пользователь обязан сделать выбор, чтобы продолжить онбординг.
 */
@AndroidEntryPoint
public class RestoreOrCreateNewWallet extends BaseActivity {
    public static final String RESTORE_OR_NEW_WALLET_CLASS = ".RestoreOrCreateNewWallet";

    private View createNewWallet;
    private View restoreWallet;



    /** Инициализирует экран: разметка, View, слушатели кнопок выбора. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore_or_create_new_wallet_page);
        setButtons();
        listeners();
    }



    /** Находит и сохраняет ссылки на View разметки экрана. */
    private void setButtons() {
        createNewWallet = findViewById(R.id.create_new_wallet_linc);
        restoreWallet = findViewById(R.id.restore_wallet_linc);
    }


    /** Назначает обработчики кнопок перехода к созданию нового кошелька или к восстановлению существующего. */
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


    /** Запускает Activity по имени её класса/действия. */
    private void goToAnotherPage(String namePage) {
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    /** Намеренно пустая реализация — блокирует жест/кнопку "назад", чтобы пользователь не пропустил обязательный выбор кошелька. */
    @Override
    public void onBackPressed() {
    }
}
