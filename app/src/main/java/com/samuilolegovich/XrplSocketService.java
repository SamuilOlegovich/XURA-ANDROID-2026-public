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
import com.samuilolegovich.wallet.subscribers.MyStreamSubscriber;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class XrplSocketService extends Service {
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "xrpl_socket";



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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        WalletRepository.getInstance().closeSocket();
        super.onDestroy();
    }



    private void connectSocket() {
        AppExecutors.io().execute(() -> {
            try {
                WalletRepository repo = WalletRepository.getInstance();
                repo.startSocket();
                Thread.sleep(1000);
                String address = repo.getClassicAddress();
                Map<String, Object> params = new HashMap<>();
                params.put("accounts", List.of(address));
                repo.subscribe(EnumSet.of(StreamSubscriptionEnum.ACCOUNT_CHANNELS),
                        params, new MyStreamSubscriber());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

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

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Connected to XRPL")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}