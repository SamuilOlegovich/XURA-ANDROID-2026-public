package com.samuilolegovich.assistants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.dto.HistoryPaymentDto;

import java.util.ArrayList;
import java.util.List;



public class HistoryPaymentArrayAdapter extends ArrayAdapter<HistoryPaymentDto> {
    private List<HistoryPaymentDto> historyPaymentDtoList;

    private String referralRecoveryHistory;
    private String referralOrderHistory;
    private String wonTheLottoHistory;
    private String betOnBlackHistory;
    private String betOnRedHistory;
    private String donationHistory;
    private String referralHistory;
    private String betLostHistory;
    private String betWonHistory;
    private String refundHistory;
    private String betOnHistory;
    private String tagHistory;

    private Context context;



    public HistoryPaymentArrayAdapter(Context context,
                                      int resource,
                                      ArrayList<HistoryPaymentDto> historyPaymentDtoList) {
        super(context, resource, historyPaymentDtoList);
        this.historyPaymentDtoList = historyPaymentDtoList;
        this.context = context;
        setLanguage();
    }



    private void setLanguage() {
        // Получаем ресурсы для текущего языка
        Resources resources = getResourcesForLocale();

        // Далее вы можете использовать ресурсы для доступа к строкам на текущем языке
        referralRecoveryHistory = resources.getString(R.string.referral_recovery_history);
        referralOrderHistory = resources.getString(R.string.referral_order_history);
        wonTheLottoHistory = resources.getString(R.string.won_the_lotto_history);
        betOnBlackHistory = resources.getString(R.string.bet_on_black_history);
        betOnRedHistory = resources.getString(R.string.bet_on_red_history);
        donationHistory = resources.getString(R.string.donation_history);
        referralHistory = resources.getString(R.string.referral_history);
        betLostHistory = resources.getString(R.string.bet_lost_history);
        betWonHistory = resources.getString(R.string.bet_won_history);
        refundHistory = resources.getString(R.string.refund_history);
        betOnHistory = resources.getString(R.string.bet_on_history);
        tagHistory = resources.getString(R.string.tag_history);

    }


    private Resources getResourcesForLocale() {
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(MainActivity.newLocale);
        return new Resources(context.getAssets(), context.getResources().getDisplayMetrics(), config);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // получаем свойство, которое мы отображаем
        HistoryPaymentDto historyPaymentDto = historyPaymentDtoList.get(position);

        // получить инфлятор и раздуть XML-макет для каждого элемента
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View view = inflater.inflate(R.layout.table, null);

        TextView amount = (TextView) view.findViewById(R.id.amount_field);
        TextView address = (TextView) view.findViewById(R.id.address);

        // установить адрес и описание
        address.setText(historyPaymentDto.getAddress());
        amount.setText(historyPaymentDto.getTag().startsWith("-")
                ? historyPaymentDto.getAmount()
                : historyPaymentDto.getAmount()
                + processTag(historyPaymentDto.getTag()));

//        buttonView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
                // класс для перехода на другую страницу
//                DataExchange.getMainActivity().showInfoThisTable(position);
//            }
//        });

        return view;
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
}
