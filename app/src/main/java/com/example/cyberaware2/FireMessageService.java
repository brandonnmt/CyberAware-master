package com.example.cyberaware2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


/**
 * Firebase message class. This class is used for push notifications.
 */
public class FireMessageService extends FirebaseMessagingService {
    private final String TAG = "Fire";

    /**
     * refresh token method currently not used
     * @param s token
     */
    @Override
    public void onNewToken(String s) {
        Log.d(TAG, "Refreshed token " + s);
        super.onNewToken(s);
    }

    /**
     * sends a push notification
     * @param remoteMessage firebase message
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // new android implementation
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("firebase", "firebase", importance);
                channel.setDescription("firebase");
                notificationManager.createNotificationChannel(channel);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel.getId())
                        .setSmallIcon(android.R.drawable.btn_star)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(NotificationCompat.DEFAULT_VIBRATE);
                notificationManager.notify(1002, builder.build());

            } else { // old android implementation
                Notification.Builder builder = new Notification.Builder(getApplicationContext());
                builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
                builder.setContentTitle(remoteMessage.getNotification().getTitle());
                builder.setContentText(remoteMessage.getNotification().getBody());
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
                builder.setPriority(Notification.PRIORITY_MAX); // this is deprecated
                builder.setStyle(new Notification.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
                notificationManager.notify(1002, builder.build());
            }

        }

    }
}
