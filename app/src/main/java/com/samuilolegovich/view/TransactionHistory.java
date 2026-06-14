package com.samuilolegovich.view;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.assistants.HistoryCreator;
import com.samuilolegovich.assistants.HistoryPaymentAdapter;
import com.samuilolegovich.dto.HistoryPaymentDto;

import java.util.ArrayList;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class TransactionHistory extends BaseActivity {
    public static final String TRANSACTION_HISTORY_CLASS = ".TransactionHistory";

    public static TransactionHistory TRANSACTION_HISTORY;

    private HistoryCreator historyCreator;
    private HistoryPaymentAdapter adapter;

    private TextView transactionHistoryTextView;
    private RecyclerView recyclerView;
    private com.google.android.material.progressindicator.CircularProgressIndicator historyLoading;
    private LinearLayout emptyState;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_history_page);
        historyCreator = new HistoryCreator();
        setViews();
        setLanguage();
        TRANSACTION_HISTORY = this;
        createHistoryThread();
    }



    private void setViews() {
        transactionHistoryTextView = findViewById(R.id.transaction_history_text_view);
        recyclerView = findViewById(R.id.list_of_history);
        historyLoading = findViewById(R.id.history_loading);
        emptyState = findViewById(R.id.empty_state);
        adapter = new HistoryPaymentAdapter(this);
        recyclerView.setAdapter(adapter);
    }


    private void setLanguage() {
        transactionHistoryTextView.setText(R.string.transaction_history_text);
    }


    private void createHistoryThread() {
        // TODO: убрать injectTestData() и раскомментировать настоящий вызов перед релизом
        injectTestData();
        // AppExecutors.io().execute(() -> historyCreator.createHistory());
    }

    // Тестовые данные — все возможные типы записей для визуального ревью
    private void injectTestData() {
        ArrayList<HistoryPaymentDto> list = new ArrayList<>();

        // XRP-адреса сокращены для читаемости (реальные длиннее)
        String server  = "rXURA1GameServer9kLp4QwBx3ad";
        String ref     = "rXURARef7partnerAddress2026x";
        String user    = "rUserSomebody8payerAddr1234x";

        // Выигрыши (входящие от сервера)
        list.add(new HistoryPaymentDto(server, "+180.00 XRP",  "WIN"));
        list.add(new HistoryPaymentDto(server, "+3 600.00 XRP","JKPT"));
        list.add(new HistoryPaymentDto(server, "+10.00 XRP",   "WIN"));
        list.add(new HistoryPaymentDto(server, "+360.00 XRP",  "JKPT"));
        list.add(new HistoryPaymentDto(server, "+1.00 XRP",    "RFD"));

        // Ставки (исходящие к серверу)
        list.add(new HistoryPaymentDto(server, "-5.00 XRP",    "BET:RED"));
        list.add(new HistoryPaymentDto(server, "-5.00 XRP",    "BET:BLK"));
        list.add(new HistoryPaymentDto(server, "-10.00 XRP",   "BET:N:7"));
        list.add(new HistoryPaymentDto(server, "-1.00 XRP",    "BET:N:23"));
        list.add(new HistoryPaymentDto(server, "-2.50 XRP",    "LOSE"));

        // Реферальная программа
        list.add(new HistoryPaymentDto(ref,    "-66.00 XRP",   "REF"));
        list.add(new HistoryPaymentDto(ref,    "-13.00 XRP",   "REF:REC"));
        list.add(new HistoryPaymentDto(user,   "+3.30 XRP",    "REF:rUserSomebody8payerAddr"));

        // Прочее
        list.add(new HistoryPaymentDto(user,   "+0.10 XRP",    "DON"));
        list.add(new HistoryPaymentDto(server, "-5.00 XRP",    "BET:RED"));

        selectTabButtonThread(list);
    }


    public void selectTabButtonThread(ArrayList<HistoryPaymentDto> list) {
        runOnUiThread(() -> {
            historyLoading.setVisibility(View.GONE);
            if (list == null || list.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.submitList(list);
                recyclerView.scrollToPosition(0);
            }
        });
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
