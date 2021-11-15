package com.example.cyberaware2;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * Similar to MainActivity except content is filtered for users
 */
public class FeedActivity extends AppCompatActivity {
    private ListView listView; // displays articles
    private ImageAdapter imageAdapter; // custom ArrayAdapter
    private ArrayList<Article> itemList; // article list
    private ArrayList<Drawable> drawableList; // drawable list
    private User currentUser; // current user object
    private final int BROWSER_CODE_FEED = 101; // browser code
    private final int EDIT_CODE = 102; // edit feed code
    private final String TAG = "FeedActivity"; // log tag
    private ArticleLoader loader;
    private InputStream myInput;
    private String provider;


    private ProgressBar spinner;
    /**
     * initializes UI with appropriate elements passed from main
     * @param savedInstanceState current state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        spinner=(ProgressBar)findViewById(R.id.progressBar1);
        //  spinner.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                spinner.setVisibility(View.INVISIBLE);
            }
        }, 4000);

        listView = findViewById(R.id.feedList);
        itemList = new ArrayList<>(); // initialize lists
        drawableList = new ArrayList<>();
        readUser();
        myInput = getResources().openRawResource(R.raw.newsapi_org);
        imageAdapter = new ImageAdapter(this, itemList, drawableList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * starts browser activity based on what item was clicked
             * @param adapterView parent view
             * @param view current view
             * @param i index
             * @param l id
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(FeedActivity.this, BrowserActivity.class);
                String target = adapterView.getItemAtPosition(i).toString();
                Article article = null;
                for (Article temp : itemList) { // locates article in listview
                    if (temp.getArticleTitle().equals(target)) {
                        article = temp;
                    }
                }
                if (article != null) { // starts browser activity
                    loader.cancel(true);
                    drawableList.clear();
                    itemList.clear();
                    imageAdapter.notifyDataSetChanged();
                    if(currentUser.incrementLevelCount()){
                        makeNotification();
                    }
                    saveUser();
                    intent.putExtra("article", article);
                    startActivityForResult(intent, BROWSER_CODE_FEED);
                } else {
                    Log.e(TAG, "invalid url");
                }
            }

        });

        listView.setAdapter(imageAdapter);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navMenuFeed);
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
                        loader.cancel(true);
                        intent = new Intent(FeedActivity.this, TipsActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.bookmarkButton:
                        loader.cancel(true);
                        intent = new Intent(FeedActivity.this, FavoriteActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.newsButton:
                        loader.cancel(true);
                        intent = new Intent(FeedActivity.this, MainActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                }
                return true;
            }
        });


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

        String state;
        state = "/" + ((MyLocationListener) locationListener).getCurrentState();

        String levelWord;
        switch (currentUser.getLevel()) {
            case 1:
            default:
                levelWord = "/beginner";
                break;
            case 2:
                levelWord = "/intermediate";
                break;
            case 3:
                levelWord = "/advanced";
                break;
            case 4:
                levelWord = "/expert";
                break;
        }

        String duplicate = "";

        readUser();
        if (currentUser.getUserFilter().contains("/beginner")) {
            duplicate = "/beginner";
        } else if (currentUser.getUserFilter().contains("/intermediate")) {
            duplicate = "/intermediate";
        } else if (currentUser.getUserFilter().contains("/advanced")) {
            duplicate = "/advanced";
        } else if (currentUser.getUserFilter().contains("/expert")) {
            duplicate = "/expert";
        }

        //removes duplicates to take care of level updates (e.g., level ups)
        if (!duplicate.equals("")) {
            currentUser.getUserFilter().remove(duplicate);
            currentUser.addToFilter(levelWord);
        }

        if (duplicate.equals("")) {
            currentUser.addToFilter(levelWord);
        }

        if (currentUser.getUserFilter().contains("/null")) {
            currentUser.getUserFilter().remove("/null");
        }

        if (currentUser.isLocation()) {
            if (currentUser.getState().equals("")) {
                currentUser.setState(state);
                currentUser.addToFilter(state);
            }
            //to update changes in state
            if (!currentUser.getState().equals(state)) {
                currentUser.getUserFilter().remove(currentUser.getState());
                currentUser.setState(state);
                currentUser.addToFilter(state);
            }
            saveUser();
        }

        loader = new ArticleLoader(imageAdapter, itemList, drawableList, myInput);

        if (currentUser.isUsage()){
            loader.execute(currentUser.getFilterQuery());

        } else {
            loader.execute(currentUser.getStringFilter());

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

    /**
     * creates the main_menu
     * @param menu menu.xml file.
     * @return true if action is successful.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.feed_menu, menu);
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
        switch(menuId) {
            case R.id.editFeedButton:
                loader.cancel(true);
                drawableList.clear();
                itemList.clear();
                imageAdapter.notifyDataSetChanged();
                intent = new Intent(FeedActivity.this, EditFeedActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, EDIT_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * results method for browser and editFeed activity
     * @param requestCode request code
     * @param resultCode result code
     * @param data intent from calling activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        UsageActivity location = new UsageActivity();
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == BROWSER_CODE_FEED || requestCode == EDIT_CODE) { // updates user and listView
                readUser();
                ArticleLoader loader = new ArticleLoader(imageAdapter, itemList, drawableList, myInput);
                loader.execute(currentUser.getStringFilter()); // populates listView
                saveUser();

            }
        }
    }

    /**
     * makes a notification if a user levels upp
     */
    private void makeNotification(){
        readUser();
        if (currentUser.isNotifications()) {

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

            } else {
                Notification.Builder builder = new Notification.Builder(getApplicationContext());
                builder.setSmallIcon(android.R.drawable.alert_dark_frame);
                builder.setContentTitle(title);
                builder.setContentText(message);
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
                builder.setPriority(Notification.PRIORITY_MAX); // this is deprecated
                notificationManager.notify(1004, builder.build());
            }
        }

    }


}
