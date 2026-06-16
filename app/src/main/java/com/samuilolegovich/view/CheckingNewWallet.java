package com.samuilolegovich.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.SecureSeedStorage;
import com.samuilolegovich.wallet.repository.WalletRepository;

import javax.inject.Inject;

import static com.samuilolegovich.view.Referral.REFERRAL_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




/**
 * Экран проверки нового кошелька: пользователь повторно вводит seed-фразу,
 * которую видел на предыдущем экране создания кошелька, и она сверяется
 * с сохранённым во временном хранилище pre-seed значением — так подтверждается,
 * что seed записан верно, после чего он сохраняется как постоянный seed кошелька.
 */
@AndroidEntryPoint
public class CheckingNewWallet extends BaseActivity {

    @Inject WalletRepository repository;
    public static final String CHECKING_NEW_WALLET_CLASS = ".CheckingNewWallet";

    private TextView checkingNewWalletText;
    private TextInputLayout tilSeed;
    private EditText seed;
    private View next;



    /** Инициализирует экран: включает FLAG_SECURE (запрет скриншотов/записи экрана с seed-фразой), разметку, View, локализацию, слушатели. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.checking_new_wallet);
        setButtons();
        setLanguage();
        listeners();
    }



    /** Находит и сохраняет ссылки на View разметки экрана. */
    private void setButtons() {
        checkingNewWalletText = (TextView) findViewById(R.id.checking_new_wallet_text);
        tilSeed = findViewById(R.id.til_password_field);
        seed = (EditText) findViewById(R.id.password_field);
        next = findViewById(R.id.next_link);
    }


    /** Устанавливает локализованный текст подсказки для поля ввода seed-фразы. */
    private void setLanguage() {
        checkingNewWalletText.setText(R.string.lead_the_seed);
    }


    /** Назначает обработчик кнопки "далее" (сверка введённого seed с pre-seed и сохранение/переход) и сброс ошибки при правке поля. */
    private void listeners() {
        next.setOnClickListener(v -> {
            pulse(v);
            String seedOne = getPreSeed();
            String seedTwo = seed.getText().toString();

            if (seedOne.equals(seedTwo)) {
                setSeed(seedOne);
                MainActivity.START_FLAG = false;
                repository.loadBalance();
                goToAnotherPage(REFERRAL_CLASS);
            } else {
                seed.setText("");
                tilSeed.setError(StringEnum.SEED_DOES_NOT_MATCH.getValue());
            }
        });

        seed.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { tilSeed.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }


    /** Запускает Activity по имени её класса/действия. */
    private void goToAnotherPage(String namePage) {
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    /** Читает временно сохранённый при создании кошелька pre-seed из защищённого хранилища, для сверки с тем, что ввёл пользователь. */
    private String getPreSeed() {
        return SecureSeedStorage.load(PrefsHelper.get(this), StringEnum.APP_PREFERENCES_PRE_SEED.getValue());
    }


    /** Сохраняет подтверждённую seed-фразу как постоянный seed кошелька в защищённом хранилище. */
    private void setSeed(String newSeed) {
        SecureSeedStorage.save(PrefsHelper.get(this), StringEnum.APP_PREFERENCES_SEED.getValue(), newSeed);
    }


    /** Стандартная обработка нажатия "назад" без дополнительной логики. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
