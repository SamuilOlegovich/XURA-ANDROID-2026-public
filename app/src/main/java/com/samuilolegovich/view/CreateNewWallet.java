package com.samuilolegovich.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.SecureSeedStorage;
import com.samuilolegovich.wallet.repository.WalletRepository;

import java.util.Map;

import javax.inject.Inject;

import static com.samuilolegovich.view.CheckingNewWallet.CHECKING_NEW_WALLET_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




/**
 * Экран создания нового кошелька: генерирует новый XRPL-аккаунт (адрес+seed) через репозиторий,
 * показывает сгенерированную seed-фразу пользователю и временно сохраняет её как pre-seed,
 * чтобы на следующем экране ({@link CheckingNewWallet}) проверить, что пользователь её правильно запомнил/записал.
 */
@AndroidEntryPoint
public class CreateNewWallet extends BaseActivity {

    @Inject WalletRepository repository;
    public static final String CREATE_NEW_WALLET_CLASS = ".CreateNewWallet";

    private volatile boolean isNewWallet = false;
    private String seedString;

    private TextView createNewWalletText;
    private TextView seed;
    private View next;



    /** Инициализирует экран: включает FLAG_SECURE, разметку, View, локализацию, слушатели и сразу запускает генерацию кошелька. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.create_new_wallet);
        setButtons();
        setLanguage();
        listeners();
        createNewWalletAsync();
    }



    /** Находит и сохраняет ссылки на View разметки экрана. */
    private void setButtons() {
        createNewWalletText = (TextView) findViewById(R.id.create_new_wallet_text_view);
        seed = (TextView) findViewById(R.id.seed_field);
        next = findViewById(R.id.next_link);
    }


    /** Показывает текст-заглушку "генерация seed..." и блокирует кнопку "далее" до завершения генерации кошелька. */
    private void setLanguage() {
        seed.setText(R.string.seed_generating);
        next.setAlpha(0.4f);
        next.setClickable(false);
        next.setFocusable(false);
    }


    /** Назначает обработчик кнопки "далее": переход к проверке seed при успешной генерации, либо повторная попытка генерации при неудаче. */
    private void listeners() {
        next.setOnClickListener(v -> {
            pulse(v);
            if (isNewWallet) {
                MainActivity.START_FLAG = false;
                repository.loadBalance();
                goToAnotherPage(CHECKING_NEW_WALLET_CLASS);
            } else {
                createNewWalletAsync();
            }
        });
    }


    /** Асинхронно (на IO-потоке) генерирует новый XRPL-кошелёк, показывает seed на экране и временно сохраняет его как pre-seed; при ошибке предлагает перезапустить. */
    private void createNewWalletAsync() {
        isNewWallet = false;
        AppExecutors.io().execute(() -> {
            Map<String, String> map = repository.createNewWallet();
            runOnUiThread(() -> {
                if (map != null && map.containsKey("Seed")) {
                    seedString = map.get("Seed");
                    seed.setText(seedString);
                    setPreSeed(seedString);
                    isNewWallet = true;
                    next.setAlpha(1f);
                    next.setClickable(true);
                    next.setFocusable(true);
                } else {
                    seed.setText(R.string.wrong_restart_please);
                    next.setAlpha(1f);
                    next.setClickable(true);
                    next.setFocusable(true);
                }
            });
        });
    }


    /** Временно сохраняет сгенерированную seed-фразу в защищённом хранилище, чтобы её можно было сверить на следующем экране. */
    private void setPreSeed(String newSeed) {
        SecureSeedStorage.save(PrefsHelper.get(this), StringEnum.APP_PREFERENCES_PRE_SEED.getValue(), newSeed);
    }


    /** Запускает Activity по имени её класса/действия. */
    private void goToAnotherPage(String namePage) {
        startActivity(new Intent(namePage));
    }


    /** Стандартная обработка нажатия "назад" без дополнительной логики. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
