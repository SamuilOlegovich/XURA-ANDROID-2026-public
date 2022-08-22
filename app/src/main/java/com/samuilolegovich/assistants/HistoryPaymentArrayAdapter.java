package com.samuilolegovich.assistants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.samuilolegovich.R;
import com.samuilolegovich.dto.HistoryPaymentDto;
import com.samuilolegovich.enums.StringEnum;

import java.util.ArrayList;
import java.util.List;



public class HistoryPaymentArrayAdapter extends ArrayAdapter<HistoryPaymentDto>
{
    private List<HistoryPaymentDto> historyPaymentDtoList;
    private Context context;

    public HistoryPaymentArrayAdapter(Context context, int resource, ArrayList<HistoryPaymentDto> historyPaymentDtoList) {
        super(context, resource, historyPaymentDtoList);
        this.historyPaymentDtoList = historyPaymentDtoList;
        this.context = context;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // получаем свойство, которое мы отображаем
        HistoryPaymentDto historyPaymentDto = historyPaymentDtoList.get(position);

        // получить инфлятор и раздуть XML-макет для каждого элемента
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View view = inflater.inflate(R.layout.table, null);

        TextView amount = (TextView) view.findViewById(R.id.amount);
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

        if (tag.startsWith(StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue()))  {
            return "     bet lost :(";
        }

        if (tag.startsWith(StringEnum.BET_WIN_GUESS_THE_COLOR.getValue()))  {
            return "     bet won :)";
        }

        if (tag.startsWith(StringEnum.TAG_RED_GUESS_THE_COLOR.getValue()))  {
            return "     bet on red";
        }

        if (tag.startsWith(StringEnum.TAG_BLACK_GUESS_THE_NUMBER.getValue()))  {
            return "     bet on black";
        }

        if (tag.startsWith(StringEnum.LOTTO_WIN_GUESS_THE_COLOR.getValue()))  {
            return "     won the LOTTO :)";
        }

        if (tag.startsWith(StringEnum.DONATION.getValue()))  {
            return "     donation :)";
        }

        if (tag.startsWith(StringEnum.BECOME_A_REFERRAL.getValue()) && tag.length() == 3)  {
            return "     referral order";
        }

        if (tag.startsWith(StringEnum.BECOME_A_REFERRAL.getValue()) && tag.length() > 3)  {
            return "     referral: " + tag.substring(3);
        }

        if (tag.startsWith(StringEnum.RECOVERY_BECOME_A_REFERRAL.getValue()) && tag.length() == 3)  {
            return "     referral recovery";
        }

        if (tag.startsWith(StringEnum.REFUND.getValue()))  {
            return "     refund";
        }

        if (tag.length() >= 3) {
            int tagInt = Integer.parseInt(tag.substring(0, 3)) - 100;

            if (tagInt >= 0 && tagInt <= 36) {
                return "     bet on: " + tagInt;
            }
        }
        return "     tag: " + tag;
    }
}
