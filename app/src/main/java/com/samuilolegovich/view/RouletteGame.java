package com.samuilolegovich.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.viewmodel.GameBetError;
import com.samuilolegovich.viewmodel.RouletteViewModel;

import java.util.Arrays;
import java.util.HashSet;
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

    private static final int CELL_DP = 36;

    private RouletteViewModel viewModel;
    private SharedPreferences preferences;
    private MediaPlayer casinoMediaPlayer;
    private MediaPlayer errorMediaPlayer;
    private MediaPlayer betMediaPlayer;
    private Animation animTranslate;
    private String myReferral;

    // Current bet selection
    private android.view.View selectedView;
    private String selectedBetTag;
    private int selectedWinMultiplier;
    private int selectedBgColor;

    private String YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND;
    private String IT_IS_NOT_POSSIBLE_TO_SEND_NULL;
    private String BET_IS_MADE_EXPECT_THE_RESULT;
    private String PAYMENT_AMOUNT_IS_INCORRECT;
    private String WRONG_DESTINATION_ADDRESS;
    private String BET_CANNOT_BE_MORE_THAN;
    private String BET_CANNOT_BE_LESS_THAN;

    private TextView rulesInfo;
    private TextView balance;
    private TextView selectedBetLabel;
    private EditText bet;
    private ChipGroup chipGroupAmounts;
    private TextInputLayout tilBetField;
    private MaterialButton btnSpin;



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
            setBetParams(preparedAmount);
            startActivity(new Intent(FLASHER_CLASS));
            showToast(BET_IS_MADE_EXPECT_THE_RESULT);
        });

        viewModel.loadBalance();
    }



    private void setViews() {
        rulesInfo        = findViewById(R.id.rules_of_the_game_link);
        balance          = findViewById(R.id.your_balance_xrp_text);
        selectedBetLabel = findViewById(R.id.roulette_selected_bet_label);
        bet              = findViewById(R.id.bet_field);
        tilBetField      = findViewById(R.id.til_bet_field);
        chipGroupAmounts = findViewById(R.id.chip_group_amounts);
        btnSpin          = findViewById(R.id.btn_spin_roulette);

        casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);
        errorMediaPlayer  = MediaPlayer.create(this, R.raw.error);
        betMediaPlayer    = MediaPlayer.create(this, R.raw.bet);

        casinoMediaPlayer.setVolume(0.5f, 0.5f);
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


    // ── Casino table builder ────────────────────────────────────────────

    private void buildTable() {
        GridLayout grid       = findViewById(R.id.roulette_numbers_grid);
        LinearLayout dozRow   = findViewById(R.id.roulette_dozens_row);
        LinearLayout outsRow  = findViewById(R.id.roulette_outside_row);

        int cellPx = dp(CELL_DP);
        Typeface font = ResourcesCompat.getFont(this, R.font.montserrat);

        buildNumberGrid(grid, cellPx, font);
        buildDozensRow(dozRow, cellPx, font);
        buildOutsideRow(outsRow, cellPx, font);
    }


    private void buildNumberGrid(GridLayout grid, int cellPx, Typeface font) {
        // "0" cell spans all 3 rows (col 0)
        TextView zero = makeCell("0", COLOR_GREEN, cellPx, cellPx * 3, font, true);
        GridLayout.LayoutParams zp = new GridLayout.LayoutParams(
                GridLayout.spec(0, 3, 1f), GridLayout.spec(0, 1, 1f));
        zp.width  = cellPx;
        zp.height = cellPx * 3;
        zp.setMargins(2, 2, 2, 2);
        zero.setLayoutParams(zp);
        zero.setOnClickListener(v -> selectBet(v, "N:0", 36, COLOR_GREEN));
        grid.addView(zero);

        // Rows:  top=3,6,...,36  mid=2,5,...,35  bot=1,4,...,34
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 12; col++) {
                int num = (col + 1) * 3 - row; // top row: 3,6,9...  mid: 2,5,8...  bot: 1,4,7...
                int bg  = BLACK_NUMS.contains(num) ? COLOR_BLACK : COLOR_RED;

                TextView cell = makeCell(String.valueOf(num), bg, cellPx, cellPx, font, true);
                GridLayout.LayoutParams p = new GridLayout.LayoutParams(
                        GridLayout.spec(row, 1, 1f), GridLayout.spec(col + 1, 1, 1f));
                p.width  = cellPx;
                p.height = cellPx;
                p.setMargins(2, 2, 2, 2);
                cell.setLayoutParams(p);

                final int n = num;
                final int c = bg;
                cell.setOnClickListener(v -> selectBet(v, "N:" + n, 36, c));
                grid.addView(cell);
            }
        }

        // "2:1" column-bet buttons (col 13), one per row
        //   row 0 → top row numbers (3,6,...,36) → C3
        //   row 1 → mid row numbers (2,5,...,35) → C2
        //   row 2 → bot row numbers (1,4,...,34) → C1
        String[] colTags = {"C3", "C2", "C1"};
        for (int row = 0; row < 3; row++) {
            TextView cb = makeCell("2:1", COLOR_CARD, cellPx, cellPx, font, false);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams(
                    GridLayout.spec(row, 1, 1f), GridLayout.spec(13, 1, 1f));
            p.width  = cellPx;
            p.height = cellPx;
            p.setMargins(2, 2, 2, 2);
            cb.setLayoutParams(p);

            final String tag = colTags[row];
            cb.setOnClickListener(v -> selectBet(v, tag, 3, COLOR_CARD));
            grid.addView(cb);
        }
    }


    private void buildDozensRow(LinearLayout row, int cellPx, Typeface font) {
        // Left spacer aligns with the "0" column
        row.addView(spacer(cellPx + 4, cellPx));

        String[] labels = {
            getString(R.string.roulette_first_dozen),
            getString(R.string.roulette_second_dozen),
            getString(R.string.roulette_third_dozen)
        };
        String[] tags = {"D1", "D2", "D3"};

        for (int i = 0; i < 3; i++) {
            int w = cellPx * 4 - 4;
            TextView btn = makeCell(labels[i], COLOR_CARD, w, cellPx, font, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, cellPx);
            lp.setMargins(2, 2, 2, 2);
            btn.setLayoutParams(lp);
            final String tag = tags[i];
            btn.setOnClickListener(v -> selectBet(v, tag, 3, COLOR_CARD));
            row.addView(btn);
        }

        // Right spacer aligns with the "2:1" column
        row.addView(spacer(cellPx + 4, cellPx));
    }


    private void buildOutsideRow(LinearLayout row, int cellPx, Typeface font) {
        row.addView(spacer(cellPx + 4, cellPx));

        String[] labels = {"1–18", "EVEN", "●", "●", "ODD", "19–36"};
        String[] tags   = {"LOW",  "EVEN", "RED", "BLACK", "ODD", "HIGH"};
        int[]    colors = {COLOR_CARD, COLOR_CARD, COLOR_RED, COLOR_BLACK, COLOR_CARD, COLOR_CARD};

        for (int i = 0; i < 6; i++) {
            int w = cellPx * 2 - 4;
            TextView btn = makeCell(labels[i], colors[i], w, cellPx, font, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, cellPx);
            lp.setMargins(2, 2, 2, 2);
            btn.setLayoutParams(lp);
            final String tag = tags[i];
            final int c = colors[i];
            btn.setOnClickListener(v -> selectBet(v, tag, 2, c));
            row.addView(btn);
        }

        row.addView(spacer(cellPx + 4, cellPx));
    }


    // ── Cell factory ────────────────────────────────────────────────────

    private TextView makeCell(String text, int bg, int w, int h, Typeface font, boolean goldText) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(goldText ? COLOR_GOLD : 0xFFFFFFFF);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tv.setTypeface(font, Typeface.BOLD);
        tv.setBackground(roundedBg(bg, false));
        tv.setMinWidth(w);
        tv.setMinHeight(h);
        return tv;
    }

    private android.view.View spacer(int w, int h) {
        android.view.View v = new android.view.View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(w, h));
        return v;
    }


    // ── Selection ───────────────────────────────────────────────────────

    private void selectBet(android.view.View v, String betTag, int multiplier, int bgColor) {
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
        if (tag.startsWith("N:")) return getString(R.string.roulette_bet_number) + " " + tag.substring(2);
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


    // ── Drawables ───────────────────────────────────────────────────────

    private GradientDrawable roundedBg(int color, boolean selected) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(color);
        d.setCornerRadius(dp(4));
        if (selected) {
            d.setStroke(dp(2), COLOR_GOLD);
        } else {
            d.setStroke(dp(1), 0x33FFFFFF);
        }
        return d;
    }


    // ── Listeners ───────────────────────────────────────────────────────

    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        chipGroupAmounts.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if      (checkedIds.contains(R.id.chip_1_xrp))  bet.setText("1");
            else if (checkedIds.contains(R.id.chip_5_xrp))  bet.setText("5");
            else if (checkedIds.contains(R.id.chip_10_xrp)) bet.setText("10");
        });

        bet.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
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


    // ── Bet params for Flasher ──────────────────────────────────────────

    private void setBetParams(String amount) {
        Flasher.TEST_MODE_ENUM         = TestModeEnum.ROULETTE_GAME;
        Flasher.TEST_SAND_AMOUNT       = amount;
        Flasher.ROULETTE_BET_TAG       = selectedBetTag;
        Flasher.ROULETTE_WIN_MULTIPLIER = selectedWinMultiplier;

        if (selectedBetTag != null && selectedBetTag.startsWith("N:")) {
            int num = Integer.parseInt(selectedBetTag.substring(2));
            Flasher.NUMBER_BET = String.valueOf(num);
            Flasher.COLOR_BET  = num == 0 ? false : !BLACK_NUMS.contains(num);
        } else {
            Flasher.NUMBER_BET = "0";
            Flasher.COLOR_BET  = "RED".equals(selectedBetTag);
        }
    }


    // ── Helpers ─────────────────────────────────────────────────────────

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


    @Override
    public void onBackPressed() {
        casinoMediaPlayer.stop();
        super.onBackPressed();
    }
}
