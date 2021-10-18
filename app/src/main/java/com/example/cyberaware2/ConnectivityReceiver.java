package com.example.cyberaware2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * This class acts as listener for network connectivity changes.
 * This class is based of of an one I found on stack overflow.
 * The original had an isConnected method but I removed it since I already using
 * a different one.
 * url Link
 * https://stackoverflow.com/questions/48527171/detect-connectivity-change-in-android-7-and-above-when-app-is-killed-in-backgrou#
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    private ConnectivityReceiverListener crl; // crl = connectivity receiver listener

    /**
     * constructor that initializes crl
     * @param listener adds listener
     */
    public ConnectivityReceiver(ConnectivityReceiverListener listener) {
        crl = listener;
    }

    /**
     * on receive method calls NetWorkMonitor
     * @param context context
     * @param intent current intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        crl.onNetworkConnectionChanged();
    }


    /**
     * interface for connectivity listener.
     */
    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged();
    }
}
