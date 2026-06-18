package com.samuilolegovich.view;

import static com.samuilolegovich.view.BecomeReferral.BECOME_REFERRAL_CLASS;
import static com.samuilolegovich.view.InfoMain.INFO_MAIN_CLASS;
import static com.samuilolegovich.view.SelectLanguage.SELECT_LANGUAGE_CLASS;
import static com.samuilolegovich.view.SettingsSetPasswordForApp.SETTINGS_SET_PASSWORD_FOR_APP_CLASS;
import static com.samuilolegovich.view.TransactionHistory.TRANSACTION_HISTORY_CLASS;

import android.annotation.SuppressLint;
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
import com.samuilolegovich.utils.AudioHelper;
import com.samuilolegovich.utils.BiometricHelper;
import com.samuilolegovich.utils.ClipboardUtil;
import com.samuilolegovich.utils.InactivityGuard;
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



/**
 * Экран настроек приложения: пароль, биометрия, язык, переключение реального/тестового
 * режима игры, реферальная программа, информация о приложении, история транзакций,
 * а также скрытая DEV-секция (открывается 7 нажатиями на заголовок) для переключения
 * сети testnet/mainnet, ручной настройки адресов игровых серверов, фасета тестовой сети
 * и генерации/пополнения тестовых кошельков.
 */
@AndroidEntryPoint
public class Settings extends BaseActivity {
    public static final String SETTINGS_CLASS = ".Settings";

    @SuppressLint("StaticFieldLeak")
    public static volatile Settings SETTINGS_ACTIVITY;

    @Inject WalletRepository repository;

    private static final long[] TIMEOUT_OPTIONS_MS = {
        30_000L, 60_000L, 2 * 60_000L, 5 * 60_000L, 15 * 60_000L
    };
    private static final String[] TIMEOUT_LABELS = {
        "30 sec", "1 min", "2 min", "5 min", "15 min"
    };

    // ── Существующие View ───────────────────────────────────────────────
    private View settingsSelectEnglishLinc;
    private View settingsSetPasswordLinc;
    private View settingsBiometricLinc;
    private View settingsLockTimeoutLinc;
    private View settingsSoundLinc;
    private TextView biometricTitleText;
    private TextView lockTimeoutTitle;
    private TextView soundTitle;
    private android.widget.ImageView soundIcon;
    private android.widget.ImageView setPasswordIcon;
    private TextView settingsTextView;
    private MaterialCardView cardTestBalance;
    private TextView tvTestBalance;
    private TextView chipModeBadge;
    private MaterialButton btnResetTestBalance;
    private View btnGameMode;
    private TextView gameModeTitle;
    private android.widget.ImageView gameModeIcon;
    private TextView gameModeSubtitle;
    private View becomeReferralLinc;
    private View infoLinc;
    private View devTxHistoryLinc;
    private View root;
    private TextView settingsFooterVersion;
    private TextView settingsFooterDeveloper;

    // ── View скрытой DEV-секции ──────────────────────────────────────────
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

    // ── Разблокировка DEV-секции: 7 нажатий на заголовок ─────────────────
    private static final int  UNLOCK_TAPS    = 7;
    private static final long UNLOCK_WINDOW  = 3000L; // ms
    private int  tapCount;
    private long firstTapTime;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();



    /**
     * Инициализирует экран: сохраняет ссылку на активити, разметку, View, локализацию/состояние
     * переключателей, слушателей, нижнюю навигацию, и подписывается на баланс для тестовой карточки.
     */
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



    /** Находит и сохраняет ссылки на все View разметки экрана, включая скрытую DEV-секцию. */
    private void setButtons() {
        settingsSelectEnglishLinc = findViewById(R.id.settings_select_english_linc);
        settingsSetPasswordLinc   = findViewById(R.id.settings_set_password_linc);
        settingsBiometricLinc     = findViewById(R.id.settings_biometric_linc);
        settingsLockTimeoutLinc   = findViewById(R.id.settings_lock_timeout_linc);
        settingsSoundLinc         = findViewById(R.id.settings_sound_linc);
        biometricTitleText        = findViewById(R.id.biometric_title);
        lockTimeoutTitle          = findViewById(R.id.lock_timeout_title);
        soundTitle                = findViewById(R.id.sound_title);
        soundIcon                 = findViewById(R.id.sound_icon);
        setPasswordIcon           = findViewById(R.id.set_password_icon);
        settingsTextView          = findViewById(R.id.settings_text_view);
        cardTestBalance           = findViewById(R.id.card_test_balance);
        tvTestBalance             = findViewById(R.id.tv_test_balance);
        chipModeBadge             = findViewById(R.id.chip_mode_badge);
        btnResetTestBalance       = findViewById(R.id.btn_reset_test_balance);
        btnGameMode               = findViewById(R.id.settings_game_mode_linc);
        gameModeTitle             = findViewById(R.id.game_mode_title);
        gameModeSubtitle          = findViewById(R.id.game_mode_subtitle);
        gameModeIcon              = findViewById(R.id.game_mode_icon);
        becomeReferralLinc        = findViewById(R.id.become_referral_linc);
        infoLinc                  = findViewById(R.id.info_settings_linc);
        devTxHistoryLinc          = findViewById(R.id.dev_tx_history_linc);
        settingsFooterVersion     = findViewById(R.id.settings_footer_version);
        settingsFooterDeveloper   = findViewById(R.id.settings_footer_developer);

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


    /** Устанавливает локализованный заголовок и обновляет состояние всех динамических элементов экрана (биометрия, режим игры, тестовый баланс, иконка пароля, DEV-секция, футер). */
    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        settingsTextView.setText(R.string.settings_text);
        updateBiometricButton();
        updateLockTimeoutButton();
        updateSoundButton();
        updateGameModeButton();
        updateTestBalanceCard();
        updatePasswordIcon();
        restoreDevSection();
        updateFooter();
    }

