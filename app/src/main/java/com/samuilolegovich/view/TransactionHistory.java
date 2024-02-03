package com.samuilolegovich.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.assistants.HistoryCreator;
import com.samuilolegovich.assistants.HistoryPaymentArrayAdapter;
import com.samuilolegovich.dto.HistoryPaymentDto;

import java.util.ArrayList;



public class TransactionHistory  extends AppCompatActivity {
    public static final String TRANSACTION_HISTORY_CLASS = ".TransactionHistory";
    public static TransactionHistory TRANSACTION_HISTORY;
    private HistoryCreator historyCreator;
    private ArrayList listHistory;

    private ListView listView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.transaction_history);
        historyCreator = new HistoryCreator();
        setButtons();
        TRANSACTION_HISTORY = this;
        createHistoryThread();
//        listeners();
    }



    private void setButtons() {
        listView = (ListView) findViewById(R.id.list_of_history);
    }



    private void createHistoryThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                historyCreator.createHistory();
            }
        }).start();
    }


    public void selectTabButtonThread(ArrayList<HistoryPaymentDto> listHistory) {
        new Thread() {
            public void run() {
                TRANSACTION_HISTORY.runOnUiThread(new Runnable() {
                    public void run() {
                        selectTabButton(listHistory);
                    }
                });
            }
        }.start();
    }

    private void selectTabButton(ArrayList<HistoryPaymentDto> in) {
        ArrayAdapter<HistoryPaymentDto> adapter = new HistoryPaymentArrayAdapter(this, 0, in);
        listView.setAdapter(adapter);
        // для того чтобы лист показывался сверху вниз
        listView.setSelection(0);
    }



    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
