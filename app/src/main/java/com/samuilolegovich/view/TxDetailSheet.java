package com.samuilolegovich.view;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.samuilolegovich.R;
import com.samuilolegovich.dto.HistoryPaymentDto;
import com.samuilolegovich.enums.RouletteBetCode;

import java.math.BigDecimal;
import java.math.MathContext;



public class TxDetailSheet extends BottomSheetDialogFragment {

    private static final String ARG = "dto";

    public static void show(FragmentManager fm, HistoryPaymentDto dto) {
        TxDetailSheet sheet = new TxDetailSheet();
        Bundle b = new Bundle();
        b.putSerializable(ARG, dto);
        sheet.setArguments(b);
        sheet.show(fm, "tx_detail");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_tx_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dark background for the bottom sheet container itself
        View parent = (View) view.getParent();
        if (parent != null) {
            parent.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.xura_surface));
        }

        HistoryPaymentDto dto = (HistoryPaymentDto) requireArguments().getSerializable(ARG);
        if (dto == null) return;

        bindHeader(view, dto);
        bindInfoRow(view, dto);
        if (dto.getTag() != null && dto.getTag().startsWith("BET:R:")) {
            bindBetsBreakdown(view, dto.getTag());
        }
    }

    // ── Header ────────────────────────────────────────────────────────────

    private void bindHeader(View root, HistoryPaymentDto dto) {
        String tag      = dto.getTag() != null ? dto.getTag() : "";
        String amt      = dto.getAmount() != null ? dto.getAmount() : "";
        boolean incoming = amt.startsWith("+");

        ImageView icon   = root.findViewById(R.id.detail_icon);
        TextView title   = root.findViewById(R.id.detail_title);
        TextView address = root.findViewById(R.id.detail_address);
        TextView amount  = root.findViewById(R.id.detail_amount);
        TextView time    = root.findViewById(R.id.detail_time);

        // Icon resource + tint colour
        int iconRes  = resolveIconRes(tag, incoming);
        int iconTint = resolveTypeColor(tag, incoming);
        icon.setImageResource(iconRes);
        ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(iconTint));

        title.setText(resolveTitle(tag));
        title.setTextColor(iconTint);

        address.setText(truncate(dto.getAddress()));

        int amtColor = incoming
                ? ContextCompat.getColor(requireContext(), R.color.xura_success)
                : ContextCompat.getColor(requireContext(), R.color.xura_pink);
        amount.setText(amt);
        amount.setTextColor(amtColor);

        String t = dto.getTime();
        if (t != null && !t.isEmpty()) {
            time.setText(t);
            time.setVisibility(View.VISIBLE);
        } else {
            time.setVisibility(View.GONE);
        }
    }

    // ── Info row (destination tag or memo label for single-bet types) ─────

    private void bindInfoRow(View root, HistoryPaymentDto dto) {
        String tag = dto.getTag() != null ? dto.getTag() : "";

        // Show numeric destination tag
        boolean isNumericTag = !tag.isEmpty() && tag.matches("\\d+");
        // Show memo content for types we can't fully decode
        boolean isRawMemo = !tag.isEmpty()
                && !tag.startsWith("BET:")
                && !tag.equals("WIN")   && !tag.startsWith("WIN:")
                && !tag.equals("LOSE")  && !tag.startsWith("LOSE:")
                && !tag.equals("JKPT")  && !tag.startsWith("JKPT:")
                && !tag.equals("RFD")   && !tag.startsWith("RFD:")
                && !tag.equals("DON")   && !tag.startsWith("DON:")
                && !tag.equals("REF")   && !tag.startsWith("REF:")
                && !tag.equals("---")
                && !isNumericTag;

        LinearLayout section = root.findViewById(R.id.detail_info_section);
        TextView label = root.findViewById(R.id.detail_info_label);
        TextView value = root.findViewById(R.id.detail_info_value);

        if (isNumericTag) {
            section.setVisibility(View.VISIBLE);
            label.setText(getString(R.string.detail_dest_tag));
            value.setText(tag);
        } else if (isRawMemo) {
            section.setVisibility(View.VISIBLE);
            label.setText(getString(R.string.detail_memo));
            value.setText(tag);
        } else {
            section.setVisibility(View.GONE);
        }
    }

    // ── Bets breakdown (BET:R: only) ─────────────────────────────────────

    private void bindBetsBreakdown(View root, String tag) {
        // Format: BET:R:n5@1.5,r@2.0,d1@0.5:referralCode
        String[] parts = tag.split(":", -1);
        if (parts.length < 3) return;

        String betsStr = parts[2];
        String[] bets  = betsStr.split(",");

        LinearLayout section   = root.findViewById(R.id.detail_bets_section);
        LinearLayout container = root.findViewById(R.id.detail_bets_container);
        TextView     totalView = root.findViewById(R.id.detail_bets_total);

        section.setVisibility(View.VISIBLE);

        BigDecimal total = BigDecimal.ZERO;
        Typeface font = Typeface.DEFAULT;

        for (String bet : bets) {
            String[] pair = bet.split("@", 2);
            if (pair.length != 2) continue;
            String code = pair[0].trim();
            BigDecimal betAmt;
            try {
                betAmt = new BigDecimal(pair[1].trim());
            } catch (NumberFormatException e) {
                continue;
            }
            total = total.add(betAmt);

            String fullTag    = RouletteBetCode.codeToTag(code);
            String betName    = tagToDisplayName(fullTag);
            int    multiplier = RouletteBetCode.multiplierForTag(fullTag);

            container.addView(makeBetRow(betName, betAmt, multiplier, font));
        }

        totalView.setText(total.stripTrailingZeros().toPlainString() + " XRP");
    }

    private View makeBetRow(String name, BigDecimal amount, int multiplier, Typeface font) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(32));
        row.setLayoutParams(rowLp);

        int textPrimary   = ContextCompat.getColor(requireContext(), R.color.xura_text_primary);
        int textSecondary = ContextCompat.getColor(requireContext(), R.color.xura_text_secondary);
        int goldColor     = ContextCompat.getColor(requireContext(), R.color.xura_gold);

        // Bet name
        TextView tvName = new TextView(requireContext());
        tvName.setText(name);
        tvName.setTextColor(textPrimary);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvName.setTypeface(font);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvName.setLayoutParams(nameLp);
        row.addView(tvName);

        // Amount
        TextView tvAmt = new TextView(requireContext());
        tvAmt.setText(amount.stripTrailingZeros().toPlainString() + " XRP");
        tvAmt.setTextColor(textSecondary);
        tvAmt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvAmt.setTypeface(font);
        tvAmt.setGravity(Gravity.END);
        LinearLayout.LayoutParams amtLp = new LinearLayout.LayoutParams(dp(80),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tvAmt.setLayoutParams(amtLp);
        row.addView(tvAmt);

        // Multiplier
        TextView tvMult = new TextView(requireContext());
        tvMult.setText(getString(R.string.detail_multiplier_fmt, multiplier));
        tvMult.setTextColor(goldColor);
        tvMult.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvMult.setTypeface(font, Typeface.BOLD);
        tvMult.setGravity(Gravity.END);
        LinearLayout.LayoutParams multLp = new LinearLayout.LayoutParams(dp(44),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tvMult.setLayoutParams(multLp);
        row.addView(tvMult);

        return row;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String resolveTitle(String tag) {
        if (tag.startsWith("BET:R:"))  return getString(R.string.roulette_bet_history);
        if (tag.startsWith("BET:RED")) return getString(R.string.bet_on_red_history);
        if (tag.startsWith("BET:BLK")) return getString(R.string.bet_on_black_history);
        if (tag.startsWith("BET:N:"))  {
            String[] p = tag.split(":");
            return getString(R.string.bet_on_history) + " " + (p.length > 2 ? p[2] : "?");
        }
        if (tag.equals("WIN")  || tag.startsWith("WIN:"))  return getString(R.string.bet_won_history);
        if (tag.equals("LOSE") || tag.startsWith("LOSE:")) return getString(R.string.bet_lost_history);
        if (tag.equals("JKPT") || tag.startsWith("JKPT:")) return getString(R.string.won_the_lotto_history);
        if (tag.equals("RFD")  || tag.startsWith("RFD:"))  return getString(R.string.refund_history);
        if (tag.equals("DON")  || tag.startsWith("DON:"))  return getString(R.string.donation_history);
        if (tag.equals("REF:REC"))                          return getString(R.string.referral_recovery_history);
        if (tag.equals("REF")  || tag.startsWith("REF:"))  return getString(R.string.referral_order_history);
        return getString(R.string.tag_history) + " " + tag;
    }

    private int resolveTypeColor(String tag, boolean incoming) {
        if (tag.equals("WIN")   || tag.startsWith("WIN:"))  return ContextCompat.getColor(requireContext(), R.color.xura_success);
        if (tag.equals("LOSE")  || tag.startsWith("LOSE:")) return ContextCompat.getColor(requireContext(), R.color.xura_error);
        if (tag.equals("JKPT")  || tag.startsWith("JKPT:")) return ContextCompat.getColor(requireContext(), R.color.xura_gold);
        if (tag.startsWith("BET:R:"))                        return ContextCompat.getColor(requireContext(), R.color.xura_pink);
        if (tag.startsWith("BET:RED"))                       return ContextCompat.getColor(requireContext(), R.color.xura_pink);
        if (tag.startsWith("BET:BLK"))                       return ContextCompat.getColor(requireContext(), R.color.xura_text_secondary);
        if (tag.startsWith("BET:N:"))                        return ContextCompat.getColor(requireContext(), R.color.xura_cyan);
        if (tag.equals("RFD")   || tag.startsWith("RFD:"))  return ContextCompat.getColor(requireContext(), R.color.xura_cyan);
        if (tag.equals("DON")   || tag.startsWith("DON:"))  return ContextCompat.getColor(requireContext(), R.color.xura_gold);
        if (tag.equals("REF")   || tag.startsWith("REF:"))  return ContextCompat.getColor(requireContext(), R.color.xura_gold);
        return incoming
                ? ContextCompat.getColor(requireContext(), R.color.xura_cyan)
                : ContextCompat.getColor(requireContext(), R.color.xura_pink);
    }

    private int resolveIconRes(String tag, boolean incoming) {
        if (tag.equals("WIN")   || tag.startsWith("WIN:"))  return R.drawable.ic_check_circle;
        if (tag.equals("LOSE")  || tag.startsWith("LOSE:")) return R.drawable.ic_lost_x;
        if (tag.equals("JKPT")  || tag.startsWith("JKPT:")) return R.drawable.ic_bolt;
        if (tag.startsWith("BET:R:"))                        return R.drawable.ic_roulette_wheel;
        if (tag.startsWith("BET:RED"))                       return R.drawable.ic_favorite;
        if (tag.startsWith("BET:BLK"))                       return R.drawable.ic_clubs;
        if (tag.startsWith("BET:N:"))                        return R.drawable.ic_target;
        if (tag.equals("RFD")   || tag.startsWith("RFD:"))  return R.drawable.ic_restore;
        if (tag.equals("DON")   || tag.startsWith("DON:"))  return R.drawable.ic_favorite;
        if (tag.equals("REF:REC"))                           return R.drawable.ic_referral_restore;
        if (tag.equals("REF")   || tag.startsWith("REF:"))  return R.drawable.ic_referral;
        return incoming ? R.drawable.ic_receive_arrow : R.drawable.ic_send_arrow;
    }

    private String tagToDisplayName(String fullTag) {
        if (fullTag.startsWith("N:")) return getString(R.string.detail_bet_number, fullTag.substring(2));
        switch (fullTag) {
            case "RED":   return getString(R.string.detail_bet_red);
            case "BLACK": return getString(R.string.detail_bet_black);
            case "ODD":   return getString(R.string.detail_bet_odd);
            case "EVEN":  return getString(R.string.detail_bet_even);
            case "LOW":   return getString(R.string.detail_bet_low);
            case "HIGH":  return getString(R.string.detail_bet_high);
            case "D1":    return getString(R.string.detail_bet_dozen, 1);
            case "D2":    return getString(R.string.detail_bet_dozen, 2);
            case "D3":    return getString(R.string.detail_bet_dozen, 3);
            case "C1":    return getString(R.string.detail_bet_column, 1);
            case "C2":    return getString(R.string.detail_bet_column, 2);
            case "C3":    return getString(R.string.detail_bet_column, 3);
            default:      return fullTag;
        }
    }

    private String truncate(String addr) {
        if (addr == null || addr.length() <= 14) return addr;
        return addr.substring(0, 6) + "…" + addr.substring(addr.length() - 4);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
