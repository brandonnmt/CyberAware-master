package com.example.cyberaware2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

/**
 * sends a notification based on your location. Currently only works in Cramer nmt.
 */
public class LocationReceiver extends Service {

    /**
     * sends notification
     * @param intent current intent
     * @param flags current flags
     * @param startId current id
     * @return true if successful
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String title = getString(R.string.location);
        String message = getString(R.string.location_message);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // implementation for new android apis
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(title, title, importance);
            channel.setDescription(title);
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel.getId())
                    .setSmallIcon(android.R.drawable.btn_star)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE);
            notificationManager.notify(1001, builder.build());

        } else { // old api implementation
            Notification.Builder builder = new Notification.Builder(getApplicationContext());
            builder.setSmallIcon(android.R.drawable.btn_star);
            builder.setContentTitle(title);
            builder.setContentText(message);
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
            builder.setPriority(Notification.PRIORITY_MAX ); // this is deprecated
            notificationManager.notify(1001, builder.build());
        }
        return super.onStartCommand(intent, flags, startId);

    }

    /**
     * stub method
     * @param intent current intent
     * @return null
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
