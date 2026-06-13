package com.samuilolegovich.view;

import static com.samuilolegovich.view.InfoReferral.INFO_REFERRAL_CLASS;
import static com.samuilolegovich.view.Referral.REFERRAL_CLASS;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;

import java.math.BigDecimal;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class BecomeReferral extends BaseActivity {

    @Inject WalletRepository repository;
    public static final String BECOME_REFERRAL_CLASS = ".BecomeReferral";

    private String YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND;
    private String GET_RECOVERY_BECOME_REFERRAL;
    private String PAYMENT_AMOUNT_IS_INCORRECT;
    private String WRONG_DESTINATION_ADDRESS;
    private String GET_BECOME_REFERRAL;

    private View root;

    private View restoreReferral;
    private View becomeReferral;
    private View infoReferral;
    private TextView textReferral;
    private View setReferral;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.become_referral);
        root = findViewById(android.R.id.content);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        restoreReferral = findViewById(R.id.restore_referral);
        infoReferral    = findViewById(R.id.referral_info_linc);
        textReferral    = (TextView) findViewById(R.id.text_referral_view);
        becomeReferral  = findViewById(R.id.become_referral);
        setReferral     = findViewById(R.id.set_referral_linc);
    }


    private void setLanguage() {
        YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND = getString(R.string.your_account_is_not_enough_to_send);
        GET_RECOVERY_BECOME_REFERRAL       = getString(R.string.get_recovery_become_referral);
        PAYMENT_AMOUNT_IS_INCORRECT        = getString(R.string.payment_amount_is_incorrect);
        WRONG_DESTINATION_ADDRESS          = getString(R.string.wrong_destination_address);
        GET_BECOME_REFERRAL                = getString(R.string.get_becom_referral_enum);
        textReferral.setText(R.string.become_referral_and_start_earning);
    }


    private void listeners() {
        infoReferral.setOnClickListener(v -> {
            Referral.FLAG = false;
            goToAnotherPage(INFO_REFERRAL_CLASS);
        });

        setReferral.setOnClickListener(v -> {
            Referral.FLAG = false;
            goToAnotherPage(REFERRAL_CLASS);
        });

        restoreReferral.setOnClickListener(v -> {
            pulse(v);
            AppExecutors.io().execute(() -> makeStack(false));
        });

        becomeReferral.setOnClickListener(v -> {
            pulse(v);
            AppExecutors.io().execute(() -> makeStack(true));
        });
    }


    // Выполняется на IO-потоке
    private void makeStack(boolean b) {
        String sendAmount = b
                ? StringEnum.REFERRAL_COST.getValue()
                : StringEnum.REFERRAL_RECOVERY_COST.getValue();
        String memo = b ? "REF" : "REF:REC";

        if (checkData(sendAmount, memo)) {
            String msg = b ? GET_BECOME_REFERRAL : GET_RECOVERY_BECOME_REFERRAL;
            SnackbarType type = b ? SnackbarType.SUCCESS : SnackbarType.INFO;
            runOnUiThread(() -> showSnackbar(root, msg, type));
        }
    }


    private boolean checkData(String sendAmount, String memo) {
        if (sendAmount == null) {
            runOnUiThread(() -> showSnackbar(root, PAYMENT_AMOUNT_IS_INCORRECT, SnackbarType.ERROR));
            return false;
        }
        BigDecimal balance = repository.getBalance();
        if (new BigDecimal(sendAmount).compareTo(balance) > 0) {
            runOnUiThread(() -> showSnackbar(root, YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND, SnackbarType.ERROR));
            return false;
        }
        return makePayment(sendAmount, memo);
    }


    private boolean makePayment(String sendAmount, String memo) {
        boolean success;
        try {
            success = repository
                    .sendPayment(StringEnum.SERVER_ADDRESS_BECOME_REFERRAL.getValue(),
                            memo,
                            new BigDecimal(sendAmount));
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        if (!success) runOnUiThread(() -> showSnackbar(root, WRONG_DESTINATION_ADDRESS, SnackbarType.ERROR));
        return success;
    }


    private void goToAnotherPage(String namePage) {
        startActivity(new Intent(namePage));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void closeThisPage() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
