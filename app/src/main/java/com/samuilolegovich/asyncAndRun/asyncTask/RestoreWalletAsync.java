package com.samuilolegovich.asyncAndRun.asyncTask;

import android.os.AsyncTask;

import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;

import java.util.Map;

public class RestoreWalletAsync extends AsyncTask<String, Void, Map<String, String>> {

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Map<String, String> doInBackground(String... arg) {
        Map<String, String> map = null;
        try {
            map = PaymentAndSocketManagerXRPL.getInstances().connectAnExistingWallet(arg[0], true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    protected void onPostExecute(Map<String, String> map) {
    }
}
