package com.samuilolegovich.view;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.RouletteBetCode;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.AudioHelper;
import com.samuilolegovich.utils.GameSoundPool;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.viewmodel.RouletteViewModel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.samuilolegovich.view.Flasher.FLASHER_CLASS;
import static com.samuilolegovich.view.RulesOfTheGameRoulette.RULES_OF_THE_GAME_ROULETTE_CLASS;
import dagger.hilt.android.AndroidEntryPoint;



/**
 * Экран игры в европейскую рулетку с полноценным казино-столом: программно строит сетку
 * чисел 0–36, ставки на дюжины/столбцы/чёт-нечёт/цвет/диапазон, поддерживает несколько
 * одновременных ставок (мульти-бет) с визуальными фишками на ячейках. При запуске спина
 * ставки отправляются через {@link RouletteViewModel}, а результат показывается на {@link Flasher}.
 */
@AndroidEntryPoint
public class RouletteGame extends BaseActivity {
    public static final String ROULETTE_GAME_CLASS = ".RouletteGame";

    private static final int COLOR_RED   = 0xFFC81030;
    private static final int COLOR_BLACK = 0xFF111111;
    private static final int COLOR_GREEN = 0xFF007040;
    private static final int COLOR_GOLD  = 0xFFFFB000;
    private static final int COLOR_CARD  = 0xFF0D0D20;

    private static final Set<Integer> BLACK_NUMS = new HashSet<>(Arrays.asList(
            2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35));

    private static final int CELL_H = 44;

    // ── ViewModel + prefs ────────────────────────────────────────────────
    private RouletteViewModel viewModel;
    private SharedPreferences preferences;
    private String myReferral;

    // ── Sound ────────────────────────────────────────────────────────────
    private MediaPlayer casinoMediaPlayer;
    private GameSoundPool soundPool;
    private AudioFocusRequest audioFocusRequest;
    private BroadcastReceiver noisyReceiver;

    private final AudioManager.OnAudioFocusChangeListener focusListener = focusChange -> {
        if (casinoMediaPlayer == null) return;
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (casinoMediaPlayer.isPlaying()) casinoMediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                casinoMediaPlayer.setVolume(0.1f, 0.1f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                casinoMediaPlayer.setVolume(0.4f, 0.4f);
                if (!casinoMediaPlayer.isPlaying() && AudioHelper.isSoundEnabled(this))
                    casinoMediaPlayer.start();
                break;
        }
    };

    // ── Multi-bet table state ─────────────────────────────────────────────
    private final LinkedHashMap<String, BigDecimal> tableBets   = new LinkedHashMap<>();
    private final Map<String, View>                 betViews    = new HashMap<>();
    private final Map<String, Integer>              betBgColors = new HashMap<>();
    // chip overlay TextView per bet tag — shows the placed amount on the cell
    private final Map<String, TextView>             chipViews   = new HashMap<>();

    private String pendingPrimaryTag        = null;
    private int    pendingPrimaryMultiplier = 2;

    // ── Error strings ────────────────────────────────────────────────────
    private String YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND;
    private String IT_IS_NOT_POSSIBLE_TO_SEND_NULL;
    private String PAYMENT_AMOUNT_IS_INCORRECT;
    private String WRONG_DESTINATION_ADDRESS;
    private String BET_CANNOT_BE_MORE_THAN;
    private String BET_CANNOT_BE_LESS_THAN;
    private String ENTER_AMOUNT_FIRST;

    // ── Views ────────────────────────────────────────────────────────────
    private View                       rulesInfo;
    private TextView                   balance;
    private TextView                   selectedBetLabel;
    private EditText                   bet;
    private ChipGroup                  chipGroupAmounts;
    private TextInputLayout            tilBetField;
    private View                       btnSpin;
    private View                       btnClearBets;
    private ImageView                  spinIcon;
    private CircularProgressIndicator  spinProgress;



