package com.samuilolegovich.async.runnable;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;
import com.samuilolegovich.wallet.subscribers.StreamSubscriberImpl;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Восстанавливает соединение с XRPL-сокетом после разрыва: закрывает старый сокет,
 * переподключается (с повторными попытками через паузу) и заново подписывается
 * на обновления по счёту кошелька.
 */
public class RestartSubscriberRun implements Runnable {
    public static volatile boolean FLAG = true;

    private final WalletRepository repository;
    private final int time;



    /** Создаёт задачу восстановления соединения с паузой между попытками переподключения 1000мс по умолчанию. */
    public RestartSubscriberRun() {
        this.repository = WalletRepository.getInstance();
        this.time = 1000;
    }

    /** Создаёт задачу восстановления соединения с заданной паузой между попытками переподключения. */
    public RestartSubscriberRun(Integer time) {
        this.repository = WalletRepository.getInstance();
        this.time = time;
    }



    /** Выполняет полный цикл восстановления: закрывает сокет, переподключается и переподписывается, пока флаг FLAG сброшен. */
    @Override
    public void run() {
        FLAG = false;
        restartSocket();
        startSocket();
        restartSubscribeTo();
        FLAG = true;
    }



    /** Закрывает и переинициализирует текущее соединение с XRPL-узлом. */
    private void restartSocket() {
        try {
            repository.restartSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Повторяет попытки открыть сокет с паузой между ними, пока соединение не будет установлено. */
    private void startSocket() {
        boolean connected = false;

        while (!connected) {
            try {
                connected = repository.startSocket();
            } catch (Exception e) {
                e.printStackTrace();
                restartSocket();
            }

            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Заново подписывается на поток обновлений по счёту текущего кошелька (ACCOUNT_CHANNELS).
     * Если подписка не удалась — планирует повторную полную попытку восстановления через 10 секунд,
     * чтобы не оставить кошелёк без обновлений баланса/транзакций.
     */
    private void restartSubscribeTo() {
        try {
            StreamSubscriber subscriber = new StreamSubscriberImpl();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("accounts", List.of(repository.getClassicAddress()));

            Thread.sleep(1000);
            repository.subscribe(EnumSet.of(StreamSubscriptionEnum.ACCOUNT_CHANNELS), parameters, subscriber);

        } catch (Exception e) {
            FLAG = true;
            AppExecutors.io().execute(new RestartSubscriberRun(10000));
            e.printStackTrace();
        }
    }
}