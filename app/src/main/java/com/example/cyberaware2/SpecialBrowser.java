package com.example.cyberaware2;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SpecialBrowser extends AppCompatActivity {

    private WebView webView;
    private String url; // url of article
    private User currentUser;
    private Article article; // current article

    /**
     * initializes UI and loads url
     * @param savedInstanceState current state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        Intent intent = getIntent();
        article = (Article) intent.getSerializableExtra("article");
        url = article.getUrl().trim();
        makeRecommendation();
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

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(getApplicationContext(), R.string.url_toast, Toast.LENGTH_SHORT).show();
            finish();
        }
        if(currentUser.incrementLevelCount()){
            makeNotification();
        }

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navMenuBrowser);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            /**
             * navigation menu
             * @param menuItem selected item
             * @return true
             */
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int menuId = menuItem.getItemId();
                Intent intent;
                switch(menuId) {
                    case R.id.tipButton:
                        intent = new Intent(SpecialBrowser.this, TipsActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.bookmarkButton:
                        intent = new Intent(SpecialBrowser.this, FavoriteActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.myFeedButton:
                        intent = new Intent(SpecialBrowser.this, FeedActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.newsButton:
                        intent = new Intent(SpecialBrowser.this, MainActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                }
                return true;
            }
        });

    }

    /**
     * saves user data
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
     * inflates menu
     * @param menu current menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.special_browser_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * makes a recommendation
     */
    private void makeRecommendation(){
        Intent recommendationIntent = new Intent(this, RecommendationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, recommendationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long delay = SystemClock.elapsedRealtime() + 1000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, delay, pendingIntent);
    }


    /**
     * OnClickListener for the exit button
     * @param item the button that was pressed
     * @return true if action is successful
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case R.id.starButton: // saves article to favorites
                Toast.makeText(this, R.string.saved , Toast.LENGTH_SHORT).show();
                currentUser.addFavorite(article);
                saveUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * sends a notification when a user levels up
     */
    private void makeNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Resources res = getResources();
        String[] articleArray = res.getStringArray(R.array.user_filter_types);
        String title = "Congratulations";
        String message = "Your skill level is now " + (articleArray[currentUser.getLevel() - 1]) + "!";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("levelup", "levelup", importance);
            channel.setDescription("Level Up");
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel.getId())
                    .setSmallIcon(android.R.drawable.alert_dark_frame)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE);
            notificationManager.notify(1004, builder.build());

        }else {
            Notification.Builder builder = new Notification.Builder(getApplicationContext());
            builder.setSmallIcon(android.R.drawable.alert_dark_frame);
            builder.setContentTitle(title);
            builder.setContentText(message);
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
            builder.setPriority(Notification.PRIORITY_MAX ); // this is deprecated
            notificationManager.notify(1004, builder.build());
        }

    }

}
