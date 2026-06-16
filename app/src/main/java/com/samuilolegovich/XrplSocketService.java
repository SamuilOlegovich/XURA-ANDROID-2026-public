package com.samuilolegovich;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;
import com.samuilolegovich.wallet.subscribers.StreamSubscriberImpl;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;



/**
 * Foreground-сервис, держащий открытым WebSocket-соединение с XRPL-узлом,
 * чтобы приложение получало обновления по счёту кошелька (баланс, транзакции)
 * даже когда экран кошелька не виден поверх других экранов.
 */
@AndroidEntryPoint
public class XrplSocketService extends Service {

    @Inject WalletRepository repository;
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "xrpl_socket";



    /** Создаёт канал уведомлений, переводит сервис в foreground (обязательно на Android 8+) и открывает сокет. */
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        connectSocket();
    }

    /** Не перезапускать сервис системой самостоятельно после убийства процесса — соединение восстановит сам кошелёк при следующем открытии. */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    /** Сервис не поддерживает привязку (bind) — используется только как самостоятельный foreground-процесс. */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Закрывает WebSocket-соединение при остановке сервиса, чтобы не оставлять висящих сетевых ресурсов. */
    @Override
    public void onDestroy() {
        repository.closeSocket();
        super.onDestroy();
    }



    /** В фоновом потоке открывает сокет к XRPL-узлу и подписывается на обновления по адресу текущего кошелька. */
    private void connectSocket() {
        AppExecutors.io().execute(() -> {
            try {
                WalletRepository repo = repository;
                repo.startSocket();
                Thread.sleep(1000);
                String address = repo.getClassicAddress();
                Map<String, Object> params = new HashMap<>();
                params.put("accounts", List.of(address));
                repo.subscribe(EnumSet.of(StreamSubscriptionEnum.ACCOUNT_CHANNELS),
                        params, new StreamSubscriberImpl());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /** Регистрирует канал уведомлений с низким приоритетом для foreground-сервиса (требуется на Android 8+). */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "XURA Network",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("XRPL ledger connection");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    /** Собирает постоянное уведомление foreground-сервиса с именем приложения и статусом подключения. */
    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Connected to XRPL")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}