package com.example.cyberaware2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


/**
 * Created by Vhl2 on 6/13/2019.
 * This Activity tracks user usage stats and location
 * usage from based off of.
 * https://stackoverflow.com/questions/49743206/android-usagestatsmanager-get-a-list-of-currently-running-apps-on-phone
 */
public class UsageActivity extends AppCompatActivity {
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private final String TAG = "UsageActivity"; // log tag
    private User currentUser;
    private String provider;

    private class AppInfo implements Comparable<AppInfo>{
        private String appName;
        private long timeSpent;

        private AppInfo(String appName, long timeSpent) {
            this.appName = appName;
            this.timeSpent = timeSpent;
        }

        private String getAppName() {
            return this.appName;
        }

        private long getTimeSpent() {
            return this.timeSpent;
        }

        /**
         * to sort the apps based on timeSpent
         */
        @Override
        public int compareTo(AppInfo comparedApp) {
            int compareTimeSpent = (int) comparedApp.getTimeSpent();
            return compareTimeSpent-(int)this.timeSpent;
        }
    }


    /**
     * On create method for the page
     * @param savedInstanceState current state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);
        listView = (ListView) findViewById(R.id.usageList);
        ArrayList<String> itemList = new ArrayList<>();
        TextView locCity = findViewById(R.id.currentCity);
        TextView locStateCountry = findViewById(R.id.currentStateCountry);


        readUser();
        if (currentUser.isUsage()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
                int mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), this.getPackageName());
                if (!(mode == AppOpsManager.MODE_ALLOWED)) {
                    Toast.makeText(UsageActivity.this, "Please grant Usage Access in Phone Settings to view Statistics", Toast.LENGTH_LONG).show();
                }
            }

            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);

            //Daily interval of app usage
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            long startTime = calendar.getTimeInMillis();
            long endTime = System.currentTimeMillis();

            ArrayList<AppInfo> appInfoList = new ArrayList<>();

            // code format heavily copied from https://stackoverflow.com/questions/49743206/android-usagestatsmanager-get-a-list-of-currently-running-apps-on-phone
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);


            ArrayList<String> appPackages = new ArrayList();
            if (appList != null && appList.size() > 0) {
                for (UsageStats stat : appList) {
                    if (!stat.getPackageName().contains("com.google") && !stat.getPackageName().contains("com.android.provider")) {
                        if ((stat.getTotalTimeInForeground() / 1000) / 60 != 0) {
                            PackageManager pkgManager = getApplicationContext().getPackageManager();
                            try {
                                String appName = (String) pkgManager.getApplicationLabel(pkgManager.getApplicationInfo(stat.getPackageName(), PackageManager.GET_META_DATA));
                                AppInfo app = new AppInfo(appName, (stat.getTotalTimeInForeground() / 1000) / 60);
                                appInfoList.add(app);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        appPackages.add(stat.getPackageName());
                    }
                }
            }

            //looks for malicious apps if user downloads more apps
            if (appPackages.size() > currentUser.getNumApps()) {
                maliciousAppsDetected(appPackages);
            } else {

                TextView malApps = findViewById(R.id.malicious_app);
                TextView malAppsInstruct = findViewById(R.id.malicious_app_instruct);
                malAppsInstruct.setVisibility(View.INVISIBLE);
                malApps.setText("*No malicious apps detected*");
            }
            currentUser.setNumApps(appPackages.size());



            Collections.sort(appInfoList);
            for (AppInfo eachApp : appInfoList) {
                Log.e(TAG, "Executed app: " + eachApp.getAppName() + "\ntime: " + eachApp.getTimeSpent() + " mins");
                itemList.add("Executed app: " + eachApp.getAppName() + "\ntime: " + eachApp.getTimeSpent() + " mins");
            }

            //add top 3 most used apps to the news filter
            //todo currently this will only update if the user clicks on the star. Is this okay?
            if (appInfoList.size() >= 4) {
                for (int i = 0; i < 4; i++) {
                    if (appInfoList.get(i).appName.contains("CyberAware")) {
                        continue;
                    }
                    if (!currentUser.getUserFilter().contains(appInfoList.get(i).appName)) {
                        currentUser.addToFilter(appInfoList.get(i).appName);
                    }
                }
                saveUser();
            }

        } else {
            Toast.makeText(UsageActivity.this, "Please enable usage in App Settings to view Usage Statistics", Toast.LENGTH_LONG).show();
        }

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, itemList);
        listView.setAdapter(adapter);
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            }

        }

        // code heavily copied from https://stackoverflow.com/questions/18221614/how-i-can-get-the-city-name-of-my-current-position
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null && currentUser.isLocation()) {
            locationListener.onLocationChanged(location);
        }

        String country;
        String city;
        String state;
        country = ((MyLocationListener) locationListener).getCurrentCountry();
        city = ((MyLocationListener) locationListener).getCurrentCity();
        state = ((MyLocationListener) locationListener).getCurrentState();

        locCity.setText(city);
        locStateCountry.setText(state + ", " + country);


    }

    /**
     * creates the main_menu
     * @param menu menu.xml file.
     * @return true if action is successful.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.exit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * OnClickListener for the exit button
     * @param item the button that was pressed
     * @return true if action is successful
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        Intent intent;
        switch (menuId) {
            case R.id.doneButton:
                intent = new Intent(UsageActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * reads user data
     */
    private void saveUser(){
        try { // saves user data to internal storage
            FileOutputStream fOut = openFileOutput("mySettings.txt", MODE_PRIVATE);
            ObjectOutputStream oOut = new ObjectOutputStream(fOut);
            oOut.writeObject(currentUser);
            oOut.close();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private boolean maliciousAppsDetected(ArrayList<String> mobileApps) {
        ArrayList<String> susApps = new ArrayList();
        ArrayList<String> foundApps = new ArrayList();

        int success = 0;
        try {
            InputStream in = getAssets().open("maliciousApps.txt");

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                susApps.add(line);
            }

            for (String app : mobileApps) {
                if (susApps.contains(app)) {
                    app = app.replace("com.", "");
                    foundApps.add(app);
                }
            }

            String deleteList = "";
            if (foundApps.size() > 0) {
                for (String app: foundApps) {
                    deleteList = deleteList + app + "\n";
                }
            }

            TextView malApps = findViewById(R.id.malicious_app);
            TextView malAppsInstruct = findViewById(R.id.malicious_app_instruct);
            malApps.setMovementMethod(new ScrollingMovementMethod());
            if (foundApps != null && foundApps.size() > 0) {
                malApps.setVisibility(View.VISIBLE);
                malApps.setText(deleteList);

                if (currentUser.isNotifications()) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    String title = getString(R.string.malicious_app_warning) + "\n";
                    String message = getString(R.string.malicious_app_explanation) + "\n\n" + deleteList + getString(R.string.malicious_app_extended);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Intent intent = new Intent(this, UsageActivity.class);
                        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel("apps", "apps", importance);
                        channel.setDescription("checks apps");
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
                    }
                }
            } else {
                malAppsInstruct.setVisibility(View.INVISIBLE);
                malApps.setText("*No malicious apps detected*");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}