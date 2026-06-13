package com.samuilolegovich.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
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

import static com.samuilolegovich.view.Referral.REFERRAL_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class RestoreWallet extends BaseActivity {

    @Inject WalletRepository repository;
    public static final String RESTORE_WALLET_CLASS = ".RestoreWallet";

    private String ERROR_CHECK_THE_SEED_AND_TRY_AGAIN;

    private TextView restoreWalletTextView;
    private TextInputLayout tilSeed;
    private EditText seed;
    private View next;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.restore_wallet_page);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        restoreWalletTextView = (TextView) findViewById(R.id.restore_wallet_text_view);
        tilSeed = findViewById(R.id.til_restore_wallet_seed_field);
        seed = (EditText) findViewById(R.id.restore_wallet_seed_field);
        next = findViewById(R.id.restore_wallet_next_link);
    }


    private void setLanguage() {
        ERROR_CHECK_THE_SEED_AND_TRY_AGAIN = getString(R.string.error_check_the_seed_and_try_again);
        restoreWalletTextView.setText(R.string.restore_from_backup_seed);
    }


    private void listeners() {
        next.setOnClickListener(v -> {
            pulse(v);
            String seedRestore = seed.getText().toString();
            if (seedRestore.length() > 20) {
                recoverWalletAsync(seedRestore);
            } else {
                tilSeed.setError(ERROR_CHECK_THE_SEED_AND_TRY_AGAIN);
            }
        });

        seed.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { tilSeed.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }


    private void recoverWalletAsync(String seedRestore) {
        AppExecutors.io().execute(() -> {
            Map<String, String> map = repository.restoreWallet(seedRestore);
            boolean success = map != null && map.containsKey("Classic Address");
            runOnUiThread(() -> {
                if (success) {
                    encryptAndWriteSeed(seedRestore);
                    MainActivity.START_FLAG = false;
                    repository.loadBalance();
                    goToAnotherPage(REFERRAL_CLASS);
                } else {
                    tilSeed.setError(ERROR_CHECK_THE_SEED_AND_TRY_AGAIN);
                }
            });
        });
    }


    private void encryptAndWriteSeed(String seedRestore) {
        SecureSeedStorage.save(PrefsHelper.get(this), StringEnum.APP_PREFERENCES_SEED.getValue(), seedRestore);
    }


    private void goToAnotherPage(String namePage) {
        startActivity(new Intent(namePage));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
