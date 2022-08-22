package com.samuilolegovich.asyncAndRun.asyncTask;

import android.os.AsyncTask;

import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;

import java.math.BigDecimal;

public class SendPaymentAsync extends AsyncTask<String, Void, Boolean> {
    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Boolean doInBackground(String... arg) {
        Boolean b = null;
        try {
            if (arg[2].equals("")) {
                    b = PaymentAndSocketManagerXRPL.getInstances().sendPayment(arg[0], new BigDecimal(arg[1]), true);
                return b;
            }
            b = PaymentAndSocketManagerXRPL.getInstances().sendPayment(arg[0], Integer.parseInt(arg[2]), new BigDecimal(arg[1]), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    @Override
    protected void onPostExecute(Boolean map) {
    }
}
