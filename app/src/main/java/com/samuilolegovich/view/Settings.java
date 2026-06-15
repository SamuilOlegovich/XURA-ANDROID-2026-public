package com.samuilolegovich.view;

import static com.samuilolegovich.view.BecomeReferral.BECOME_REFERRAL_CLASS;
import static com.samuilolegovich.view.InfoMain.INFO_MAIN_CLASS;
import static com.samuilolegovich.view.SelectLanguage.SELECT_LANGUAGE_CLASS;
import static com.samuilolegovich.view.SettingsSetPasswordForApp.SETTINGS_SET_PASSWORD_FOR_APP_CLASS;
import static com.samuilolegovich.view.TransactionHistory.TRANSACTION_HISTORY_CLASS;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.config.NetworkConfig;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.BiometricHelper;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.wallet.repository.WalletRepository;

import org.xrpl.xrpl4j.codec.addresses.AddressBase58;
import org.xrpl.xrpl4j.codec.addresses.Decoded;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;

import com.google.common.primitives.UnsignedInteger;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



@AndroidEntryPoint
public class Settings extends BaseActivity {
    public static final String SETTINGS_CLASS = ".Settings";

    @SuppressLint("StaticFieldLeak")
    public static volatile Settings SETTINGS_ACTIVITY;

    @Inject WalletRepository repository;

    // ── Existing views ───────────────────────────────────────────────────
    private View settingsSelectEnglishLinc;
    private View settingsSetPasswordLinc;
    private View settingsBiometricLinc;
    private TextView biometricTitleText;
    private android.widget.ImageView setPasswordIcon;
    private TextView settingsTextView;
    private MaterialCardView cardTestBalance;
    private TextView tvTestBalance;
    private MaterialButton btnResetTestBalance;
    private View btnGameMode;
    private TextView gameModeTitle;
    private android.widget.ImageView gameModeIcon;
    private View becomeReferralLinc;
    private View infoLinc;
    private View devTxHistoryLinc;
    private View root;

    // ── DEV section views ────────────────────────────────────────────────
    private MaterialCardView cardDevNetwork;
    private SwitchMaterial   devNetworkSwitch;
    private TextView         devNetworkLabel;
    private EditText         etDevRoulette;
    private EditText         etDevColor;
    private EditText         etDevNumber;
    private MaterialButton   btnDevSave;
    private MaterialButton   btnDevFaucet;
    private MaterialButton   btnDevGenWallet;
    private MaterialButton   btnDevFundWallet;
    private MaterialButton   btnDevPasteRoulette;
    private MaterialButton   btnDevPasteColor;
    private MaterialButton   btnDevPasteNumber;
    private View             layoutDevWalletResult;
    private TextInputLayout  tilDevWalletAddress;
    private TextInputLayout  tilDevWalletSeed;
    private EditText         etDevWalletAddress;
    private EditText         etDevWalletSeed;

