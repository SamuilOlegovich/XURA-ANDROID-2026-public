package com.samuilolegovich.assistants;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.dto.HistoryPaymentDto;



/**
 * Адаптер RecyclerView для списка истории платежей.
 * Раскрашивает строки по типу операции:
 *   - победа     → зелёный (иконка + метка + индикатор)
 *   - проигрыш   → красный
 *   - ставка     → золотой / циановый (по игре)
 *   - прочее     → по направлению перевода
 */
public class HistoryPaymentAdapter extends ListAdapter<HistoryPaymentDto, HistoryPaymentAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(HistoryPaymentDto dto);
    }

    private OnItemClickListener clickListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        this.clickListener = l;
    }

    private static final DiffUtil.ItemCallback<HistoryPaymentDto> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<HistoryPaymentDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull HistoryPaymentDto old, @NonNull HistoryPaymentDto n) {
                    return old.getAddress().equals(n.getAddress()) && old.getTag().equals(n.getTag());
                }

                @Override
                public boolean areContentsTheSame(@NonNull HistoryPaymentDto old, @NonNull HistoryPaymentDto n) {
                    return old.getAddress().equals(n.getAddress())
                            && old.getAmount().equals(n.getAmount())
                            && old.getTag().equals(n.getTag());
                }
            };

    // Локализованные метки (загружаются один раз в конструкторе)
    private final String referralRecoveryHistory;
    private final String referralOrderHistory;
    private final String wonTheLottoHistory;
    private final String betOnBlackHistory;
    private final String betOnRedHistory;
    private final String donationHistory;
    private final String referralHistory;
    private final String betLostHistory;
    private final String betWonHistory;
    private final String refundHistory;
    private final String betOnHistory;
    private final String tagHistory;
    private final String rouletteBetHistory;
    private final String rouletteWonHistory;
    private final String rouletteLostHistory;
    private final String slotBetHistory;
    private final String slotWonHistory;
    private final String slotLostHistory;



    public HistoryPaymentAdapter(Context context) {
        super(DIFF_CALLBACK);
        Resources res = getResourcesForLocale(context);
        referralRecoveryHistory = res.getString(R.string.referral_recovery_history);
        referralOrderHistory    = res.getString(R.string.referral_order_history);
        wonTheLottoHistory      = res.getString(R.string.won_the_lotto_history);
        betOnBlackHistory       = res.getString(R.string.bet_on_black_history);
        betOnRedHistory         = res.getString(R.string.bet_on_red_history);
        donationHistory         = res.getString(R.string.donation_history);
        referralHistory         = res.getString(R.string.referral_history);
        betLostHistory          = res.getString(R.string.bet_lost_history);
        betWonHistory           = res.getString(R.string.bet_won_history);
        refundHistory           = res.getString(R.string.refund_history);
        betOnHistory            = res.getString(R.string.bet_on_history);
        tagHistory              = res.getString(R.string.tag_history);
        rouletteBetHistory      = res.getString(R.string.roulette_bet_history);
        rouletteWonHistory      = res.getString(R.string.roulette_won_history);
        rouletteLostHistory     = res.getString(R.string.roulette_lost_history);
        slotBetHistory          = res.getString(R.string.slot_bet_history);
        slotWonHistory          = res.getString(R.string.slot_won_history);
        slotLostHistory         = res.getString(R.string.slot_lost_history);
    }



    private static Resources getResourcesForLocale(Context context) {
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(MainActivity.newLocale);
        return new Resources(context.getAssets(), context.getResources().getDisplayMetrics(), config);
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.table, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryPaymentDto item = getItem(position);
        Context ctx = holder.itemView.getContext();

        String tag = item.getTag();
        String amt = item.getAmount();
        boolean incoming = amt.startsWith("+");

        // Сумма: зелёная для входящих, розовая для исходящих
        int amountColor = incoming
                ? ContextCompat.getColor(ctx, R.color.xura_success)
                : ContextCompat.getColor(ctx, R.color.xura_pink);
        holder.amount.setText(amt);
        holder.amount.setTextColor(amountColor);

        // Единый цвет для иконки, метки и индикатора
        int typeColor = getTypeColor(ctx, tag);

        holder.label.setText(getDisplayLabel(tag));
        holder.label.setTextColor(typeColor);

        holder.address.setText(truncateAddress(item.getAddress()));

        String time = item.getTime();
        holder.time.setText(time);
        holder.time.setVisibility(time != null && !time.isEmpty() ? View.VISIBLE : View.GONE);

        holder.icon.setImageResource(getIconRes(tag));
        ImageViewCompat.setImageTintList(holder.icon, ColorStateList.valueOf(typeColor));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(item);
        });
    }



    // ─── Метка типа ──────────────────────────────────────────────────────────

    private String getDisplayLabel(String tag) {
        return processTag(tag).trim();
    }

    private String processTag(String tag) {
        // Слот: SLOT:{p0},{p1},{p2}:WIN|LOSE  или  SLOT:{ref} (ставка)
        if (tag.startsWith("SLOT:")) {
            if (tag.contains(",") && tag.endsWith(":WIN"))  return slotWonHistory;
            if (tag.contains(",") && tag.endsWith(":LOSE")) return slotLostHistory;
            return slotBetHistory;
        }

        // Рулетка: RLT:{num}:WIN|LOSE  или  RLT:{bets@}:{ref} (ставка)  или  RLT:LOTTO:...
        if (tag.startsWith("RLT:")) {
            if (tag.contains("LOTTO"))         return wonTheLottoHistory;
            if (tag.endsWith(":WIN"))          return rouletteWonHistory;
            if (tag.endsWith(":LOSE"))         return rouletteLostHistory;
            if (tag.startsWith("BET:RED"))     return betOnRedHistory;
            if (tag.startsWith("BET:BLK"))     return betOnBlackHistory;
            return rouletteBetHistory;
        }

        if (tag.startsWith("BET:RED"))  return betOnRedHistory;
        if (tag.startsWith("BET:BLK"))  return betOnBlackHistory;

        if (tag.startsWith("BET:N:")) {
            String[] parts = tag.split(":");
            return betOnHistory + " " + (parts.length > 2 ? parts[2] : "?");
        }

        if (tag.equals("WIN")  || tag.startsWith("WIN:"))  return betWonHistory;
        if (tag.equals("LOSE") || tag.startsWith("LOSE:")) return betLostHistory;
        if (tag.equals("JKPT") || tag.startsWith("JKPT:")) return wonTheLottoHistory;
        if (tag.equals("DON")  || tag.startsWith("DON:"))  return donationHistory;
        if (tag.equals("RFD")  || tag.startsWith("RFD:"))  return refundHistory;
        if (tag.equals("REF"))                             return referralOrderHistory;
        if (tag.equals("REF:REC"))                         return referralRecoveryHistory;
        if (tag.startsWith("REF:"))                        return referralHistory + " " + truncateAddress(tag.substring(4));

        return tagHistory + " " + tag;
    }


    // ─── Единый цвет: иконка, метка и индикатор ──────────────────────────────

    private int getTypeColor(Context ctx, String tag) {
        // Слот
        if (tag.startsWith("SLOT:")) {
            if (tag.contains(",") && tag.endsWith(":WIN"))  return ContextCompat.getColor(ctx, R.color.xura_success);
            if (tag.contains(",") && tag.endsWith(":LOSE")) return ContextCompat.getColor(ctx, R.color.xura_purple);
            return ContextCompat.getColor(ctx, R.color.xura_magenta); // ставка в слот — цвет кнопки слота
        }

        // Рулетка и прочие RLT
        if (tag.startsWith("RLT:")) {
            if (tag.contains("LOTTO"))    return ContextCompat.getColor(ctx, R.color.xura_gold);
            if (tag.endsWith(":WIN"))     return ContextCompat.getColor(ctx, R.color.xura_success);
            if (tag.endsWith(":LOSE"))    return ContextCompat.getColor(ctx, R.color.xura_purple);
            return ContextCompat.getColor(ctx, R.color.xura_gold); // ставка на рулетку — золотая
        }

        if (tag.startsWith("BET:RED"))                        return ContextCompat.getColor(ctx, R.color.xura_pink);
        if (tag.startsWith("BET:BLK"))                        return ContextCompat.getColor(ctx, R.color.xura_text_secondary);
        if (tag.startsWith("BET:N:"))                         return ContextCompat.getColor(ctx, R.color.xura_cyan);

        if (tag.equals("WIN")  || tag.startsWith("WIN:"))     return ContextCompat.getColor(ctx, R.color.xura_success);
        if (tag.equals("LOSE") || tag.startsWith("LOSE:"))    return ContextCompat.getColor(ctx, R.color.xura_purple);
        if (tag.equals("JKPT") || tag.startsWith("JKPT:"))    return ContextCompat.getColor(ctx, R.color.xura_gold);
        if (tag.equals("RFD")  || tag.startsWith("RFD:"))     return ContextCompat.getColor(ctx, R.color.xura_cyan);
        if (tag.equals("DON")  || tag.startsWith("DON:"))     return ContextCompat.getColor(ctx, R.color.xura_gold);
        if (tag.equals("REF")  || tag.startsWith("REF:"))     return ContextCompat.getColor(ctx, R.color.xura_gold);

        return ContextCompat.getColor(ctx, R.color.xura_text_secondary);
    }


    // ─── Иконка ──────────────────────────────────────────────────────────────

    private int getIconRes(String tag) {
        // Слот
        if (tag.startsWith("SLOT:")) {
            if (tag.contains(",") && tag.endsWith(":WIN"))  return R.drawable.ic_check_circle;
            if (tag.contains(",") && tag.endsWith(":LOSE")) return R.drawable.ic_lost_x;
            return R.drawable.ic_slot_machine_outline;
        }

        // Рулетка
        if (tag.startsWith("RLT:")) {
            if (tag.contains("LOTTO"))   return R.drawable.ic_bolt;
            if (tag.endsWith(":WIN"))    return R.drawable.ic_check_circle;
            if (tag.endsWith(":LOSE"))   return R.drawable.ic_lost_x;
            return R.drawable.ic_roulette_outline;
        }

        if (tag.startsWith("BET:RED"))                        return R.drawable.ic_favorite;
        if (tag.startsWith("BET:BLK"))                        return R.drawable.ic_clubs;
        if (tag.startsWith("BET:N:"))                         return R.drawable.ic_target;

        if (tag.equals("WIN")  || tag.startsWith("WIN:"))     return R.drawable.ic_check_circle;
        if (tag.equals("LOSE") || tag.startsWith("LOSE:"))    return R.drawable.ic_lost_x;
        if (tag.equals("JKPT") || tag.startsWith("JKPT:"))    return R.drawable.ic_bolt;
        if (tag.equals("RFD")  || tag.startsWith("RFD:"))     return R.drawable.ic_restore;
        if (tag.equals("DON")  || tag.startsWith("DON:"))     return R.drawable.ic_favorite;
        if (tag.equals("REF:REC"))                            return R.drawable.ic_referral_restore;
        if (tag.equals("REF")  || tag.startsWith("REF:"))     return R.drawable.ic_referral;

        return R.drawable.ic_receive_arrow;
    }


    // ─── Утилиты ─────────────────────────────────────────────────────────────

    private String truncateAddress(String addr) {
        if (addr == null || addr.length() <= 14) return addr;
        return addr.substring(0, 6) + "…" + addr.substring(addr.length() - 4);
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView  label;
        final TextView  address;
        final TextView  amount;
        final TextView  time;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon      = itemView.findViewById(R.id.ic_tx_type);
            label     = itemView.findViewById(R.id.tx_label);
            address   = itemView.findViewById(R.id.address);
            amount    = itemView.findViewById(R.id.amount_field);
            time      = itemView.findViewById(R.id.tx_time);
        }
    }
}