    /**
     * Инициализирует экран: View, ViewModel, локализацию, реферала, игровой стол ставок,
     * слушателей, и подписывается на баланс/ошибки/успешную ставку из ViewModel.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roulette_game_page);

        viewModel = new ViewModelProvider(this).get(RouletteViewModel.class);

        setViews();
        setLanguage();
        getReferral();
        buildTable();
        listeners();

        viewModel.getBalance().observe(this, b ->
                balance.setText(b.stripTrailingZeros().toPlainString() + "  XRP"));

        viewModel.getError().observe(this, error -> {
            if (error == null) return;
            setSpinningState(false);
            soundPool.playError(this);
            String msg;
            switch (error) {
                case INVALID_AMOUNT:       msg = PAYMENT_AMOUNT_IS_INCORRECT; break;
                case AMOUNT_IS_ZERO:       msg = IT_IS_NOT_POSSIBLE_TO_SEND_NULL; break;
                case INSUFFICIENT_BALANCE: msg = YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND; break;
                case BET_TOO_HIGH:         msg = BET_CANNOT_BE_MORE_THAN + StringEnum.MAX_BET_ROULETTE.getValue() + " XRP"; break;
                case BET_TOO_LOW:          msg = BET_CANNOT_BE_LESS_THAN + StringEnum.MIN_BET_ROULETTE.getValue() + " XRP"; break;
                default:                   msg = WRONG_DESTINATION_ADDRESS; break;
            }
            tilBetField.setError(msg);
        });

        viewModel.getBetSuccess().observe(this, totalAmount -> {
            if (totalAmount == null) return;
            setSpinningState(false);
            tilBetField.setError(null);
            bet.setText("");
            chipGroupAmounts.clearCheck();
            setBetParamsForFlasher(totalAmount);
            clearAllBets();
            startActivity(new Intent(FLASHER_CLASS));
        });

        viewModel.loadBalance();
    }



    // ════════════════════════════════════════════════════════════════════
    //  View setup
    // ════════════════════════════════════════════════════════════════════

    /** Находит View разметки экрана и готовит звуковые эффекты (фон казино, ошибка, ставка). */
    private void setViews() {
        rulesInfo        = findViewById(R.id.rules_of_the_game_link);
        balance          = findViewById(R.id.your_balance_xrp_text);
        selectedBetLabel = findViewById(R.id.roulette_selected_bet_label);
        bet              = findViewById(R.id.bet_field);
        tilBetField      = findViewById(R.id.til_bet_field);
        chipGroupAmounts = findViewById(R.id.chip_group_amounts);
        btnSpin          = findViewById(R.id.btn_spin_roulette);
        btnClearBets     = findViewById(R.id.btn_clear_bets);
        spinIcon         = findViewById(R.id.spin_icon);
        spinProgress     = findViewById(R.id.spin_progress);

        casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);
        casinoMediaPlayer.setVolume(0.4f, 0.4f);
        casinoMediaPlayer.setLooping(true);

