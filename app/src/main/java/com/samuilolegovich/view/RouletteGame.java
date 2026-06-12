package com.samuilolegovich.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.viewmodel.RouletteViewModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static com.samuilolegovich.view.Flasher.FLASHER_CLASS;
import static com.samuilolegovich.view.RulesOfTheGameRoulette.RULES_OF_THE_GAME_ROULETTE_CLASS;
import dagger.hilt.android.AndroidEntryPoint;



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
    private MediaPlayer rouletteSpinMediaPlayer;
    private MediaPlayer winMediaPlayer;
    private MediaPlayer lostMediaPlayer;
    private MediaPlayer errorMediaPlayer;
    private MediaPlayer betMediaPlayer;

    // ── Animation ────────────────────────────────────────────────────────
    private Animation animTranslate;
    private Animation animSlideInDown;

    // ── Current bet selection ────────────────────────────────────────────
    private View   selectedView;
    private String selectedBetTag;
    private int    selectedWinMultiplier;
    private int    selectedBgColor;

    // ── Error strings ────────────────────────────────────────────────────
    private String YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND;
    private String IT_IS_NOT_POSSIBLE_TO_SEND_NULL;
    private String BET_IS_MADE_EXPECT_THE_RESULT;
    private String PAYMENT_AMOUNT_IS_INCORRECT;
    private String WRONG_DESTINATION_ADDRESS;
    private String BET_CANNOT_BE_MORE_THAN;
    private String BET_CANNOT_BE_LESS_THAN;

    // ── Views ────────────────────────────────────────────────────────────
    private MaterialCardView   wheelCard;
    private RouletteWheelView  wheelView;
    private LinearLayout       resultLayout;
    private TextView           resultNumberText;
    private TextView           resultMessageText;

    private TextView        rulesInfo;
    private TextView        balance;
    private TextView        selectedBetLabel;
    private EditText        bet;
    private ChipGroup       chipGroupAmounts;
    private TextInputLayout tilBetField;
    private MaterialButton  btnSpin;



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
                balance.setText(b.toString() + "  XRP"));

        viewModel.getError().observe(this, error -> {
            if (error == null) return;
            errorMediaPlayer.start();
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

        viewModel.getBetSuccess().observe(this, preparedAmount -> {
            if (preparedAmount == null) return;
            tilBetField.setError(null);
            bet.setText("");

            if (Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
                setBetParamsForFlasher(preparedAmount);
                startActivity(new Intent(FLASHER_CLASS));
                showToast(BET_IS_MADE_EXPECT_THE_RESULT);
            } else {
                startTestSpin(preparedAmount);
            }
        });

        viewModel.loadBalance();
    }



    // ════════════════════════════════════════════════════════════════════
    //  View setup
    // ════════════════════════════════════════════════════════════════════

    private void setViews() {
        wheelCard        = findViewById(R.id.roulette_wheel_card);
        wheelView        = findViewById(R.id.roulette_wheel_view);
        resultLayout     = findViewById(R.id.roulette_result_layout);
        resultNumberText = findViewById(R.id.roulette_result_number);
        resultMessageText = findViewById(R.id.roulette_result_message);

        rulesInfo        = findViewById(R.id.rules_of_the_game_link);
        balance          = findViewById(R.id.your_balance_xrp_text);
        selectedBetLabel = findViewById(R.id.roulette_selected_bet_label);
        bet              = findViewById(R.id.bet_field);
        tilBetField      = findViewById(R.id.til_bet_field);
        chipGroupAmounts = findViewById(R.id.chip_group_amounts);
        btnSpin          = findViewById(R.id.btn_spin_roulette);

        casinoMediaPlayer       = MediaPlayer.create(this, R.raw.in_casino);
        rouletteSpinMediaPlayer = MediaPlayer.create(this, R.raw.roulette_spin);
        winMediaPlayer          = MediaPlayer.create(this, R.raw.win);
        lostMediaPlayer         = MediaPlayer.create(this, R.raw.lost);
        errorMediaPlayer        = MediaPlayer.create(this, R.raw.error);
        betMediaPlayer          = MediaPlayer.create(this, R.raw.bet);

        casinoMediaPlayer.setVolume(0.4f, 0.4f);
        casinoMediaPlayer.setLooping(true);
        casinoMediaPlayer.start();
    }

    private void setLanguage() {
        YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND = getString(R.string.your_account_is_not_enough_to_send);
        IT_IS_NOT_POSSIBLE_TO_SEND_NULL    = getString(R.string.it_is_not_possible_to_send_null);
        BET_IS_MADE_EXPECT_THE_RESULT      = getString(R.string.bet_is_made_expect_the_result);
        PAYMENT_AMOUNT_IS_INCORRECT        = getString(R.string.payment_amount_is_incorrect);
        WRONG_DESTINATION_ADDRESS          = getString(R.string.wrong_destination_address);
        BET_CANNOT_BE_MORE_THAN            = getString(R.string.bet_cannot_be_more_than);
        BET_CANNOT_BE_LESS_THAN            = getString(R.string.bet_cannot_be_less_than);
        rulesInfo.setText(R.string.rules_of_the_game);
    }



    // ════════════════════════════════════════════════════════════════════
    //  TEST MODE — inline spin
    // ════════════════════════════════════════════════════════════════════

    private void startTestSpin(String betAmount) {
        viewModel.simulateBalanceDeduct(betAmount);

        // Show wheel with slide-in animation (first spin) or just re-spin
        resultLayout.setVisibility(View.GONE);
        if (wheelCard.getVisibility() != View.VISIBLE) {
            wheelCard.setVisibility(View.VISIBLE);
            wheelCard.startAnimation(animSlideInDown);
        }

        btnSpin.setEnabled(false);
        btnSpin.setAlpha(0.5f);

        if (rouletteSpinMediaPlayer.isPlaying()) rouletteSpinMediaPlayer.pause();
        rouletteSpinMediaPlayer.seekTo(0);
        rouletteSpinMediaPlayer.start();
        wheelView.startSpinning();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            int winNumber = new Random().nextInt(37);
            boolean win   = evaluateBet(selectedBetTag, winNumber);

            wheelView.stopAtNumber(winNumber, () -> {
                if (rouletteSpinMediaPlayer.isPlaying()) {
                    rouletteSpinMediaPlayer.pause();
                    rouletteSpinMediaPlayer.seekTo(0);
                }
                showSpinResult(winNumber, win, betAmount);
                btnSpin.setEnabled(true);
                btnSpin.setAlpha(1f);
            });
        }, 3500);
    }


    private void showSpinResult(int winNumber, boolean win, String betAmount) {
        if (winNumber == 0)                      wheelView.setCenterColor(COLOR_GREEN);
        else if (BLACK_NUMS.contains(winNumber)) wheelView.setCenterColor(COLOR_BLACK);
        else                                     wheelView.setCenterColor(COLOR_RED);

        int badgeBg = winNumber == 0 ? COLOR_GREEN
                    : BLACK_NUMS.contains(winNumber) ? COLOR_BLACK : COLOR_RED;
        resultNumberText.setText(String.valueOf(winNumber));
        resultNumberText.setBackground(circleBg(badgeBg));
        resultNumberText.setTextColor(COLOR_GOLD);

        if (win) {
            double wonAmount = Double.parseDouble(betAmount) * selectedWinMultiplier;
            String formatted = formatAmount(wonAmount);
            resultMessageText.setText(getString(R.string.roulette_result_win, formatted));
            resultMessageText.setTextColor(COLOR_GOLD);
            resultLayout.setVisibility(View.VISIBLE);

            viewModel.simulateBalanceCredit(wonAmount);
            winMediaPlayer.seekTo(0);
            winMediaPlayer.start();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Win.MASSAGE = getString(R.string.roulette_result_win, formatted);
                startActivity(new Intent(Win.WIN_CLASS));
            }, 1500);
        } else {
            resultMessageText.setText(getString(R.string.roulette_result_loss));
            resultMessageText.setTextColor(0xFFFF3060);
            resultLayout.setVisibility(View.VISIBLE);
            lostMediaPlayer.seekTo(0);
            lostMediaPlayer.start();
        }

        // Scroll to top so user sees the wheel result
        View scroll = findViewById(R.id.roulette_scroll);
        if (scroll instanceof android.widget.ScrollView)
            ((android.widget.ScrollView) scroll).smoothScrollTo(0, 0);
    }


    private boolean evaluateBet(String tag, int n) {
        if (tag == null) return false;
        if (tag.startsWith("N:")) return Integer.parseInt(tag.substring(2)) == n;
        if (n == 0) return false;
        switch (tag) {
            case "RED":   return !BLACK_NUMS.contains(n);
            case "BLACK": return  BLACK_NUMS.contains(n);
            case "ODD":   return n % 2 != 0;
            case "EVEN":  return n % 2 == 0;
            case "LOW":   return n <= 18;
            case "HIGH":  return n >= 19;
            case "D1":    return n <= 12;
            case "D2":    return n >= 13 && n <= 24;
            case "D3":    return n >= 25;
            case "C1":    return n % 3 == 1;
            case "C2":    return n % 3 == 2;
            case "C3":    return n % 3 == 0;
            default:      return false;
        }
    }



    // ════════════════════════════════════════════════════════════════════
    //  REAL MODE — params for Flasher
    // ════════════════════════════════════════════════════════════════════

    private void setBetParamsForFlasher(String amount) {
        Flasher.TEST_MODE_ENUM          = TestModeEnum.ROULETTE_GAME;
        Flasher.TEST_SAND_AMOUNT        = amount;
        Flasher.ROULETTE_BET_TAG        = selectedBetTag;
        Flasher.ROULETTE_WIN_MULTIPLIER = selectedWinMultiplier;

        if (selectedBetTag != null && selectedBetTag.startsWith("N:")) {
            int num = Integer.parseInt(selectedBetTag.substring(2));
            Flasher.NUMBER_BET = String.valueOf(num);
            Flasher.COLOR_BET  = (num != 0) && !BLACK_NUMS.contains(num);
        } else {
            Flasher.NUMBER_BET = "0";
            Flasher.COLOR_BET  = "RED".equals(selectedBetTag);
        }
    }



    // ════════════════════════════════════════════════════════════════════
    //  Casino table builder — vertical layout
    // ════════════════════════════════════════════════════════════════════

    private void buildTable() {
        LinearLayout container = findViewById(R.id.roulette_table_container);
        int  cellH = dp(CELL_H);
        Typeface font = ResourcesCompat.getFont(this, R.font.montserrat);

        buildZeroCell(container, cellH, font);
        buildNumberGrid(container, cellH, font);
        buildColBetsRow(container, cellH, font);
        buildDozensRow(container, cellH, font);
        buildOutsideRows(container, cellH, font);
    }


    // "0" — full width green cell
    private void buildZeroCell(LinearLayout container, int cellH, Typeface font) {
        TextView zero = makeCell("0", COLOR_GREEN, 0, cellH, font, true);
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, cellH);
        lp.setMargins(2, 2, 2, 2);
        zero.setLayoutParams(lp);
        zero.setOnClickListener(v -> selectBet(v, "N:0", 36, COLOR_GREEN));
        container.addView(zero);
    }


    // Numbers 1–36 in a 3×12 GridLayout filling parent width
    private void buildNumberGrid(LinearLayout container, int cellH, Typeface font) {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        grid.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        for (int row = 0; row < 12; row++) {
            for (int col = 0; col < 3; col++) {
                int num = row * 3 + col + 1; // 1,2,3 / 4,5,6 / … / 34,35,36
                int bg  = BLACK_NUMS.contains(num) ? COLOR_BLACK : COLOR_RED;

                TextView cell = makeCell(String.valueOf(num), bg, 0, cellH, font, true);
                GridLayout.LayoutParams p = new GridLayout.LayoutParams(
                        GridLayout.spec(row, 1, GridLayout.FILL, 1f),
                        GridLayout.spec(col, 1, GridLayout.FILL, 1f));
                p.width  = 0;
                p.height = cellH;
                p.setMargins(2, 2, 2, 2);
                cell.setLayoutParams(p);

                final int n = num;
                final int c = bg;
                cell.setOnClickListener(v -> selectBet(v, "N:" + n, 36, c));
                grid.addView(cell);
            }
        }
        container.addView(grid);
    }


    // "2:1" column-bet buttons: C1 / C2 / C3
    private void buildColBetsRow(LinearLayout container, int cellH, Typeface font) {
        LinearLayout row = makeHRow(container);
        String[] tags = {"C1", "C2", "C3"};
        for (String tag : tags) {
            TextView btn = makeCell("2:1", COLOR_CARD, 0, cellH, font, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, cellH, 1f);
            lp.setMargins(2, 2, 2, 2);
            btn.setLayoutParams(lp);
            btn.setOnClickListener(v -> selectBet(v, tag, 3, COLOR_CARD));
            row.addView(btn);
        }
    }


    // 1st 12 / 2nd 12 / 3rd 12
    private void buildDozensRow(LinearLayout container, int cellH, Typeface font) {
        LinearLayout row = makeHRow(container);
        String[] labels = {
            getString(R.string.roulette_first_dozen),
            getString(R.string.roulette_second_dozen),
            getString(R.string.roulette_third_dozen)
        };
        String[] tags = {"D1", "D2", "D3"};
        for (int i = 0; i < 3; i++) {
            TextView btn = makeCell(labels[i], COLOR_CARD, 0, cellH, font, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, cellH, 1f);
            lp.setMargins(2, 2, 2, 2);
            btn.setLayoutParams(lp);
            final String tag = tags[i];
            btn.setOnClickListener(v -> selectBet(v, tag, 3, COLOR_CARD));
            row.addView(btn);
        }
    }


    // Outside bets: two rows of 3
    private void buildOutsideRows(LinearLayout container, int cellH, Typeface font) {
        // Row 1: 1–18 | EVEN | RED●
        String[] labels1 = {"1–18", "EVEN", "●"};
        String[] tags1   = {"LOW",  "EVEN", "RED"};
        int[]    colors1 = {COLOR_CARD, COLOR_CARD, COLOR_RED};
        addOutsideRow(container, cellH, font, labels1, tags1, colors1);

        // Row 2: 19–36 | ODD | BLACK●
        String[] labels2 = {"19–36", "ODD", "●"};
        String[] tags2   = {"HIGH",  "ODD", "BLACK"};
        int[]    colors2 = {COLOR_CARD, COLOR_CARD, COLOR_BLACK};
        addOutsideRow(container, cellH, font, labels2, tags2, colors2);
    }

    private void addOutsideRow(LinearLayout container, int cellH, Typeface font,
                               String[] labels, String[] tags, int[] colors) {
        LinearLayout row = makeHRow(container);
        for (int i = 0; i < 3; i++) {
            TextView btn = makeCell(labels[i], colors[i], 0, cellH, font, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, cellH, 1f);
            lp.setMargins(2, 2, 2, 2);
            btn.setLayoutParams(lp);
            final String tag = tags[i];
            final int c = colors[i];
            btn.setOnClickListener(v -> selectBet(v, tag, 2, c));
            row.addView(btn);
        }
    }

    // Creates a horizontal LinearLayout, adds it to container and returns it
    private LinearLayout makeHRow(LinearLayout container) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.addView(row);
        return row;
    }



    // ════════════════════════════════════════════════════════════════════
    //  Bet selection
    // ════════════════════════════════════════════════════════════════════

    private void selectBet(View v, String betTag, int multiplier, int bgColor) {
        if (selectedView != null) {
            selectedView.setBackground(roundedBg(selectedBgColor, false));
        }
        selectedView          = v;
        selectedBetTag        = betTag;
        selectedWinMultiplier = multiplier;
        selectedBgColor       = bgColor;
        v.setBackground(roundedBg(bgColor, true));

        selectedBetLabel.setText(buildBetLabel(betTag));
        selectedBetLabel.setTextColor(COLOR_GOLD);
        tilBetField.setError(null);
    }

    private String buildBetLabel(String tag) {
        if (tag.startsWith("N:")) return getString(R.string.roulette_bet_number) + " " + tag.substring(2) + "  (×36)";
        switch (tag) {
            case "RED":   return "RED  ●  (×2)";
            case "BLACK": return "BLACK  ●  (×2)";
            case "ODD":   return "ODD  (×2)";
            case "EVEN":  return "EVEN  (×2)";
            case "LOW":   return "1–18  (×2)";
            case "HIGH":  return "19–36  (×2)";
            case "D1":    return getString(R.string.roulette_first_dozen)  + "  (×3)";
            case "D2":    return getString(R.string.roulette_second_dozen) + "  (×3)";
            case "D3":    return getString(R.string.roulette_third_dozen)  + "  (×3)";
            case "C1":    return "1st Column  (×3)";
            case "C2":    return "2nd Column  (×3)";
            case "C3":    return "3rd Column  (×3)";
            default:      return tag;
        }
    }



    // ════════════════════════════════════════════════════════════════════
    //  Listeners
    // ════════════════════════════════════════════════════════════════════

    private void listeners() {
        animTranslate  = AnimationUtils.loadAnimation(this, R.anim.anim_translate);
        animSlideInDown = AnimationUtils.loadAnimation(this, R.anim.slide_in_down);

        chipGroupAmounts.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if      (checkedIds.contains(R.id.chip_1_xrp))  bet.setText("1");
            else if (checkedIds.contains(R.id.chip_5_xrp))  bet.setText("5");
            else if (checkedIds.contains(R.id.chip_10_xrp)) bet.setText("10");
        });

        bet.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                tilBetField.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        rulesInfo.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            startActivity(new Intent(RULES_OF_THE_GAME_ROULETTE_CLASS));
        });

        btnSpin.setOnClickListener(v -> {
            if (selectedBetTag == null) {
                tilBetField.setError(getString(R.string.roulette_select_a_bet));
                return;
            }
            v.startAnimation(animTranslate);
            betMediaPlayer.start();
            viewModel.placeBet(bet.getText().toString(), selectedBetTag, myReferral);
        });
    }



    // ════════════════════════════════════════════════════════════════════
    //  View / drawable helpers
    // ════════════════════════════════════════════════════════════════════

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

    private GradientDrawable roundedBg(int color, boolean selected) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(color);
        d.setCornerRadius(dp(4));
        d.setStroke(dp(selected ? 2 : 1), selected ? COLOR_GOLD : 0x33FFFFFF);
        return d;
    }

    private GradientDrawable circleBg(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        d.setStroke(dp(2), COLOR_GOLD);
        return d;
    }



    // ════════════════════════════════════════════════════════════════════
    //  Misc helpers
    // ════════════════════════════════════════════════════════════════════

    private String formatAmount(double amount) {
        if (amount == (long) amount) return String.valueOf((long) amount);
        return String.format("%.2f", amount);
    }

    private void getReferral() {
        preferences = PrefsHelper.get(this);
        myReferral  = preferences.contains(StringEnum.APP_PREFERENCES_REFERRAL.getValue())
                ? preferences.getString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), "0")
                : "0";
    }

    private void showToast(String message) {
        runOnUiThread(() -> {
            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 110);
            toast.show();
        });
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }



    // ════════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ════════════════════════════════════════════════════════════════════

    @Override
    protected void onPause() {
        super.onPause();
        if (wheelView != null) wheelView.stopSpinning();
        if (rouletteSpinMediaPlayer != null && rouletteSpinMediaPlayer.isPlaying())
            rouletteSpinMediaPlayer.pause();
    }

    @Override
    public void onBackPressed() {
        casinoMediaPlayer.stop();
        if (rouletteSpinMediaPlayer != null && rouletteSpinMediaPlayer.isPlaying())
            rouletteSpinMediaPlayer.pause();
        super.onBackPressed();
    }
}
