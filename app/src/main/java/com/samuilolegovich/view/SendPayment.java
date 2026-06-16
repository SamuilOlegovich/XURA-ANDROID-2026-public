package com.samuilolegovich.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

import com.samuilolegovich.BaseActivity;
import androidx.lifecycle.ViewModelProvider;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.viewmodel.SendPaymentViewModel;
import dagger.hilt.android.AndroidEntryPoint;




/**
 * Экран отправки XRP-платежа: ввод адреса (вручную или сканированием QR), суммы и тега,
 * подтверждение через диалог (защита от подмены адреса в буфере обмена) и отправка
 * через {@link SendPaymentViewModel} с обработкой всех видов ошибок валидации/отправки.
 */
@AndroidEntryPoint
public class SendPayment extends BaseActivity {
    public static final String SEND_PAYMENT_CLASS = ".SendPayment";

    /** Заполняется {@link ScanQrCode} после сканирования QR-кода и читается в {@link #onResume()}. */
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
    private View send;
    private TextInputLayout tilAddress;
    private TextInputLayout tilAmount;
    private TextInputLayout tilTag;
    private CircularProgressIndicator sendProgress;
    private EditText tag;



    /**
     * Инициализирует экран: разметка, View, локализация, слушатели, и подписывается на баланс,
     * ошибки валидации/отправки и успешное завершение платежа из {@link SendPaymentViewModel}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_payment_page);

        View root = findViewById(android.R.id.content);
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
                case WRONG_ADDRESS:        tilAddress.setError(WRONG_DESTINATION_ADDRESS); break;
                case INVALID_AMOUNT:       tilAmount.setError(PAYMENT_AMOUNT_IS_INCORRECT); break;
                case AMOUNT_IS_ZERO:       tilAmount.setError(IT_IS_NOT_POSSIBLE_TO_SEND_NULL); break;
                case INSUFFICIENT_BALANCE: showSnackbar(root, YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND, SnackbarType.ERROR); break;
                case TAG_TOO_LONG:
                case TAG_TOO_LARGE:        tilTag.setError(TAG_KNOWLEDGE_CANNOT_BE_MORE); break;
                case PAYMENT_FAILED:       showSnackbar(root, WRONG_DESTINATION_ADDRESS, SnackbarType.ERROR); break;
            }
        });

        viewModel.getPaymentSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                setSendingState(false);
                showSnackbar(root, getString(R.string.payment_sent), SnackbarType.SUCCESS);
                address.setText("");
                amount.setText("");
                tag.setText("");
            }
        });

        viewModel.loadBalance();
    }



    /** Находит и сохраняет ссылки на View разметки экрана. */
    private void setButtons() {
        sendPaymentPageTextViewTwo = findViewById(R.id.send_payment_page_text_view_tow);
        sendPaymentPageTextView = findViewById(R.id.send_payment_page_text_view);
        amount = findViewById(R.id.amount_field);
        address = findViewById(R.id.from_field);
        balance = findViewById(R.id.balance);
        tilAddress = findViewById(R.id.scan_linc);
        tilAmount = findViewById(R.id.til_amount_field);
        tilTag = findViewById(R.id.til_tag_field);
        send = findViewById(R.id.send_linc);
        sendProgress = findViewById(R.id.send_progress);
        tag = findViewById(R.id.tag_field);
    }


    /** Загружает локализованные строки для всех сообщений об ошибках и заголовков экрана. */
    private void setLanguage() {
        YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND = getString(R.string.your_account_is_not_enough_to_send);
        IT_IS_NOT_POSSIBLE_TO_SEND_NULL = getString(R.string.it_is_not_possible_to_send_null);
        TAG_KNOWLEDGE_CANNOT_BE_MORE = getString(R.string.tag_knowledge_cannot_be_more);
        PAYMENT_AMOUNT_IS_INCORRECT = getString(R.string.payment_amount_is_incorrect);
        WRONG_DESTINATION_ADDRESS = getString(R.string.wrong_destination_address);
        sendPaymentPageTextViewTwo.setText(R.string.your_balance);
        sendPaymentPageTextView.setText(R.string.send_payment);
    }


    /** Назначает обработчики: отправка по кнопке, переход к сканеру QR для поля адреса, сброс ошибок при правке полей. */
    private void listeners() {
        send.setOnClickListener(v -> {
            pulse(v);
            confirmAndSend();
        });

        tilAddress.setEndIconOnClickListener(v ->
                startActivity(new Intent(ScanQrCode.SCAN_QR_CODE_CLASS)));

        address.addTextChangedListener(clearErrorWatcher(tilAddress));
        amount.addTextChangedListener(clearErrorWatcher(tilAmount));
        tag.addTextChangedListener(clearErrorWatcher(tilTag));
    }

    /**
     * Показывает диалог подтверждения с адресом и суммой перед отправкой платежа — защита
     * от clipboard-hijacking: пользователь может сверить полный адрес с оригиналом, так как
     * вредоносное ПО подменяет криптоадреса в буфере обмена на похожие, принадлежащие атакующему.
     * Если поля адреса или суммы пусты, сразу передаёт их в ViewModel — он сам покажет нужную ошибку валидации.
     */
    private void confirmAndSend() {
        String addressValue = address.getText().toString().trim();
        String amountValue = amount.getText().toString().trim();
        String tagValue = tag.getText().toString().trim();

        if (addressValue.isEmpty() || amountValue.isEmpty()) {
            setSendingState(true);
            viewModel.sendPayment(addressValue, amountValue, tagValue);
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_send_title)
                .setMessage(getString(R.string.confirm_send_message, addressValue, amountValue))
                .setCancelable(true)
                .setPositiveButton(R.string.confirm_send_button, (d, w) -> {
                    setSendingState(true);
                    viewModel.sendPayment(addressValue, amountValue, tagValue);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }


    /** Переключает UI между обычным состоянием и состоянием "идёт отправка": блокирует кнопку отправки и показывает индикатор загрузки. */
    private void setSendingState(boolean sending) {
        runOnUiThread(() -> {
            send.setEnabled(!sending);
            send.setAlpha(sending ? 0.5f : 1f);
            sendProgress.setVisibility(sending ? View.VISIBLE : View.GONE);
        });
    }

    /** Создаёт TextWatcher, который сбрасывает текст ошибки указанного поля при любом изменении его содержимого. */
    private TextWatcher clearErrorWatcher(TextInputLayout til) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { til.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        };
    }


    /** При возвращении на экран подставляет в поле адреса значение, отсканированное на экране {@link ScanQrCode}, если оно есть. */
    @Override
    protected void onResume() {
        super.onResume();
        if (!ADDRESS.equals("")) {
            address.setText(ADDRESS);
            ADDRESS = "";
        }
    }


    /** Стандартная обработка нажатия "назад" без дополнительной логики. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