        soundPool = new GameSoundPool(this);
    }

    /** Загружает локализованные строки для всех сообщений об ошибках на экране. */
    private void setLanguage() {
        YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND = getString(R.string.your_account_is_not_enough_to_send);
        IT_IS_NOT_POSSIBLE_TO_SEND_NULL    = getString(R.string.it_is_not_possible_to_send_null);
        PAYMENT_AMOUNT_IS_INCORRECT        = getString(R.string.payment_amount_is_incorrect);
        WRONG_DESTINATION_ADDRESS          = getString(R.string.wrong_destination_address);
        BET_CANNOT_BE_MORE_THAN            = getString(R.string.bet_cannot_be_more_than);
        BET_CANNOT_BE_LESS_THAN            = getString(R.string.bet_cannot_be_less_than);
        ENTER_AMOUNT_FIRST                 = getString(R.string.roulette_enter_amount_first);
    }



    // ════════════════════════════════════════════════════════════════════
    //  Flasher params — snapshot at SPIN time
    // ════════════════════════════════════════════════════════════════════

    /** Снимает "снимок" параметров для экрана Flasher на момент запуска спина: режим игры, сумму, главную ставку и её число/цвет для подсветки результата. */
    private void setBetParamsForFlasher(String totalAmount) {
        Flasher.TEST_MODE_ENUM          = TestModeEnum.ROULETTE_GAME;
        Flasher.TEST_SAND_AMOUNT        = totalAmount;
        Flasher.ROULETTE_BET_TAG        = pendingPrimaryTag != null ? pendingPrimaryTag : "";
        Flasher.ROULETTE_WIN_MULTIPLIER = pendingPrimaryMultiplier;
        Flasher.ROULETTE_ALL_BETS       = new LinkedHashMap<>(tableBets);

        String tag = pendingPrimaryTag != null ? pendingPrimaryTag : "";
        if (tag.startsWith("N:")) {
            int num = Integer.parseInt(tag.substring(2));
            Flasher.NUMBER_BET = String.valueOf(num);
            Flasher.COLOR_BET  = (num != 0) && !BLACK_NUMS.contains(num);
        } else {
            Flasher.NUMBER_BET = "0";
            Flasher.COLOR_BET  = "RED".equals(tag);
        }
    }



    // ════════════════════════════════════════════════════════════════════
    //  Casino table — each cell is FrameLayout (label + chip overlay)
    // ════════════════════════════════════════════════════════════════════

    /** Программно строит игровой стол рулетки: сетку 13×5 (зеро + числа 1–36 + дюжины + столбцы 2:1) и нижний ряд внешних ставок. */
    private void buildTable() {
        LinearLayout container = findViewById(R.id.roulette_table_container);
        int      cellH = dp(CELL_H);
        Typeface font  = ResourcesCompat.getFont(this, R.font.montserrat);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(5);
        grid.setRowCount(13);
        grid.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Row 0: Zero — spans all 5 columns
        addCell(grid, "0", COLOR_GREEN, 0, 0, 1, 5, cellH, 1f, 12, font, true, "N:0");

        // Rows 1–12, cols 1–3: number cells
        for (int row = 0; row < 12; row++) {
            for (int col = 0; col < 3; col++) {
                int num = row * 3 + col + 1;
                int bg  = BLACK_NUMS.contains(num) ? COLOR_BLACK : COLOR_RED;
                addCell(grid, String.valueOf(num), bg, row + 1, col + 1, 1, 1,
                        cellH, 1f, 12, font, true, "N:" + num);
            }
        }

        // Col 0: Dozen bets — each spans 4 rows
        String[] dozLabels = {
            getString(R.string.roulette_first_dozen),
            getString(R.string.roulette_second_dozen),
            getString(R.string.roulette_third_dozen)
        };
        String[] dozTags = {"D1", "D2", "D3"};
        for (int i = 0; i < 3; i++) {
            addCell(grid, dozLabels[i], COLOR_CARD, 1 + i * 4, 0, 4, 1,
                    0, 0.55f, 9, font, false, dozTags[i]);
        }

        // Col 4: Column bets 2:1 — each spans 4 rows
        for (int i = 0; i < 3; i++) {
            addCell(grid, "2:1", COLOR_CARD, 1 + i * 4, 4, 4, 1,
                    0, 0.55f, 11, font, false, "C" + (i + 1));
        }

        container.addView(grid);
        buildOutsideRow(container, cellH, font);
    }

    /** Создаёт одну ячейку стола (число, дюжина или столбец) с подложкой-фишкой для отображения суммы ставки, и регистрирует обработчик клика. */
    private void addCell(GridLayout grid, String text, int bg,
                         int gridRow, int gridCol, int rowSpan, int colSpan,
                         int height, float colWeight, int textSizeSp,
                         Typeface font, boolean goldText, String betTag) {

        FrameLayout frame = new FrameLayout(this);
        frame.setClickable(true);
        frame.setFocusable(true);

        // Cell label — holds the rounded-rect background (toggled on bet)
        TextView tv = makeCell(text, bg, 0, height > 0 ? height : dp(CELL_H), font, goldText);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        tv.setMaxLines(4);
        frame.addView(tv, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Chip overlay — gold circle, top-right corner, shows bet amount
        TextView chip = makeChip(font);
        FrameLayout.LayoutParams chipLp = new FrameLayout.LayoutParams(dp(22), dp(22));
        chipLp.gravity = Gravity.TOP | Gravity.END;
        chipLp.setMargins(0, dp(3), dp(3), 0);
        frame.addView(chip, chipLp);
        chipViews.put(betTag, chip);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                GridLayout.spec(gridRow, rowSpan, GridLayout.FILL, 1f),
                GridLayout.spec(gridCol, colSpan, GridLayout.FILL, colWeight));
        lp.width  = 0;
        lp.height = height;
        lp.setMargins(2, 2, 2, 2);
        frame.setLayoutParams(lp);
        frame.setOnClickListener(v -> toggleBet(tv, betTag, bg));
        grid.addView(frame);
    }

    /** Строит нижний ряд внешних ставок: 1–18/19–36, чёт/нечёт, красное/чёрное. */
    private void buildOutsideRow(LinearLayout container, int cellH, Typeface font) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.addView(row);

        addOutsideBtn(row, "1–18",  "LOW",   COLOR_CARD,  cellH, font);
        addOutsideBtn(row, "EVEN",  "EVEN",  COLOR_CARD,  cellH, font);
        addOutsideBtn(row, "●",     "RED",   COLOR_RED,   cellH, font);
        addOutsideBtn(row, "●",     "BLACK", COLOR_BLACK, cellH, font);
        addOutsideBtn(row, "ODD",   "ODD",   COLOR_CARD,  cellH, font);
        addOutsideBtn(row, "19–36", "HIGH",  COLOR_CARD,  cellH, font);
    }

    /** Создаёт одну кнопку внешней ставки нижнего ряда с фишкой-оверлеем для отображения суммы. */
    private void addOutsideBtn(LinearLayout row, String label, String tag,
                                int color, int cellH, Typeface font) {
        FrameLayout frame = new FrameLayout(this);
        frame.setClickable(true);
        frame.setFocusable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, cellH, 1f);
        lp.setMargins(2, 2, 2, 2);
        frame.setLayoutParams(lp);

        TextView btn = makeCell(label, color, 0, cellH, font, false);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        frame.addView(btn, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        TextView chip = makeChip(font);
        FrameLayout.LayoutParams chipLp = new FrameLayout.LayoutParams(dp(22), dp(22));
        chipLp.gravity = Gravity.TOP | Gravity.END;
        chipLp.setMargins(0, dp(3), dp(3), 0);
        frame.addView(chip, chipLp);
        chipViews.put(tag, chip);

        frame.setOnClickListener(v -> toggleBet(btn, tag, color));
        row.addView(frame);
    }



    // ════════════════════════════════════════════════════════════════════
    //  Multi-bet logic
    // ════════════════════════════════════════════════════════════════════

    /** Переключает ставку на ячейке: если ставка уже стоит — снимает её, иначе считывает сумму из поля ввода и добавляет новую ставку с подсветкой и фишкой. */
    private void toggleBet(View v, String betTag, int bgColor) {
        if (tableBets.containsKey(betTag)) {
            // Remove bet: restore cell bg, hide chip
            tableBets.remove(betTag);
            betViews.remove(betTag);
            betBgColors.remove(betTag);
            v.setBackground(roundedBg(bgColor, false));
            TextView chip = chipViews.get(betTag);
            if (chip != null) chip.setVisibility(View.GONE);
            updateBetsLabel();
            return;
        }

        // Parse amount from field
        String rawAmount = bet.getText().toString().trim();
        if (rawAmount.isEmpty()) {
            tilBetField.setError(ENTER_AMOUNT_FIRST);
            return;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(rawAmount);
        } catch (NumberFormatException e) {
            tilBetField.setError(PAYMENT_AMOUNT_IS_INCORRECT);
            return;
        }

        // Place bet: highlight cell, show chip with amount
        tableBets.put(betTag, amount);
        betViews.put(betTag, v);
        betBgColors.put(betTag, bgColor);
        v.setBackground(roundedBg(bgColor, true));

        TextView chip = chipViews.get(betTag);
        if (chip != null) {
            chip.setText(amount.stripTrailingZeros().toPlainString());
            chip.setVisibility(View.VISIBLE);
        }
        tilBetField.setError(null);
        updateBetsLabel();
    }

    /** Обновляет текстовую метку над столом: количество ставок и их суммарную сумму, либо подсказку выбрать ставку, если их нет. */
    private void updateBetsLabel() {
        if (tableBets.isEmpty()) {
            selectedBetLabel.setText(getString(R.string.roulette_select_a_bet));
            selectedBetLabel.setTextColor(0xFFAAAAAA);
            if (btnClearBets != null) btnClearBets.setVisibility(View.GONE);
            return;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal a : tableBets.values()) total = total.add(a);
        selectedBetLabel.setText(
                getString(R.string.roulette_bets_summary, tableBets.size(),
                        total.stripTrailingZeros().toPlainString()));
        selectedBetLabel.setTextColor(COLOR_GOLD);
        if (btnClearBets != null) btnClearBets.setVisibility(View.VISIBLE);
    }

    /** Снимает все ставки со стола: возвращает обычный фон ячейкам, прячет фишки и сбрасывает накопленное состояние ставок. */
    private void clearAllBets() {
        for (Map.Entry<String, View> entry : betViews.entrySet()) {
            Integer bgColor = betBgColors.get(entry.getKey());
            if (bgColor != null) entry.getValue().setBackground(roundedBg(bgColor, false));
            TextView chip = chipViews.get(entry.getKey());
            if (chip != null) chip.setVisibility(View.GONE);
        }
        tableBets.clear();
        betViews.clear();
        betBgColors.clear();
        pendingPrimaryTag        = null;
        pendingPrimaryMultiplier = 2;
        updateBetsLabel();
    }



    // ════════════════════════════════════════════════════════════════════
    //  Listeners
    // ════════════════════════════════════════════════════════════════════

    /** Назначает обработчики: быстрый выбор суммы по чипам, сброс ошибки при правке поля, переход к правилам, очистку ставок и запуск спина со всеми текущими ставками. */
    private void listeners() {
        chipGroupAmounts.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if      (checkedIds.contains(R.id.chip_01_xrp)) bet.setText("0.1");
            else if (checkedIds.contains(R.id.chip_05_xrp)) bet.setText("0.5");
            else if (checkedIds.contains(R.id.chip_1_xrp))  bet.setText("1");
            else if (checkedIds.contains(R.id.chip_5_xrp))  bet.setText("5");
            else if (checkedIds.contains(R.id.chip_10_xrp)) bet.setText("10");
            else if (checkedIds.contains(R.id.chip_20_xrp)) bet.setText("20");
        });

        bet.addTextChangedListener(new android.text.TextWatcher() {
            private boolean editing = false;
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                tilBetField.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {
                if (editing) return;
                String text = s.toString();
                int dot = text.indexOf('.');
                if (dot >= 0 && text.length() > dot + 2) {
                    editing = true;
                    s.replace(0, s.length(), text.substring(0, dot + 2));
                    editing = false;
                }
            }
        });

        rulesInfo.setOnClickListener(v -> {
            pulse(v);
            startActivity(new Intent(RULES_OF_THE_GAME_ROULETTE_CLASS));
        });


        if (btnClearBets != null) {
            btnClearBets.setOnClickListener(v -> {
                pulse(v);
                clearAllBets();
            });
        }

        btnSpin.setOnClickListener(v -> {
            if (tableBets.isEmpty()) {
                tilBetField.setError(getString(R.string.roulette_select_a_bet));
                return;
            }
            pendingPrimaryTag        = tableBets.keySet().iterator().next();
            pendingPrimaryMultiplier = RouletteBetCode.multiplierForTag(pendingPrimaryTag);

            pulse(v);
            setSpinningState(true);
            soundPool.playBet(this);
            viewModel.placeBets(new LinkedHashMap<>(tableBets), myReferral);
        });

        updateBetsLabel();
    }



    // ════════════════════════════════════════════════════════════════════
    //  View / drawable helpers
    // ════════════════════════════════════════════════════════════════════

    /** Создаёт TextView ячейки стола со скруглённым фоном нужного цвета и стилем текста (золотой для зеро, белый для остальных). */
    private TextView makeCell(String text, int bg, int w, int h, Typeface font, boolean goldText) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(goldText ? COLOR_GOLD : 0xFFFFFFFF);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setTypeface(font, Typeface.BOLD);
        tv.setBackground(roundedBg(bg, false));
        if (w > 0) tv.setMinWidth(w);
        tv.setMinHeight(h);
        return tv;
    }

    /** Создаёт скрытую по умолчанию TextView-фишку (золотой кружок) для отображения суммы ставки на ячейке. */
    private TextView makeChip(Typeface font) {
        TextView chip = new TextView(this);
        chip.setVisibility(View.GONE);
        chip.setGravity(Gravity.CENTER);
        chip.setTextColor(0xFF1A1A2E);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 7);
        chip.setTypeface(font, Typeface.BOLD);
        chip.setSingleLine(true);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(COLOR_GOLD);
        bg.setStroke(dp(1), 0xCC000000);
        chip.setBackground(bg);
        return chip;
    }

    /** Создаёт скруглённый прямоугольный фон ячейки; при selected=true добавляет золотую обводку, обозначающую активную ставку. */
    private GradientDrawable roundedBg(int color, boolean selected) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(color);
        d.setCornerRadius(dp(4));
        d.setStroke(dp(selected ? 2 : 1), selected ? COLOR_GOLD : 0x33FFFFFF);
        return d;
    }



    // ════════════════════════════════════════════════════════════════════
    //  Misc helpers
    // ════════════════════════════════════════════════════════════════════

    /** Читает сохранённый реферальный код пользователя из preferences (по умолчанию "0", если не задан). */
    private void getReferral() {
        preferences = PrefsHelper.get(this);
        myReferral  = preferences.contains(StringEnum.APP_PREFERENCES_REFERRAL.getValue())
                ? preferences.getString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), "0")
                : "0";
    }

    /** Переводит значение dp в пиксели для текущей плотности экрана. */
    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }



    /** Переключает UI между обычным состоянием и состоянием "идёт спин": блокирует кнопку спина и показывает индикатор загрузки. */
    private void setSpinningState(boolean spinning) {
        runOnUiThread(() -> {
            btnSpin.setEnabled(!spinning);
            btnSpin.setAlpha(spinning ? 0.7f : 1f);
            spinIcon.setVisibility(spinning ? View.GONE : View.VISIBLE);
            spinProgress.setVisibility(spinning ? View.VISIBLE : View.GONE);
        });
    }



    // ════════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ════════════════════════════════════════════════════════════════════

    /** При уходе с экрана приостанавливает музыку, освобождает аудиофокус и отписывается от наушников. */
    @Override
    protected void onPause() {
        super.onPause();
        if (casinoMediaPlayer != null && casinoMediaPlayer.isPlaying()) casinoMediaPlayer.pause();
        AudioHelper.abandonFocus(this, audioFocusRequest);
        AudioHelper.unregisterNoisyReceiver(this, noisyReceiver);
        audioFocusRequest = null;
        noisyReceiver = null;
    }

    /** При возвращении запрашивает аудиофокус, регистрирует приёмник наушников и запускает музыку (если не замьючено). */
    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadBalance();
        noisyReceiver = AudioHelper.registerNoisyReceiver(this,
                () -> { if (casinoMediaPlayer != null && casinoMediaPlayer.isPlaying()) casinoMediaPlayer.pause(); });
        audioFocusRequest = AudioHelper.requestFocus(this, focusListener);
        if (casinoMediaPlayer != null && AudioHelper.isSoundEnabled(this)) casinoMediaPlayer.start();
    }

    /** Останавливает фоновую музыку казино перед закрытием экрана. */
    @Override
    public void onBackPressed() {
        if (casinoMediaPlayer != null) casinoMediaPlayer.stop();
        super.onBackPressed();
    }

    /** Освобождает ресурсы MediaPlayer и SoundPool при уничтожении Activity. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (casinoMediaPlayer != null) { casinoMediaPlayer.release(); casinoMediaPlayer = null; }
        if (soundPool != null) { soundPool.release(); soundPool = null; }
    }
}
