package com.samuilolegovich.assistants;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.dto.HistoryPaymentDto;



public class HistoryPaymentAdapter extends ListAdapter<HistoryPaymentDto, HistoryPaymentAdapter.ViewHolder> {

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
        holder.address.setText(item.getAddress());
        holder.amount.setText(item.getTag().startsWith("-")
                ? item.getAmount()
                : item.getAmount() + processTag(item.getTag()));
    }



    private String processTag(String tag) {
        if (tag.startsWith("BET:RED")) return " " + betOnRedHistory;
        if (tag.startsWith("BET:BLK")) return " " + betOnBlackHistory;

        if (tag.startsWith("BET:N:")) {
            String[] parts = tag.split(":");
            return " " + betOnHistory + " " + (parts.length > 2 ? parts[2] : "?");
        }

        if (tag.equals("WIN")  || tag.startsWith("WIN:"))  return " " + betWonHistory;
        if (tag.equals("LOSE") || tag.startsWith("LOSE:")) return " " + betLostHistory;
        if (tag.equals("JKPT") || tag.startsWith("JKPT:")) return " " + wonTheLottoHistory;
        if (tag.equals("DON")  || tag.startsWith("DON:"))  return " " + donationHistory;
        if (tag.equals("RFD")  || tag.startsWith("RFD:"))  return " " + refundHistory;
        if (tag.equals("REF"))     return " " + referralOrderHistory;
        if (tag.equals("REF:REC")) return " " + referralRecoveryHistory;
        if (tag.startsWith("REF:")) return " " + referralHistory + " " + tag.substring(4);

        return " " + tagHistory + " " + tag;
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView address;
        final TextView amount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.address);
            amount  = itemView.findViewById(R.id.amount_field);
        }
    }
}
