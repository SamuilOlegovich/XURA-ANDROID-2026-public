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




// тут мы сверим информаци о новом кошельке, правильно ли ее записал юзер
@AndroidEntryPoint
public class CheckingNewWallet extends BaseActivity {

    @Inject WalletRepository repository;
    public static final String CHECKING_NEW_WALLET_CLASS = ".CheckingNewWallet";

    private TextView checkingNewWalletText;
    private TextInputLayout tilSeed;
    private EditText seed;
    private View next;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.checking_new_wallet);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        checkingNewWalletText = (TextView) findViewById(R.id.checking_new_wallet_text);
        tilSeed = findViewById(R.id.til_password_field);
        seed = (EditText) findViewById(R.id.password_field);
        next = findViewById(R.id.next_link);
    }


    private void setLanguage() {
        checkingNewWalletText.setText(R.string.lead_the_seed);
    }


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


    private void goToAnotherPage(String namePage) {
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    private String getPreSeed() {
        return SecureSeedStorage.load(PrefsHelper.get(this), StringEnum.APP_PREFERENCES_PRE_SEED.getValue());
    }


    private void setSeed(String newSeed) {
        SecureSeedStorage.save(PrefsHelper.get(this), StringEnum.APP_PREFERENCES_SEED.getValue(), newSeed);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