    /** Заполняет футер экрана номером версии приложения и годами разработки в копирайте. */
    @SuppressLint("SetTextI18n")
    private void updateFooter() {
        String versionName = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException ignored) {}
        settingsFooterVersion.setText("XURA v" + versionName);

        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String years = currentYear > 2022 ? "2022–" + currentYear : "2022";
        settingsFooterDeveloper.setText("© " + years + " Samuil Olegovich");
    }

    /** Обновляет вид кнопки режима игры: текст LIVE/TRIAL, цвет и фон в зависимости от текущего режима. */
    @SuppressLint("SetTextI18n")
    private void updateGameModeButton() {
        boolean isReal = Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE);
        String state = isReal ? "  ●  LIVE" : "  ○  TRIAL";
        gameModeTitle.setText(getString(R.string.settings_game_mode) + state);
        int bgRes       = isReal ? R.drawable.bg_card_action_primary : R.drawable.bg_card_send;
        int colorRes    = isReal ? R.color.xura_cyan               : R.color.xura_pink;
        int iconRes     = isReal ? R.drawable.ic_flask             : R.drawable.ic_shield;
        int subtitleRes = isReal ? R.string.settings_game_mode_tap_to_trial
                                 : R.string.settings_game_mode_tap_to_live;
        btnGameMode.setBackgroundResource(bgRes);
        int color = ContextCompat.getColor(this, colorRes);
        gameModeTitle.setTextColor(color);
        gameModeSubtitle.setText(subtitleRes);
        gameModeIcon.setImageResource(iconRes);
        gameModeIcon.setImageTintList(android.content.res.ColorStateList.valueOf(color));
    }

    /** Показывает карточку тестового баланса только в тестовом режиме игры; обновляет чип-бейдж:
     *  TESTNET (dev) → фиолетовый, TRIAL (обычный) → cyan. */
    private void updateTestBalanceCard() {
        boolean isReal = Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE);
        if (!isReal) {
            cardTestBalance.setVisibility(View.VISIBLE);
            updateTestBalanceDisplay();
            if (chipModeBadge != null) {
                if (NetworkConfig.IS_TESTNET) {
                    chipModeBadge.setText(R.string.settings_testnet_badge);
                    chipModeBadge.setBackgroundResource(R.drawable.bg_chip_testnet);
                } else {
                    chipModeBadge.setText(R.string.badge_trial_mode);
                    chipModeBadge.setBackgroundResource(R.drawable.bg_chip_trial);
                }
            }
        } else {
            cardTestBalance.setVisibility(View.GONE);
        }
    }

    /** Отображает текущий тестовый баланс кошелька на карточке. */
    @SuppressLint("SetTextI18n")
    private void updateTestBalanceDisplay() {
        java.math.BigDecimal balance = repository.getBalance();
        tvTestBalance.setText(balance.setScale(2, java.math.RoundingMode.DOWN).toPlainString() + " XRP");
    }

    /** Обновляет текст пункта биометрии, показывая её текущее состояние (ON/OFF). */
    private void updateBiometricButton() {
        String state = isBiometricEnabled() ? "  ●  ON" : "  ○  OFF";
        biometricTitleText.setText(getString(R.string.settings_biometric) + state);
    }

    /** Обновляет текст и иконку пункта звука в зависимости от текущего состояния (ON/OFF). */
    private void updateSoundButton() {
        boolean enabled = AudioHelper.isSoundEnabled(this);
        soundTitle.setText("Sound" + (enabled ? "  ●  ON" : "  ○  OFF"));
        soundIcon.setImageResource(enabled ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
    }

    /** Обновляет текст пункта таймаута, показывая текущее значение. */
    private void updateLockTimeoutButton() {
        lockTimeoutTitle.setText("Auto-lock  ●  " + formatTimeout(InactivityGuard.getTimeoutMs()));
    }

    /** Форматирует миллисекунды в читаемую строку (sec / min). */
    private String formatTimeout(long ms) {
        if (ms < 60_000L) return (ms / 1000) + " sec";
        long min = ms / 60_000L;
        return min + " min";
    }

    /** Сохраняет таймаут в preferences и применяет к InactivityGuard. */
    private void saveLockTimeout(long ms) {
        PrefsHelper.get(this).edit()
                .putLong(StringEnum.APP_PREFERENCES_LOCK_TIMEOUT.getValue(), ms)
                .apply();
        InactivityGuard.setTimeoutMs(ms);
    }

    /** Меняет иконку пункта пароля (закрытый/открытый замок) в зависимости от того, установлен ли пароль приложения. */
    private void updatePasswordIcon() {
        boolean hasPassword = isPasswordSet();
        setPasswordIcon.setImageResource(hasPassword ? R.drawable.ic_lock : R.drawable.ic_lock_open);
    }



    // ════════════════════════════════════════════════════════════════════
    //  DEV section
    // ════════════════════════════════════════════════════════════════════

    /** Заполняет поля DEV-карточки текущим состоянием {@link NetworkConfig} (сеть, адреса игровых серверов, видимость фасета). */
    private void restoreDevSection() {
        devNetworkSwitch.setChecked(NetworkConfig.IS_TESTNET);
        updateNetworkLabel(NetworkConfig.IS_TESTNET);
        etDevRoulette.setText(NetworkConfig.SERVER_ROULETTE);
        etDevColor.setText(NetworkConfig.SERVER_COLOR);
        etDevNumber.setText(NetworkConfig.SERVER_NUMBER);
        btnDevFaucet.setVisibility(NetworkConfig.IS_TESTNET ? View.VISIBLE : View.GONE);

        // Показываем карточку, если testnet уже активен (сохранён с предыдущей сессии)
        if (NetworkConfig.IS_TESTNET) {
            cardDevNetwork.setVisibility(View.VISIBLE);
        }
    }

    /** Обновляет текст и цвет метки текущей сети (testnet/mainnet). */
    private void updateNetworkLabel(boolean isTestnet) {
        devNetworkLabel.setText(
                isTestnet ? getString(R.string.dev_network_testnet)
                          : getString(R.string.dev_network_mainnet));
        devNetworkLabel.setTextColor(ContextCompat.getColor(this,
                isTestnet ? R.color.xura_purple : R.color.xura_text_primary));
    }

    /** Сохраняет введённые в DEV-секции адреса игровых серверов в {@link NetworkConfig} и в preferences. */
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

    /** Запрашивает у testnet-фасета тестовые XRP на текущий адрес кошелька и после паузы обновляет баланс. */
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

    /** Выполняет синхронный HTTP-запрос к testnet-фасету для пополнения указанного адреса. */
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

    /** Создаёт HTTP-клиент с доверием ко всем сертификатам — допустимо только для DEV-запросов к тестовому фасету. */
    private OkHttpClient buildDevHttpClient() {
        return com.samuilolegovich.wallet.client.SslUtil.trustAllOkHttpClient();
    }

    /** Возвращает текст поля ввода (обрезанный), либо запасное значение, если поле пустое. */
    private String textOf(EditText et, String fallback) {
        String s = et.getText() != null ? et.getText().toString().trim() : "";
        return s.isEmpty() ? fallback : s;
    }



    // ════════════════════════════════════════════════════════════════════
    //  Слушатели
    // ════════════════════════════════════════════════════════════════════

    /**
     * Назначает обработчики всех пунктов настроек (пароль, язык, биометрия, режим игры,
     * рефералы, инфо, история транзакций, сброс тестового баланса), скрытую разблокировку
     * DEV-секции по 7 нажатиям на заголовок, и все элементы управления DEV-секцией.
     */
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

        settingsSoundLinc.setOnClickListener(v -> {
            pulse(v);
            boolean current = AudioHelper.isSoundEnabled(this);
            AudioHelper.setSoundEnabled(this, !current);
            updateSoundButton();
        });

        settingsLockTimeoutLinc.setOnClickListener(v -> {
            pulse(v);
            long current = InactivityGuard.getTimeoutMs();
            int checkedIndex = 0;
            for (int i = 0; i < TIMEOUT_OPTIONS_MS.length; i++) {
                if (TIMEOUT_OPTIONS_MS[i] == current) { checkedIndex = i; break; }
            }
            new AlertDialog.Builder(this)
                    .setTitle("Auto-lock timeout")
                    .setSingleChoiceItems(TIMEOUT_LABELS, checkedIndex, (d, which) -> {
                        saveLockTimeout(TIMEOUT_OPTIONS_MS[which]);
                        updateLockTimeoutButton();
                        d.dismiss();
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
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
                        repository.loadBalance();
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

        // 7 нажатий на заголовок → переключить видимость DEV-карточки
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

        // Переключатель сети
        devNetworkSwitch.setOnCheckedChangeListener((btn, isTestnet) -> {
            NetworkConfig.switchNetwork(PrefsHelper.get(this), isTestnet);
            updateNetworkLabel(isTestnet);
            btnDevFaucet.setVisibility(isTestnet ? View.VISIBLE : View.GONE);
            // Перезагружаем поля адресов для только что выбранной сети
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

    /** Генерирует новый тестовый XRPL-кошелёк (ed25519): выводит адрес и seed-фразу в поля DEV-секции. */
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

    /** Запрашивает у testnet-фасета тестовые XRP на адрес сгенерированного DEV-кошелька. */
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

    /** Подставляет адрес сгенерированного DEV-кошелька в поле адреса указанной игры. */
    private void pasteAddressToGame(EditText target, String gameName) {
        String address = etDevWalletAddress.getText() != null
                ? etDevWalletAddress.getText().toString().trim() : "";
        if (address.isEmpty()) return;
        target.setText(address);
        showSnackbar(root, getString(R.string.dev_wallet_pasted) + " → " + gameName,
                SnackbarType.INFO);
    }

    /** Копирует текст в буфер обмена с автоочисткой (через {@link ClipboardUtil}) и показывает уведомление. */
    private void copyToClipboard(String label, String text, String toast) {
        if (text == null || text.isEmpty()) return;
        ClipboardUtil.copyWithAutoClear(this, label, text);
        showSnackbar(root, toast, SnackbarType.INFO);
    }



    // ════════════════════════════════════════════════════════════════════
    //  Общие вспомогательные методы
    // ════════════════════════════════════════════════════════════════════

    /** Проверяет, установлен ли пароль приложения (не пустой и не равен значению "пароль не установлен"). */
    private boolean isPasswordSet() {
        String stored = PrefsHelper.get(this).getString(
                StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "");
        return stored != null
                && !stored.isEmpty()
                && !stored.equals(StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue());
    }

    /** Проверяет, включена ли биометрическая разблокировка приложения. */
    private boolean isBiometricEnabled() {
        return "true".equalsIgnoreCase(
                PrefsHelper.get(this).getString(
                        StringEnum.APP_PREFERENCES_BIOMETRIC_ENABLED.getValue(), "false"));
    }

    /** Сохраняет в preferences признак того, включена ли биометрическая разблокировка приложения. */
    private void saveBiometricEnabled(boolean enabled) {
        PrefsHelper.get(this).edit()
                .putString(StringEnum.APP_PREFERENCES_BIOMETRIC_ENABLED.getValue(),
                        enabled ? "true" : "false")
                .apply();
    }

    /** Переключает и сохраняет режим игры (реальный/тестовый) в preferences и в статическом флаге {@link MainActivity#IS_REAL_GAME_MODE}. */
    private void saveGameMode(boolean isReal) {
        String value = isReal
                ? StringEnum.APP_GAME_MODE_REAL.getValue()
                : StringEnum.APP_GAME_MODE_TEST.getValue();
        PrefsHelper.get(this).edit()
                .putString(StringEnum.APP_GAME_MODE.getValue(), value)
                .apply();
        MainActivity.IS_REAL_GAME_MODE = isReal;
    }

    /** Обрабатывает переключение биометрии: при выключении спрашивает подтверждение, при включении запрашивает биометрическую проверку через {@link BiometricHelper}. */
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

    /** Запускает Activity по имени её класса/действия. */
    private void goToAnotherPage(String namePage) {
        startActivity(new Intent(namePage));
    }

    /** Пересоздаёт текущую активити на UI-потоке (вызывается извне для применения смены языка). */
    public void setLanguageThread() {
        runOnUiThread(this::recreate);
    }



    /** При возврате из дочерних экранов обновляет динамические элементы (иконка пароля, кнопка звука). */
    @Override
    protected void onResume() {
        super.onResume();
        updatePasswordIcon();
        updateSoundButton();
    }

    /** Стандартная обработка нажатия "назад" без дополнительной логики. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /** Останавливает фоновый executor DEV-секции и сбрасывает статическую ссылку на активити при её уничтожении. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        SETTINGS_ACTIVITY = null;
    }
}