    // ── DEV unlock: 7 taps on title ──────────────────────────────────────
    private static final int  UNLOCK_TAPS    = 7;
    private static final long UNLOCK_WINDOW  = 3000L; // ms
    private int  tapCount;
    private long firstTapTime;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
        SETTINGS_ACTIVITY = this;
        root = findViewById(android.R.id.content);
        setButtons();
        setLanguage();
        listeners();
        setupBottomNav();
        repository.getBalanceLiveData().observe(this, balance -> {
            if (!Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE) && tvTestBalance != null) {
                tvTestBalance.setText(
                    balance.setScale(2, java.math.RoundingMode.DOWN).toPlainString() + " XRP");
            }
        });
    }



    private void setButtons() {
        settingsSelectEnglishLinc = findViewById(R.id.settings_select_english_linc);
        settingsSetPasswordLinc   = findViewById(R.id.settings_set_password_linc);
        settingsBiometricLinc     = findViewById(R.id.settings_biometric_linc);
        biometricTitleText        = findViewById(R.id.biometric_title);
        setPasswordIcon           = findViewById(R.id.set_password_icon);
        settingsTextView          = findViewById(R.id.settings_text_view);
        cardTestBalance           = findViewById(R.id.card_test_balance);
        tvTestBalance             = findViewById(R.id.tv_test_balance);
        btnResetTestBalance       = findViewById(R.id.btn_reset_test_balance);
        btnGameMode               = findViewById(R.id.settings_game_mode_linc);
        gameModeTitle             = findViewById(R.id.game_mode_title);
        gameModeIcon              = findViewById(R.id.game_mode_icon);
        becomeReferralLinc        = findViewById(R.id.become_referral_linc);
        infoLinc                  = findViewById(R.id.info_settings_linc);
        devTxHistoryLinc          = findViewById(R.id.dev_tx_history_linc);

        // DEV
        cardDevNetwork  = findViewById(R.id.card_dev_network);
        devNetworkSwitch = findViewById(R.id.dev_network_switch);
        devNetworkLabel  = findViewById(R.id.dev_network_label);
        etDevRoulette    = findViewById(R.id.et_dev_roulette);
        etDevColor       = findViewById(R.id.et_dev_color);
        etDevNumber      = findViewById(R.id.et_dev_number);
        btnDevSave          = findViewById(R.id.btn_dev_save);
        btnDevFaucet        = findViewById(R.id.btn_dev_faucet);
        btnDevGenWallet       = findViewById(R.id.btn_dev_gen_wallet);
        btnDevFundWallet      = findViewById(R.id.btn_dev_fund_wallet);
        btnDevPasteRoulette   = findViewById(R.id.btn_dev_paste_roulette);
        btnDevPasteColor      = findViewById(R.id.btn_dev_paste_color);
        btnDevPasteNumber     = findViewById(R.id.btn_dev_paste_number);
        layoutDevWalletResult = findViewById(R.id.layout_dev_wallet_result);
        tilDevWalletAddress   = findViewById(R.id.til_dev_wallet_address);
        tilDevWalletSeed      = findViewById(R.id.til_dev_wallet_seed);
        etDevWalletAddress    = findViewById(R.id.et_dev_wallet_address);
        etDevWalletSeed       = findViewById(R.id.et_dev_wallet_seed);
    }


    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        settingsTextView.setText(R.string.settings_text);
        updateBiometricButton();
        updateGameModeButton();
        updateTestBalanceCard();
        updatePasswordIcon();
        restoreDevSection();
    }

    @SuppressLint("SetTextI18n")
    private void updateGameModeButton() {
        boolean isReal = Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE);
        String state = isReal ? "  ●  LIVE" : "  ○  TRIAL";
        gameModeTitle.setText(getString(R.string.settings_game_mode) + state);
        int bgRes    = isReal ? R.drawable.bg_card_gold   : R.drawable.bg_card_send;
        int colorRes = isReal ? R.color.xura_gold         : R.color.xura_pink;
        btnGameMode.setBackgroundResource(bgRes);
        int color = ContextCompat.getColor(this, colorRes);
        gameModeTitle.setTextColor(color);
        gameModeIcon.setImageTintList(android.content.res.ColorStateList.valueOf(color));
    }

    private void updateTestBalanceCard() {
        boolean isReal = Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE);
        if (!isReal) {
            cardTestBalance.setVisibility(View.VISIBLE);
            updateTestBalanceDisplay();
        } else {
            cardTestBalance.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateTestBalanceDisplay() {
        java.math.BigDecimal balance = repository.getBalance();
        tvTestBalance.setText(balance.setScale(2, java.math.RoundingMode.DOWN).toPlainString() + " XRP");
    }

    private void updateBiometricButton() {
        String state = isBiometricEnabled() ? "  ●  ON" : "  ○  OFF";
        biometricTitleText.setText(getString(R.string.settings_biometric) + state);
    }

    private void updatePasswordIcon() {
        boolean hasPassword = isPasswordSet();
        setPasswordIcon.setImageResource(hasPassword ? R.drawable.ic_lock : R.drawable.ic_lock_open);
    }



    // ════════════════════════════════════════════════════════════════════
    //  DEV section
    // ════════════════════════════════════════════════════════════════════

    /** Fills DEV card fields from current NetworkConfig state. */
    private void restoreDevSection() {
        devNetworkSwitch.setChecked(NetworkConfig.IS_TESTNET);
        updateNetworkLabel(NetworkConfig.IS_TESTNET);
        etDevRoulette.setText(NetworkConfig.SERVER_ROULETTE);
        etDevColor.setText(NetworkConfig.SERVER_COLOR);
        etDevNumber.setText(NetworkConfig.SERVER_NUMBER);
        btnDevFaucet.setVisibility(NetworkConfig.IS_TESTNET ? View.VISIBLE : View.GONE);

        // Show card if testnet is already active (persisted from previous session)
        if (NetworkConfig.IS_TESTNET) {
            cardDevNetwork.setVisibility(View.VISIBLE);
        }
    }

    private void updateNetworkLabel(boolean isTestnet) {
        devNetworkLabel.setText(
                isTestnet ? getString(R.string.dev_network_testnet)
                          : getString(R.string.dev_network_mainnet));
        devNetworkLabel.setTextColor(ContextCompat.getColor(this,
                isTestnet ? R.color.xura_purple : R.color.xura_text_primary));
    }

    private void saveDevSettings() {
        SharedPreferences prefs = PrefsHelper.get(this);

        String roulette = textOf(etDevRoulette, NetworkConfig.SERVER_ROULETTE);
        String color    = textOf(etDevColor,    NetworkConfig.SERVER_COLOR);
        String number   = textOf(etDevNumber,   NetworkConfig.SERVER_NUMBER);

        NetworkConfig.SERVER_ROULETTE = roulette;
        NetworkConfig.SERVER_COLOR    = color;
        NetworkConfig.SERVER_NUMBER   = number;
        NetworkConfig.save(prefs);

        showSnackbar(root, getString(R.string.dev_saved_toast), SnackbarType.INFO);
    }

    private void requestFaucet() {
        String address = repository.getClassicAddress();
        if (address == null || address.isEmpty()) {
            showSnackbar(root, "No wallet address loaded.", SnackbarType.ERROR);
            return;
        }

        btnDevFaucet.setText(getString(R.string.dev_faucet_loading));
        btnDevFaucet.setEnabled(false);

        executor.execute(() -> {
            boolean success = callFaucet(address);
            new Handler(Looper.getMainLooper()).post(() -> {
                btnDevFaucet.setText(getString(R.string.dev_faucet_btn));
                btnDevFaucet.setEnabled(true);
                if (success) {
                    showSnackbar(root, getString(R.string.dev_faucet_success), SnackbarType.INFO);
                    new Handler(Looper.getMainLooper()).postDelayed(
                            () -> repository.loadNetworkBalance(), 4000);
                } else {
                    showSnackbar(root, getString(R.string.dev_faucet_error), SnackbarType.ERROR);
                }
            });
        });
    }

    private boolean callFaucet(String address) {
        try {
            OkHttpClient client = buildDevHttpClient();
            String json = "{\"destination\":\"" + address + "\"}";
            RequestBody body = RequestBody.create(json,
                    MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(StringEnum.FAUCET_CLIENT_HTTP_URL_TEST.getValue() + "/accounts")
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private OkHttpClient buildDevHttpClient() {
        return com.samuilolegovich.wallet.myClient.SslUtil.trustAllOkHttpClient();
    }

    private String textOf(EditText et, String fallback) {
        String s = et.getText() != null ? et.getText().toString().trim() : "";
        return s.isEmpty() ? fallback : s;
    }



    // ════════════════════════════════════════════════════════════════════
    //  Listeners
    // ════════════════════════════════════════════════════════════════════

    private void listeners() {
        settingsSetPasswordLinc.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(SETTINGS_SET_PASSWORD_FOR_APP_CLASS);
        });

        settingsSelectEnglishLinc.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(SELECT_LANGUAGE_CLASS);
        });

        settingsBiometricLinc.setOnClickListener(v -> {
            pulse(v);
            handleBiometricToggle();
        });

        btnGameMode.setOnClickListener(v -> {
            pulse(v);
            boolean isReal = Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE);
            new AlertDialog.Builder(this)
                    .setTitle(getString(isReal
                            ? R.string.settings_game_mode_switch_to_trial
                            : R.string.settings_game_mode_switch_to_live))
                    .setMessage(getString(isReal
                            ? R.string.settings_game_mode_trial_info
                            : R.string.settings_game_mode_live_warning))
                    .setPositiveButton("CONFIRM", (d, w) -> {
                        saveGameMode(!isReal);
                        updateGameModeButton();
                        updateTestBalanceCard();
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        });

        becomeReferralLinc.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(BECOME_REFERRAL_CLASS);
        });

        infoLinc.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(INFO_MAIN_CLASS);
        });

        devTxHistoryLinc.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(TRANSACTION_HISTORY_CLASS);
        });

        btnResetTestBalance.setOnClickListener(v -> {
            pulse(v);
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.settings_test_mode_section))
                    .setMessage(getString(R.string.settings_reset_test_balance_confirm))
                    .setPositiveButton(getString(R.string.settings_reset_test_balance), (d, w) -> {
                        repository.resetTestBalance();
                        updateTestBalanceDisplay();
                        showSnackbar(root, getString(R.string.settings_reset_test_balance_done), SnackbarType.INFO);
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        });

        // 7 taps on title → toggle DEV card (show if hidden, hide if visible)
        settingsTextView.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (tapCount == 0 || now - firstTapTime > UNLOCK_WINDOW) {
                tapCount = 1;
                firstTapTime = now;
            } else {
                tapCount++;
            }
            if (tapCount >= UNLOCK_TAPS) {
                tapCount = 0;
                if (cardDevNetwork.getVisibility() == View.VISIBLE) {
                    cardDevNetwork.setVisibility(View.GONE);
                } else {
                    cardDevNetwork.setVisibility(View.VISIBLE);
                    pulse(cardDevNetwork);
                }
            }
        });

        // Network switch
        devNetworkSwitch.setOnCheckedChangeListener((btn, isTestnet) -> {
            NetworkConfig.switchNetwork(PrefsHelper.get(this), isTestnet);
            updateNetworkLabel(isTestnet);
            btnDevFaucet.setVisibility(isTestnet ? View.VISIBLE : View.GONE);
            // Reload address fields for the newly selected network
            etDevRoulette.setText(NetworkConfig.SERVER_ROULETTE);
            etDevColor.setText(NetworkConfig.SERVER_COLOR);
            etDevNumber.setText(NetworkConfig.SERVER_NUMBER);
            String msg = isTestnet
                    ? getString(R.string.dev_network_switched_testnet)
                    : getString(R.string.dev_network_switched_mainnet);
            showSnackbar(root, msg, SnackbarType.INFO);
        });

        btnDevSave.setOnClickListener(v -> {
            pulse(v);
            saveDevSettings();
        });

        btnDevFaucet.setOnClickListener(v -> {
            pulse(v);
            requestFaucet();
        });

        btnDevGenWallet.setOnClickListener(v -> {
            pulse(v);
            generateTestWallet();
        });

        btnDevFundWallet.setOnClickListener(v -> {
            pulse(v);
            fundGeneratedWallet();
        });

        btnDevPasteRoulette.setOnClickListener(v -> {
            pasteAddressToGame(etDevRoulette, "ROULETTE");
        });

        btnDevPasteColor.setOnClickListener(v -> {
            pasteAddressToGame(etDevColor, "COLOR");
        });

        btnDevPasteNumber.setOnClickListener(v -> {
            pasteAddressToGame(etDevNumber, "NUMBER");
        });

        tilDevWalletAddress.setEndIconOnClickListener(v ->
                copyToClipboard("address", etDevWalletAddress.getText().toString(),
                        getString(R.string.dev_wallet_copied_address)));

        tilDevWalletSeed.setEndIconOnClickListener(v ->
                copyToClipboard("seed", etDevWalletSeed.getText().toString(),
                        getString(R.string.dev_wallet_copied_seed)));
    }

    private void generateTestWallet() {
        Seed seed = Seed.ed25519Seed();
        KeyPair keyPair = seed.deriveKeyPair();
        String address = keyPair.publicKey().deriveAddress().toString();

        Decoded decoded = seed.decodedSeed();
        String seedBase58 = AddressBase58.encode(
                decoded.bytes(),
                Collections.singletonList(decoded.version()),
                UnsignedInteger.valueOf(16));

        etDevWalletAddress.setText(address);
        etDevWalletSeed.setText(seedBase58);
        layoutDevWalletResult.setVisibility(View.VISIBLE);
    }

    private void fundGeneratedWallet() {
        String address = etDevWalletAddress.getText() != null
                ? etDevWalletAddress.getText().toString().trim() : "";
        if (address.isEmpty()) return;

        btnDevFundWallet.setText(getString(R.string.dev_wallet_fund_loading));
        btnDevFundWallet.setEnabled(false);

        executor.execute(() -> {
            boolean success = callFaucet(address);
            new Handler(Looper.getMainLooper()).post(() -> {
                btnDevFundWallet.setText(getString(R.string.dev_wallet_fund_btn));
                btnDevFundWallet.setEnabled(true);
                showSnackbar(root,
                        getString(success ? R.string.dev_wallet_fund_success
                                          : R.string.dev_wallet_fund_error),
                        success ? SnackbarType.INFO : SnackbarType.ERROR);
                if (success) {
                    new Handler(Looper.getMainLooper()).postDelayed(
                            () -> repository.loadNetworkBalance(), 4000);
                }
            });
        });
    }

    private void pasteAddressToGame(EditText target, String gameName) {
        String address = etDevWalletAddress.getText() != null
                ? etDevWalletAddress.getText().toString().trim() : "";
        if (address.isEmpty()) return;
        target.setText(address);
        showSnackbar(root, getString(R.string.dev_wallet_pasted) + " → " + gameName,
                SnackbarType.INFO);
    }

    private void copyToClipboard(String label, String text, String toast) {
        if (text == null || text.isEmpty()) return;
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText(label, text));
        showSnackbar(root, toast, SnackbarType.INFO);
    }



    // ════════════════════════════════════════════════════════════════════
    //  Shared helpers
    // ════════════════════════════════════════════════════════════════════

    private boolean isPasswordSet() {
        String stored = PrefsHelper.get(this).getString(
                StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "");
        return stored != null
                && !stored.isEmpty()
                && !stored.equals(StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue());
    }

    private boolean isBiometricEnabled() {
        return "true".equalsIgnoreCase(
                PrefsHelper.get(this).getString(
                        StringEnum.APP_PREFERENCES_BIOMETRIC_ENABLED.getValue(), "false"));
    }

    private void saveBiometricEnabled(boolean enabled) {
        PrefsHelper.get(this).edit()
                .putString(StringEnum.APP_PREFERENCES_BIOMETRIC_ENABLED.getValue(),
                        enabled ? "true" : "false")
                .apply();
    }

    private void saveGameMode(boolean isReal) {
        String value = isReal
                ? StringEnum.APP_GAME_MODE_REAL.getValue()
                : StringEnum.APP_GAME_MODE_TEST.getValue();
        PrefsHelper.get(this).edit()
                .putString(StringEnum.APP_GAME_MODE.getValue(), value)
                .apply();
        MainActivity.IS_REAL_GAME_MODE = isReal;
    }

    private void handleBiometricToggle() {
        if (!BiometricHelper.isAvailable(this)) {
            showSnackbar(root, getString(R.string.biometrics_not_set_up), SnackbarType.ERROR);
            return;
        }
        if (isBiometricEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.settings_biometric))
                    .setMessage("Disable biometrics?")
                    .setPositiveButton("DISABLE", (d, w) -> {
                        saveBiometricEnabled(false);
                        updateBiometricButton();
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        } else {
            BiometricHelper.prompt(this,
                    getString(R.string.biometric_prompt_title),
                    getString(R.string.biometric_prompt_subtitle),
                    new BiometricHelper.Callback() {
                        @Override public void onSuccess() {
                            saveBiometricEnabled(true);
                            updateBiometricButton();
                        }
                        @Override public void onFallback() {}
                        @Override public void onError(String message) {}
                    });
        }
    }

    private void goToAnotherPage(String namePage) {
        startActivity(new Intent(namePage));
    }

    public void setLanguageThread() {
        runOnUiThread(this::recreate);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        SETTINGS_ACTIVITY = null;
    }
}
