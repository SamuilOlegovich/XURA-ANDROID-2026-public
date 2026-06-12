package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

import com.samuilolegovich.BaseActivity;
import androidx.lifecycle.ViewModelProvider;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.viewmodel.SendPaymentViewModel;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class SendPayment extends BaseActivity {
    public static final String SEND_PAYMENT_CLASS = ".SendPayment";

    // Заполняется ScanQrCode и читается в onResume
    public static String ADDRESS = "";

    private String YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND;
    private String IT_IS_NOT_POSSIBLE_TO_SEND_NULL;
    private String TAG_KNOWLEDGE_CANNOT_BE_MORE;
    private String PAYMENT_AMOUNT_IS_INCORRECT;
    private String WRONG_DESTINATION_ADDRESS;

    private SendPaymentViewModel viewModel;

    private TextView sendPaymentPageTextViewTwo;
    private TextView sendPaymentPageTextView;
    private TextView balance;
    private EditText address;
    private EditText amount;
    private TextView send;
    private TextInputLayout scan;
    private CircularProgressIndicator sendProgress;
    private EditText tag;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_payment_page);

        viewModel = new ViewModelProvider(this).get(SendPaymentViewModel.class);

        setButtons();
        setLanguage();
        listeners();

        viewModel.getBalance().observe(this, b ->
                balance.setText(b.toString() + " XRP"));

        viewModel.getError().observe(this, error -> {
            if (error == null) return;
            setSendingState(false);
            switch (error) {
                case WRONG_ADDRESS:    showToast(WRONG_DESTINATION_ADDRESS); break;
                case INVALID_AMOUNT:   showToast(PAYMENT_AMOUNT_IS_INCORRECT); break;
                case AMOUNT_IS_ZERO:   showToast(IT_IS_NOT_POSSIBLE_TO_SEND_NULL); break;
                case INSUFFICIENT_BALANCE: showToast(YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND); break;
                case TAG_TOO_LONG:
                case TAG_TOO_LARGE:    showToast(TAG_KNOWLEDGE_CANNOT_BE_MORE); break;
                case PAYMENT_FAILED:   showToast(WRONG_DESTINATION_ADDRESS); break;
            }
        });

        viewModel.getPaymentSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                setSendingState(false);
                showToast("PAYMENT SENT");
                address.setText("");
                amount.setText("");
                tag.setText("");
            }
        });

        viewModel.loadBalance();
    }



    private void setButtons() {
        sendPaymentPageTextViewTwo = findViewById(R.id.send_payment_page_text_view_tow);
        sendPaymentPageTextView = findViewById(R.id.send_payment_page_text_view);
        amount = findViewById(R.id.amount_field);
        address = findViewById(R.id.from_field);
        balance = findViewById(R.id.balance);
        scan = findViewById(R.id.scan_linc);
        send = findViewById(R.id.send_linc);
        sendProgress = findViewById(R.id.send_progress);
        tag = findViewById(R.id.tag_field);
    }


    private void setLanguage() {
        YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND = getString(R.string.your_account_is_not_enough_to_send);
        IT_IS_NOT_POSSIBLE_TO_SEND_NULL = getString(R.string.it_is_not_possible_to_send_null);
        TAG_KNOWLEDGE_CANNOT_BE_MORE = getString(R.string.tag_knowledge_cannot_be_more);
        PAYMENT_AMOUNT_IS_INCORRECT = getString(R.string.payment_amount_is_incorrect);
        WRONG_DESTINATION_ADDRESS = getString(R.string.wrong_destination_address);
        sendPaymentPageTextViewTwo.setText(R.string.your_balance);
        sendPaymentPageTextView.setText(R.string.send_payment);
        send.setText(R.string.send);
    }


    private void listeners() {
        send.setOnClickListener(v -> {
            pulse(v);
            setSendingState(true);
            viewModel.sendPayment(
                    address.getText().toString(),
                    amount.getText().toString(),
                    tag.getText().toString()
            );
        });

        scan.setEndIconOnClickListener(v ->
                startActivity(new Intent(ScanQrCode.SCAN_QR_CODE_CLASS)));
    }


    private void setSendingState(boolean sending) {
        runOnUiThread(() -> {
            send.setEnabled(!sending);
            send.setAlpha(sending ? 0.5f : 1f);
            sendProgress.setVisibility(sending ? View.VISIBLE : View.GONE);
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> {
            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 110);
            toast.show();
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!ADDRESS.equals("")) {
            address.setText(ADDRESS);
            ADDRESS = "";
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}