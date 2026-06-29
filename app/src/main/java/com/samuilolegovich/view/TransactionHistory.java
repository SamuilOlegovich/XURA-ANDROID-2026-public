package com.samuilolegovich.view;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.assistants.HistoryCreator;
import com.samuilolegovich.assistants.HistoryPaymentAdapter;
import com.samuilolegovich.dto.HistoryPaymentDto;

import java.util.ArrayList;
import dagger.hilt.android.AndroidEntryPoint;




/**
 * Экран истории транзакций кошелька: в реальном режиме загружает реальную историю
 * XRPL-платежей через {@link HistoryCreator}, в тестовом режиме показывает заранее
 * заготовленные демонстрационные записи всех типов для визуальной проверки экрана.
 */
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
    private SwipeRefreshLayout swipeRefresh;



    /** Инициализирует экран: разметка, View, локализация, сохраняет ссылку на активити и запускает загрузку истории. */
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



    /** Находит View разметки экрана и настраивает RecyclerView с адаптером истории, открывающим детали записи при нажатии. */
    private void setViews() {
        transactionHistoryTextView = findViewById(R.id.transaction_history_text_view);
        recyclerView = findViewById(R.id.list_of_history);
        historyLoading = findViewById(R.id.history_loading);
        emptyState = findViewById(R.id.empty_state);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.xura_purple, R.color.xura_cyan);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.xura_card);
        swipeRefresh.setOnRefreshListener(() ->
                AppExecutors.io().execute(() -> historyCreator.createHistory()));
        adapter = new HistoryPaymentAdapter(this);
        adapter.setOnItemClickListener(dto ->
                TxDetailSheet.show(getSupportFragmentManager(), dto));
        recyclerView.setAdapter(adapter);
    }


    /** Устанавливает локализованный текст заголовка экрана. */
    private void setLanguage() {
        transactionHistoryTextView.setText(R.string.transaction_history_text);
    }


    /** Загружает реальную историю платежей на фоновом потоке — всегда, независимо от режима игры. */
    private void createHistoryThread() {
        AppExecutors.io().execute(() -> historyCreator.createHistory());
    }

    /** Заполняет историю тестовыми данными — все возможные типы записей (выигрыши, ставки, рефералы, прочее) для визуальной проверки экрана. */
    private void injectTestData() {
        ArrayList<HistoryPaymentDto> list = new ArrayList<>();

        // XRP-адреса сокращены для читаемости (реальные длиннее)
        String server  = "rXURA1GameServer9kLp4QwBx3ad";
        String ref     = "rXURARef7partnerAddress2026x";
        String user    = "rUserSomebody8payerAddr1234x";

        // Выигрыши (входящие от сервера)
        list.add(new HistoryPaymentDto(server, "+180.00 XRP",   "WIN",                          "14 Jun 23:41"));
        list.add(new HistoryPaymentDto(server, "+3 600.00 XRP", "JKPT",                         "14 Jun 23:40"));
        list.add(new HistoryPaymentDto(server, "+10.00 XRP",    "WIN",                          "14 Jun 22:17"));
        list.add(new HistoryPaymentDto(server, "+360.00 XRP",   "JKPT",                         "14 Jun 21:55"));
        list.add(new HistoryPaymentDto(server, "+1.00 XRP",     "RFD",                          "14 Jun 20:03"));

        // Ставки (исходящие к серверу)
        list.add(new HistoryPaymentDto(server, "-4.00 XRP",     "RLT:n5@1.5,r@2.0,d1@0.5:0", "14 Jun 23:50"));
        list.add(new HistoryPaymentDto(server, "-5.00 XRP",     "BET:RED",                      "14 Jun 23:41"));
        list.add(new HistoryPaymentDto(server, "-5.00 XRP",     "BET:BLK",                      "14 Jun 22:17"));
        list.add(new HistoryPaymentDto(server, "-10.00 XRP",    "BET:N:7",                      "14 Jun 21:55"));
        list.add(new HistoryPaymentDto(server, "-1.00 XRP",     "BET:N:23",                     "14 Jun 19:30"));
        list.add(new HistoryPaymentDto(server, "-2.50 XRP",     "LOSE",                         "14 Jun 18:44"));

        // Реферальная программа
        list.add(new HistoryPaymentDto(ref,    "-66.00 XRP",    "REF",                          "13 Jun 15:20"));
        list.add(new HistoryPaymentDto(ref,    "-13.00 XRP",    "REF:REC",                      "12 Jun 09:05"));
        list.add(new HistoryPaymentDto(user,   "+3.30 XRP",     "REF:rUserSomebody8payerAddr",  "11 Jun 17:48"));

        // Прочее
        list.add(new HistoryPaymentDto(user,   "+0.10 XRP",     "DON",                          "10 Jun 11:22"));
        list.add(new HistoryPaymentDto(server, "-5.00 XRP",     "BET:RED",                      "10 Jun 10:58"));

        selectTabButtonThread(list);
    }


    /** Отображает загруженный список истории на UI-потоке: скрывает индикатор загрузки и показывает либо список, либо состояние "пусто". */
    public void selectTabButtonThread(ArrayList<HistoryPaymentDto> list) {
        runOnUiThread(() -> {
            historyLoading.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
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


    /** Стандартная обработка нажатия "назад" без дополнительной логики. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /** Сбрасывает статическую ссылку на активити при её уничтожении, чтобы избежать утечки памяти. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        TRANSACTION_HISTORY = null;
    }
}
