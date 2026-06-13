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




@AndroidEntryPoint
public class CreateNewWallet extends BaseActivity {

    @Inject WalletRepository repository;
    public static final String CREATE_NEW_WALLET_CLASS = ".CreateNewWallet";

    private volatile boolean isNewWallet = false;
    private String seedString;

    private TextView createNewWalletText;
    private TextView seed;
    private View next;



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



    private void setButtons() {
        createNewWalletText = (TextView) findViewById(R.id.create_new_wallet_text_view);
        seed = (TextView) findViewById(R.id.seed_field);
        next = findViewById(R.id.next_link);
    }


    private void setLanguage() {
        createNewWalletText.setText(R.string.lead_the_seed);
        seed.setText(R.string.wrong_restart_please);
    }


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
                }
            });
        });
    }


    private void setPreSeed(String newSeed) {
        SecureSeedStorage.save(PrefsHelper.get(this), StringEnum.APP_PREFERENCES_PRE_SEED.getValue(), newSeed);
    }


    private void goToAnotherPage(String namePage) {
        startActivity(new Intent(namePage));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
