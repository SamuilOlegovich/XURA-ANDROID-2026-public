package com.samuilolegovich.assistants;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.dto.HistoryPaymentDto;
import com.samuilolegovich.view.TransactionHistory;
import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class HistoryCreator {
    private final PaymentAndSocketManagerXRPL paymentManager;
    private String myAccount;

    public HistoryCreator() {
        this.paymentManager = PaymentAndSocketManagerXRPL.getInstances();
    }



    public synchronized void createHistory() {
        myAccount = paymentManager.getClassicAddress(true);
        getAllHistory();
    }



    private void getAllHistory() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("account", myAccount);
        parameters.put("validated", "true");
        parameters.put("limit", 100);
//        parameters.put("transactions", 4);

        try {
            paymentManager.sendCommand("account_tx", parameters, (response) -> {
                parseResponse(response.toString());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void parseResponse(String s) {
        ArrayList<HistoryPaymentDto> list = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(s);
            JSONArray jsonArray = json.getJSONObject("result").getJSONArray("transactions");

            for (int i = 0; i < jsonArray.length(); i++) {
                String acc = jsonArray.getJSONObject(i).getJSONObject("tx").get("Destination").toString();
                String account =  acc.equals(myAccount)
                        ? jsonArray.getJSONObject(i).getJSONObject("tx").get("Account").toString()
                        : jsonArray.getJSONObject(i).getJSONObject("tx").get("Destination").toString();

                String amount =  acc.equals(myAccount) ?
                        new BigDecimal(jsonArray.getJSONObject(i).getJSONObject("tx").get("Amount").toString())
                                .divide(BigDecimal.valueOf(MainActivity.ONE_XRP_IN_DROPS), MathContext.DECIMAL128).toString()

                        : "-" + new BigDecimal(jsonArray.getJSONObject(i).getJSONObject("tx").get("Amount").toString())
                        .divide(BigDecimal.valueOf(MainActivity.ONE_XRP_IN_DROPS), MathContext.DECIMAL128);

                String destinationTag = jsonArray.getJSONObject(i).getJSONObject("tx").has("DestinationTag")
                        ? jsonArray.getJSONObject(i).getJSONObject("tx").get("DestinationTag").toString()
                        :  "---";
                amount = amount + " XRP";

                list.add(new HistoryPaymentDto(account, amount, destinationTag));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            list.add(new HistoryPaymentDto("Maybe you don't have a story yet", "-------", "---"));
        }

        TransactionHistory.TRANSACTION_HISTORY.selectTabButtonThread(list);

    }

}
