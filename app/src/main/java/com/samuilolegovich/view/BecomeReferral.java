package com.samuilolegovich.view;

import static com.samuilolegovich.view.InfoReferral.INFO_REFERRAL_CLASS;
import static com.samuilolegovich.view.Referral.REFERRAL_CLASS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

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

    private TextView restoreReferral;
    private TextView becomeReferral;
    private TextView infoReferral;
    private TextView textReferral;
    private TextView setReferral;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.become_referral);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        restoreReferral = (TextView) findViewById(R.id.restore_referral);
        infoReferral    = (TextView) findViewById(R.id.referral_info_linc);
        textReferral    = (TextView) findViewById(R.id.text_referral_view);
        becomeReferral  = (TextView) findViewById(R.id.become_referral);
        setReferral     = (TextView) findViewById(R.id.set_referral_linc);
    }


    private void setLanguage() {
        YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND = getString(R.string.your_account_is_not_enough_to_send);
        GET_RECOVERY_BECOME_REFERRAL       = getString(R.string.get_recovery_become_referral);
        PAYMENT_AMOUNT_IS_INCORRECT        = getString(R.string.payment_amount_is_incorrect);
        WRONG_DESTINATION_ADDRESS          = getString(R.string.wrong_destination_address);
        GET_BECOME_REFERRAL                = getString(R.string.get_becom_referral_enum);
        textReferral.setText(R.string.become_referral_and_start_earning);
        restoreReferral.setText(R.string.restore_referral);
        becomeReferral.setText(R.string.become_referral);
        setReferral.setText(R.string.set_referral);
        infoReferral.setText(R.string.info);
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
            makeToast(b ? GET_BECOME_REFERRAL : GET_RECOVERY_BECOME_REFERRAL);
        }
    }


    private boolean checkData(String sendAmount, String memo) {
        if (sendAmount == null) {
            makeToast(PAYMENT_AMOUNT_IS_INCORRECT);
            return false;
        }
        BigDecimal balance = repository.getBalance();
        if (new BigDecimal(sendAmount).compareTo(balance) > 0) {
            makeToast(YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND);
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
        if (!success) makeToast(WRONG_DESTINATION_ADDRESS);
        return success;
    }


    private void goToAnotherPage(String namePage) {
        startActivity(new Intent(namePage));
    }


    private void makeToast(String massage) {
        runOnUiThread(() -> {
            Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 110);
            toast.show();
        });
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