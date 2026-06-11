package com.samuilolegovich.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.assistants.HistoryCreator;
import com.samuilolegovich.assistants.HistoryPaymentArrayAdapter;
import com.samuilolegovich.dto.HistoryPaymentDto;

import java.util.ArrayList;



public class TransactionHistory  extends BaseActivity {
    public static final String TRANSACTION_HISTORY_CLASS = ".TransactionHistory";

    public static TransactionHistory TRANSACTION_HISTORY;

    private HistoryCreator historyCreator;
    private ArrayList listHistory;

    private TextView transactionHistoryTextView;
    private ListView listView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_history_page);
        historyCreator = new HistoryCreator();
        setButtons();
        setLanguage();
        TRANSACTION_HISTORY = this;
        createHistoryThread();
    }



    private void setButtons() {
        transactionHistoryTextView = (TextView) findViewById(R.id.transaction_history_text_view);
        listView = (ListView) findViewById(R.id.list_of_history);
    }


    private void setLanguage() {
        transactionHistoryTextView.setText(R.string.transaction_history_text);
    }


    private void createHistoryThread() {
        AppExecutors.io().execute(() -> historyCreator.createHistory());
    }


    public void selectTabButtonThread(ArrayList<HistoryPaymentDto> listHistory) {
        runOnUiThread(() -> selectTabButton(listHistory));
    }


    private void selectTabButton(ArrayList<HistoryPaymentDto> in) {
        ArrayAdapter<HistoryPaymentDto> adapter = new HistoryPaymentArrayAdapter(this, 0, in);
        listView.setAdapter(adapter);
        // для того чтобы лист показывался сверху вниз
        listView.setSelection(0);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TRANSACTION_HISTORY = null;
    }
}
