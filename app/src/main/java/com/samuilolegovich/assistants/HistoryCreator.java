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



/**
 * Загружает историю платежей текущего кошелька с XRPL-узла и преобразует её
 * в список {@link HistoryPaymentDto} для отображения на экране истории транзакций.
 */
public class HistoryCreator {
    private final WalletRepository repository;
    private String myAccount;

    /** Привязывается к единственному экземпляру WalletRepository (Singleton) для отправки команд на узел. */
    public HistoryCreator() {
        this.repository = WalletRepository.getInstance();
    }



    /** Запускает построение истории: запоминает адрес текущего кошелька и запрашивает по нему транзакции. */
    public synchronized void createHistory() {
        myAccount = repository.getClassicAddress();
        getAllHistory();
    }



    /** Отправляет на XRPL-узел запрос account_tx (последние 100 валидированных транзакций по счёту).
     *  Если сокет не подключён — пересоздаёт и переподключает его перед отправкой. */
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
            // Сокет не был открыт (InvalidStateException или другая ошибка) — пересоединяем и повторяем
            try {
                repository.closeSocket();
                repository.restartSocket();
                boolean connected = repository.startSocket();
                if (connected) {
                    repository.sendCommand("account_tx", parameters, (response) -> {
                        parseResponse(response.toString());
                    });
                }
            } catch (Exception retryEx) {
                retryEx.printStackTrace();
            }
        }
    }


    /**
     * Разбирает JSON-ответ узла на список транзакций: определяет направление (входящая/исходящая)
     * по адресу назначения, переводит сумму из дропс в XRP, извлекает метку из Memo/DestinationTag
     * и конвертирует время из XRP-эпохи (с 2000-01-01) в обычную дату. При ошибке парсинга
     * выводит список из одной заглушки, чтобы экран не остался пустым без объяснения.
     */
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
                } else {
                    label = "---";
                }

                String destTagStr = tx.has("DestinationTag")
                        ? tx.get("DestinationTag").toString()
                        : null;

                // XRP Epoch: секунды с 2000-01-01 UTC → Java ms
                String time = "";
                if (tx.has("date")) {
                    long xrpSec = tx.getLong("date");
                    Date date = new Date((xrpSec + 946684800L) * 1000L);
                    time = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(date);
                }

                HistoryPaymentDto dto = new HistoryPaymentDto(account, amount, label, time);
                dto.setDestTag(destTagStr);
                list.add(dto);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            list.add(new HistoryPaymentDto("Maybe you don't have a story yet", "-------", "---"));
        }

        TransactionHistory screen = TransactionHistory.TRANSACTION_HISTORY;
        if (screen != null) screen.selectTabButtonThread(list);

    }


    /** Преобразует hex-строку (как хранится MemoData в XRPL) в массив байт для декодирования в текст. */
    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return bytes;
    }

}
