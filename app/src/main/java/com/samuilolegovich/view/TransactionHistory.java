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
        AppExecutors.io().execute(() -> historyCreator.createHistory());
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
