package com.example.cyberaware2;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;


/**
 * This service sends a notification if your network is insecure.
 * The service can be run in the background of the app and persists when the app is killed.
 * partially based on code from
 * https://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android
 * note onNetworkConnectionChanged is very loosely based on
 * https://stackoverflow.com/questions/25662761/how-to-differentiate-open-and-secure-wifi-networks-without-connecting-to-it-in-a
 */
public class NetworkMonitor extends Service implements ConnectivityReceiver.ConnectivityReceiverListener {

    private ConnectivityReceiver connectivityReceiver; // connectivity reviver
    private User currentUser;


    /**
     * default constructor initializes connectivityReceiver though not used is required by
     * the compiler.
     */
    public NetworkMonitor(){
        connectivityReceiver = new ConnectivityReceiver(this);
    }

    /**
     * initializes connectivityReceiver
     * @param context current context
     */
    public NetworkMonitor(Context context) {
        connectivityReceiver = new ConnectivityReceiver(this);
    }

    /**
     * registers the receiver
     * @param intent current intent
     * @param flags flags
     * @param startId starting id
     * @return super onStartCommand
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * initializes connectivityReceiver
     */
    @Override
    public void onCreate() {
        super.onCreate();
        connectivityReceiver = new ConnectivityReceiver(this);
    }

    /**
     * unregisters connectivity Reviver an asks wifimonitor to reinitialize the service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            unregisterReceiver(connectivityReceiver);
            Intent broadcastIntent = new Intent(this, WifiMonitor.class);
            sendBroadcast(broadcastIntent);
        }
    }


    /**
     * implements on network connectivity change and sends a notification if wifi is an open network
     */
    @Override
    public void onNetworkConnectionChanged() {
        // https://stackoverflow.com/questions/25662761/how-to-differentiate-open-and-secure-wifi-networks-without-connecting-to-it-in-a
        WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> networkList = wifiMan.getScanResults();

        String ssid = "";

        //https://stackoverflow.com/questions/53944755/how-to-get-current-wifi-connection-name-in-android-pie9-devices
        ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            ssid = wifiInfo.getSSID();
            ssid = ssid.replace("\"", "");
        }

        readUser();

        if (currentUser.isNotifications()) {
            if (networkList != null) {
                for (ScanResult network : networkList) {
                    if (network.SSID.equals(ssid)) {
                        String capabilities = network.capabilities;
                        if (capabilities.toUpperCase().contains("WEP")) {

                        } else if (capabilities.toUpperCase().contains("WPA")) {

                        } else {

                            Boolean inWhitelist = false;
                            for (String wifi : currentUser.getWhitelist()) {
                                if (wifi.equals(ssid)) {
                                    inWhitelist = true;
                                    break;
                                }
                            }

                            if (!inWhitelist) {
                                LocationManager locationManager = (LocationManager)
                                        getSystemService(Context.LOCATION_SERVICE);
                                LocationListener locationListener = new MyLocationListener(this);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                                    }
                                }

                                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                String title = getString(R.string.wifi_warning);
                                String message = getString(R.string.wifi_explanation);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    Intent intent = new Intent(this, WifiActivity.class);
                                    PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT);

                                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                                    NotificationChannel channel = new NotificationChannel("wifi", "wifi", importance);
                                    channel.setDescription("checks wifi");
                                    notificationManager.createNotificationChannel(channel);
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel.getId())
                                            .setSmallIcon(android.R.drawable.alert_dark_frame)
                                            .setContentTitle(title)
                                            .setContentText(message)
                                            .setStyle(new NotificationCompat.BigTextStyle()
                                                    .bigText(message))
                                            .setPriority(NotificationCompat.PRIORITY_MAX)
                                            .setContentIntent(pIntent)
                                            .setDefaults(NotificationCompat.DEFAULT_VIBRATE);
                                    notificationManager.notify(1002, builder.build());

                                } else {
                                    Notification.Builder builder = new Notification.Builder(getApplicationContext());
                                    builder.setSmallIcon(android.R.drawable.alert_dark_frame);
                                    builder.setContentTitle(title);
                                    builder.setContentText(message);
                                    builder.setDefaults(Notification.DEFAULT_VIBRATE);
                                    builder.setPriority(Notification.PRIORITY_MAX); // this is deprecated
                                    notificationManager.notify(1002, builder.build());
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * auto generated stub method
     * @param intent current intent
     * @return null
     */
    @Override
    public IBinder onBind(Intent intent) { return null; }

    /**
     * reads user data from memory
     */
    private void readUser(){
        try { // read user info from local storage
            FileInputStream fIn = openFileInput("mySettings.txt");
            ObjectInputStream oIn = new ObjectInputStream(fIn);
            currentUser = (User) oIn.readObject(); // gets the current user from a file
            oIn.close();
            fIn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
