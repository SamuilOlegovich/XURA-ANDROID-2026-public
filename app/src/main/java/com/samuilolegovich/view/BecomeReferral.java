package com.samuilolegovich.view;

import static com.samuilolegovich.view.InfoReferral.INFO_REFERRAL_CLASS;
import static com.samuilolegovich.view.Referral.REFERRAL_CLASS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.asyncTask.GetBalanceAsync;
import com.samuilolegovich.asyncAndRun.asyncTask.SendPaymentAsync;
import com.samuilolegovich.enums.StringEnum;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;



public class BecomeReferral  extends AppCompatActivity {
    public static final String BECOME_REFERRAL_CLASS = ".BecomeReferral";
    private BecomeReferral BECOME_REFERRAL;

    private Animation animTranslate;
    private BigDecimal yourBalance;

    private TextView restoreReferral;
    private TextView becomeReferral;
    private TextView infoReferral;
    private TextView textReferral;
    private TextView setReferral;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.become_referral);
        setButtons();
        setLanguage();
        listeners();
        setBalance();
        BECOME_REFERRAL = this;
    }



    private void setButtons() {
        restoreReferral = (TextView) findViewById(R.id.restore_referral);
        infoReferral = (TextView) findViewById(R.id.referral_info_linc);
        textReferral = (TextView) findViewById(R.id.text_referral_view);
        becomeReferral = (TextView) findViewById(R.id.become_referral);
        setReferral = (TextView) findViewById(R.id.set_referral_linc);
    }


    private void setLanguage() {
        textReferral.setText(R.string.become_referral_and_start_earning);
        restoreReferral.setText(R.string.restore_referral);
        becomeReferral.setText(R.string.become_referral);
        setReferral.setText(R.string.set_referral);
        infoReferral.setText(R.string.info);
    }


    @SuppressLint("SetTextI18n")
    private void setBalance() {
        AsyncTask<String, Void, BigDecimal> getBalanceAsync = new GetBalanceAsync().execute("");

        try {
            yourBalance = getBalanceAsync.get();
//            balance.setText(yourBalance.toString() + "  XRP");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        infoReferral.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Referral.FLAG = false;
                        goToAnotherPage(INFO_REFERRAL_CLASS);
                    }
                }
        );

        setReferral.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Referral.FLAG = false;
                        goToAnotherPage(REFERRAL_CLASS);
                    }
                }
        );

        restoreReferral.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        makeStackThread(false);
                    }
                }
        );

        becomeReferral.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        makeStackThread(true);
                    }
                }
        );
    }


    private void makeStackThread(boolean b) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                makeStack(b);
            }
        }).start();
    }


    private void makeStack(boolean b) {
        if (checkData(b
                ? StringEnum.REFERRAL_COST.getValue()
                : StringEnum.REFERRAL_RECOVERY_COST.getValue(),
                b
                        ? StringEnum.BECOME_A_REFERRAL.getValue()
                        : StringEnum.RECOVERY_BECOME_A_REFERRAL.getValue())) {
            makeToast(b
                    ? StringEnum.GET_BECOME_REFERRAL.getValue()
                    : StringEnum.GET_RECOVERY_BECOME_REFERRAL.getValue() );
        }
    }


    private boolean checkData(String sendAmount, String sendTeg) {
        if (sendAmount == null) {
            makeToast("PAYMENT AMOUNT IS INCORRECT");
            return false;
        }
        if (new BigDecimal(sendAmount).compareTo(yourBalance) > 0) {
            makeToast("YOUR ACCOUNT IS NOT ENOUGH TO SEND");
            return false;
        }
        if (sendTeg != null && !sendTeg.equals("") && Long.parseLong(sendTeg) >= Integer.MAX_VALUE) {
            makeToast("TAG KNOWLEDGE CANNOT BE MORE - 2147483647");
            return false;
        }
        return makePayment(sendAmount, sendTeg);
    }


    private boolean makePayment(String sendAmount, String sendTeg) {
        AsyncTask<String, Void, Boolean> asyncTask = new SendPaymentAsync()
                .execute(StringEnum.SERVER_ADDRESS_BECOME_REFERRAL.getValue(), sendAmount, sendTeg);
        boolean b = false;
        try {
            b = asyncTask.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (!b) {
            makeToast("WRONG DESTINATION ADDRESS");
        }
        return b;
    }


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    private void makeToast(String massage) {
        new Thread() {
            public void run() {
                BECOME_REFERRAL.runOnUiThread(new Runnable() {
                    public void run() {
                        //Do your UI operations like dialog opening or Toast here
                        Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0,110);   // import android.view.Gravity;
                        toast.show();
                    }
                });
            }
        }.start();
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    // для закрытие этой активити и попадания на главную активити
    public void closeThisPage() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
