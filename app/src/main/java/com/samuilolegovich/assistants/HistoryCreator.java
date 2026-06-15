package com.samuilolegovich.assistants;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.dto.HistoryPaymentDto;
import com.samuilolegovich.view.TransactionHistory;
import com.samuilolegovich.wallet.repository.WalletRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;



public class HistoryCreator {
    private final WalletRepository repository;
    private String myAccount;

    public HistoryCreator() {
        this.repository = WalletRepository.getInstance();
    }



    public synchronized void createHistory() {
        myAccount = repository.getClassicAddress();
        getAllHistory();
    }



    private void getAllHistory() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("account", myAccount);
        parameters.put("validated", "true");
        parameters.put("limit", 100);

        try {
            repository.sendCommand("account_tx", parameters, (response) -> {
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
                JSONObject tx = jsonArray.getJSONObject(i).getJSONObject("tx");

                String acc     = tx.get("Destination").toString();
                String account = acc.equals(myAccount)
                        ? tx.get("Account").toString()
                        : tx.get("Destination").toString();

                BigDecimal raw = new BigDecimal(tx.get("Amount").toString())
                        .divide(BigDecimal.valueOf(MainActivity.ONE_XRP_IN_DROPS), MathContext.DECIMAL128);
                String amount = (acc.equals(myAccount) ? "+" + raw : "-" + raw) + " XRP";

                String label;
                if (tx.has("Memos")) {
                    String hexMemo = tx.getJSONArray("Memos")
                            .getJSONObject(0)
                            .getJSONObject("Memo")
                            .getString("MemoData");
                    label = new String(hexToBytes(hexMemo), StandardCharsets.UTF_8);
                } else if (tx.has("DestinationTag")) {
                    label = tx.get("DestinationTag").toString();
                } else {
                    label = "---";
                }

                // XRP Epoch: секунды с 2000-01-01 UTC → Java ms
                String time = "";
                if (tx.has("date")) {
                    long xrpSec = tx.getLong("date");
                    Date date = new Date((xrpSec + 946684800L) * 1000L);
                    time = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(date);
                }

                list.add(new HistoryPaymentDto(account, amount, label, time));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            list.add(new HistoryPaymentDto("Maybe you don't have a story yet", "-------", "---"));
        }

        TransactionHistory.TRANSACTION_HISTORY.selectTabButtonThread(list);

    }


    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return bytes;
    }

}
