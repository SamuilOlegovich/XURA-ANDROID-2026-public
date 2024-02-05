package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.asyncTask.GetBalanceAsync;
import com.samuilolegovich.asyncAndRun.asyncTask.SendPaymentAsync;


import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;



// тут страница для отсылки платежа
public class SendPayment extends AppCompatActivity {
    public static final String SEND_PAYMENT_CLASS = ".SendPayment";
    public static SendPayment SEND_PAYMENT;
    private static final String XRP = " XRP";
    public static String ADDRESS = "";

    private Animation animTranslate;
    private BigDecimal yourBalance;

    private TextView balance;
    private EditText address;
    private EditText amount;
    private TextView send;
    private TextView scan;
    private EditText tag;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.send_payment);
        setButtons();
        setLanguage();
        setBalance();
        listeners();
        SEND_PAYMENT = this;
    }

    private void setButtons() {
        balance = (TextView) findViewById(R.id.balance);
        amount = (EditText) findViewById(R.id.amount);
        address = (EditText) findViewById(R.id.from);
        send = (TextView) findViewById(R.id.send);
        scan = (TextView) findViewById(R.id.scan);
        tag = (EditText) findViewById(R.id.tag);
    }

    @SuppressLint("SetTextI18n")
    private void setBalance() {
        AsyncTask<String, Void, BigDecimal> getBalanceAsync = new GetBalanceAsync().execute("");
        try {
            yourBalance = getBalanceAsync.get();
            balance.setText(yourBalance.toString() + XRP);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setNewText() {
        new Thread() {
            public void run() {
                SEND_PAYMENT.runOnUiThread(new Runnable() {
                    public void run() {
                        balance.setText(yourBalance.toString() + XRP);
                        address.setText("");
                        amount.setText("");
                        tag.setText("");
                    }
                });
            }
        }.start();
    }

    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);
        send.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        sendThread(true);

                    }
                }
        );
        scan.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(ScanQrCode.SCAN_QR_CODE_CLASS);
                    }
                }
        );
    }

    private void sendThread(boolean b) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sendAmount = prepareTheShippingAmount(amount.getText().toString());
                String sendAddress = address.getText().toString();
                String sendTeg = tag.getText().toString();
                if (checkData(sendAddress, sendAmount, sendTeg)) {
                    setNewText();
                    makeToast("PAYMENT SENT");
                }
            }
        }).start();
    }

    private void makeToast(String massage) {
        new Thread() {
            public void run() {
                SEND_PAYMENT.runOnUiThread(new Runnable() {
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

    private String prepareTheShippingAmount(String sendAmount) {
        if (sendAmount.contains(".")) {
            int i = sendAmount.indexOf(".");
            int max = i + 6;
            if (max < sendAmount.length()) {
                return sendAmount.substring(0, max + 1);
            }
        }
        return sendAmount;
    }

    private boolean checkData(String sendAddress, String sendAmount, String sendTeg) {
        setBalance();
        if (sendAddress == null || sendAddress.length() < 33) {
            makeToast("WRONG DESTINATION ADDRESS");
            return false;
        }
        if (sendAmount == null || sendAmount.length() < 1) {
            makeToast("PAYMENT AMOUNT IS INCORRECT");
            return false;
        }
        if (new BigDecimal(sendAmount).compareTo(new BigDecimal("0.000000")) == 0) {
            makeToast( "IT IS NOT POSSIBLE TO SEND NULL");
            return false;
        }
        if (new BigDecimal(sendAmount).compareTo(yourBalance) > 0) {
            makeToast("YOUR ACCOUNT IS NOT ENOUGH TO SEND");
            return false;
        }
        if (sendTeg != null && sendTeg.length() > 11) {
            makeToast("TAG KNOWLEDGE CANNOT BE MORE - 2147483647");
            return false;
        }
        if (sendTeg != null && !sendTeg.equals("") && Long.parseLong(sendTeg) >= Integer.MAX_VALUE) {
            makeToast("TAG KNOWLEDGE CANNOT BE MORE - 2147483647");
            return false;
        }
        return makePayment(sendAddress, sendAmount, sendTeg);
    }

    private boolean makePayment(String sendAddress, String sendAmount, String sendTeg) {
        AsyncTask<String, Void, Boolean> asyncTask = new SendPaymentAsync().execute(sendAddress, sendAmount, sendTeg);
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

    @SuppressLint("SetTextI18n")
    public void updateBalance(BigDecimal bigDecimal) {
        yourBalance = bigDecimal;
        balance.setText(yourBalance.toString());
    }



//    private void makeToast(String massage) {
//        Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.TOP, 0,110);   // import android.view.Gravity;
//        toast.show();
//    }


    public void setAddress(String address) {
        new Thread() {
            public void run() {
                SEND_PAYMENT.runOnUiThread(new Runnable() {
                    public void run() {
                        ADDRESS = address;
                    }
                });
            }
        }.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (!ADDRESS.equals("")) {
            address.setText(ADDRESS);
            ADDRESS = "";
        }
    }



    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
