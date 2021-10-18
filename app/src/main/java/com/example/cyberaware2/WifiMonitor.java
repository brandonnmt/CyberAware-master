package com.example.cyberaware2;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This receiver restarts NetworkMonitor when the app is killed
 */
public class WifiMonitor extends BroadcastReceiver {
    /**
     * restarts NetworkMonitor
     * @param context current context
     * @param intent current intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, NetworkMonitor.class));
    }
}

















