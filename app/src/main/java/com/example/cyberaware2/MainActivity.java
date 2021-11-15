  package com.example.cyberaware2;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.io.FileInputStream;


// https://developer.android.com/training/articles/security-ssl.html?source=post_page---------------------------#UnknownCa
/*
    request codes
    private final int SETTINGS_CODE = 1;        // code for settings
    private final int BROWSER_CODE = 2;         // code for browser
    private final int TIP_CODE = 200;           // tip code
    private final int BROWSER_CODE_FEED = 101;  // browser code
    private final int EDIT_CODE = 102;          // edit feed code
    private final int BROWSER_CODE_FAV = 12;    // browser code
    Notification id
    insecure wifi  1000
    location       1001
    fireBase       1002
    recommendation 1003
    levelup        1004
 */

/**
 * This class displays articles from a ListView and acts as the hub page for the app
 * AppCompatActivity
 */
public class MainActivity extends AppCompatActivity {

    private ListView listView;                  // displays articles
    private ImageAdapter imageAdapter;          // a custom adapter that displays images, title, descriptions
    private final String TAG = "MainActivity";  // log tag
    private User currentUser;                   // current user object
    private ArrayList<Article> itemList;        // arrayList of article objects
    private ArrayList<Drawable> drawableList;   // arrayList of drawables
    private final int SETTINGS_CODE = 1;        // code for settings
    private final int BROWSER_CODE = 2;         // code for browser
    private Intent monitorIntent;               // intent for the monitor class
    private ArticleLoader loader;
    private InputStream myInput;



    private ProgressBar spinner;
    /**
     * On create method for the page
     * @param savedInstanceState current state unused
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner=(ProgressBar)findViewById(R.id.progressBar1);
      //  spinner.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                spinner.setVisibility(View.INVISIBLE);
            }
        }, 3000);


        NetworkMonitor monitor = new NetworkMonitor(this);
        monitorIntent = new Intent(this, monitor.getClass());
        if (!isMyServiceRunning(monitorIntent.getClass())) {
            startService(monitorIntent);
        }
        myInput = getResources().openRawResource(R.raw.newsapi_org);

        itemList = new ArrayList<>(); // initialize article list
        drawableList = new ArrayList<>(); // initialize drawable list
        listView = (ListView) findViewById(R.id.articleList); // listView
        imageAdapter = new ImageAdapter(this, itemList, drawableList);

        readUser();
        if (currentUser == null) { // this is just a backup in case something fails
            currentUser = new User("error"); // creates a default user
        }
        listView.setAdapter(imageAdapter);  // initialize image adapter
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * click listener for listView that takes you to the respective article.
             * @param adapterView parentView
             * @param view currentView
             * @param i position
             * @param l id
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
                String target = adapterView.getItemAtPosition(i).toString().trim();

                for (Article article: itemList) { // finds target article and starts browser activity
                    if (article.getArticleTitle().equals(target)) {
                        loader.cancel(true);
                        if(currentUser.incrementLevelCount()){
                            makeNotification();
                        }
                        drawableList.clear();
                        itemList.clear();
                        imageAdapter.notifyDataSetChanged();
                        intent.putExtra("article", article);
                        saveUser();
                        startActivityForResult(intent, BROWSER_CODE);
                        break;
                    }
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.navMenuMain);
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
                        intent = new Intent(MainActivity.this, TipsActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.bookmarkButton:
                        loader.cancel(true);
                        intent = new Intent(MainActivity.this, FavoriteActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.myFeedButton:
                        loader.cancel(true);
                        intent = new Intent(MainActivity.this, FeedActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                }
                return true;
            }
        });

        loader = new ArticleLoader(imageAdapter, itemList, drawableList, myInput);
        Log.e(TAG, "execute");

        loader.execute("cybersecurity"); // loads the listView
      //  spinner.setVisibility(View.GONE);
    }

    /**
     * saves user data to local memory
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
     * listener for the search button. populates list based on user input
     * @param view current view
     * @return true
     */
    public boolean searchListener (View view){
        hideKeyboardFrom(getApplicationContext(), view);
        EditText editText = findViewById(R.id.search_bar);
        String keyword = editText.getText().toString().trim();
        keyword = keyword.replace(" ", "+");
        if (!keyword.isEmpty()) {
            currentUser.addSearchHistory(keyword); // update keyword;
            saveUser();
            loader.cancel(true);
            ArticleLoader loader = new ArticleLoader(imageAdapter, itemList, drawableList, myInput);
            loader.execute(keyword);
        } else {
            Log.e(TAG, "illegal");
        }
        return true;
    }

    public void checkSearchString (){
        EditText editText = findViewById(R.id.search_bar);
        String keyword = editText.getText().toString().trim();
        keyword = keyword.replace(" ", "+");
        if (keyword.isEmpty()) {
            keyword = "cybersecurity";
        }
        ArticleLoader loader = new ArticleLoader(imageAdapter, itemList, drawableList, myInput);
        loader.execute(keyword);
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * creates the main_menu
     * @param menu menu.xml file.
     * @return true if action is successful.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * OnClickListener for the options menu
     * @param item the button that was pressed
     * @return true if action is successful
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        Intent intent;
        switch (menuId) {
            case R.id.usageButton:
                loader.cancel(true);
                drawableList.clear();
                itemList.clear();
                imageAdapter.notifyDataSetChanged();
                saveUser();
                intent = new Intent(MainActivity.this, UsageActivity.class);
                startActivity(intent);
                return true;
            case R.id.settingsItem: // starts settings activity
                loader.cancel(true);
                drawableList.clear();
                itemList.clear();
                imageAdapter.notifyDataSetChanged();
                saveUser();
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_CODE);
                return true;
            case R.id.wifiButton:
                loader.cancel(true);
                drawableList.clear();
                itemList.clear();
                imageAdapter.notifyDataSetChanged();
                saveUser();
                intent = new Intent(MainActivity.this, WifiActivity.class);
                startActivity(intent);
                return true;
            default:
                Log.e(TAG, "onOptionsItemSelected: Invalid item selected");
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * This method writes user data to local storage
     * @param requestCode request code
     * @param resultCode result code
     * @param data an intent containing variables
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == SETTINGS_CODE || requestCode == BROWSER_CODE) {
                readUser();
                loader = new ArticleLoader(imageAdapter, itemList, drawableList);
                loader.execute("cybersecurity"); // loads the listView
            }
        }
    }

    /**
     * this class prevents service duplication
     * @param serviceClass service to be checked
     * @return true if the service is running
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    /**
     * stops the the monitor service so it can be reactivated outside the main app.
     * This allows the service to run when the app is not in the foreground.
     */
    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            stopService(monitorIntent);
        }
        super.onDestroy();
    }


    /**
     * levels up user
     */
    private void makeNotification(){
        if (currentUser.isNotifications()) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Resources res = getResources();
            String[] articleArray = res.getStringArray(R.array.user_filter_types);
            String title = "Congratulations";
            Log.e(TAG, "" + currentUser.getLevel());
            String message = "Your skill level is now " + (articleArray[(currentUser.getLevel() - 1)]) + "!";
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
