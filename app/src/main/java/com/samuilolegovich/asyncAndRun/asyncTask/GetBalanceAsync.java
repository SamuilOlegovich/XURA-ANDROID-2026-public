package com.samuilolegovich.asyncAndRun.asyncTask;

import android.os.AsyncTask;

import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;

import java.math.BigDecimal;

public class GetBalanceAsync extends AsyncTask<String, Void, BigDecimal> {
    @Override
    protected void onPreExecute() {
    }

    @Override
    protected BigDecimal doInBackground(String... arg) {
        BigDecimal bigDecimal = null;
        try {
            bigDecimal = PaymentAndSocketManagerXRPL.getInstances().getBalance(true);
        } catch (Exception e) {
            bigDecimal = new BigDecimal("0.000000");
            e.printStackTrace();
        }
        if (bigDecimal == null) {
            bigDecimal = new BigDecimal("0.000000");
        }
        return bigDecimal;
    }

    @Override
    protected void onPostExecute(BigDecimal bigDecimal) {
    }
}
