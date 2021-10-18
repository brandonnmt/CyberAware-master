package com.example.cyberaware2;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * lets user edit what shows up on their personal feed
 */
public class EditFeedActivity extends AppCompatActivity {
    private User currentUser; // currentUser
    private ListView filterList; // listView of possible filters
    private CheckAdapter checkAdapter;
    private MyLocationListener location;
    private String TAG = "EditFeedActivity"; // log tag
    private ArrayList<Boolean> checkedList; // arrayList representing what variables are checked

    /**
     * On create method for the page
     * @param savedInstanceState current state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_feed);

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

        filterList = findViewById(R.id.filterList);
        Resources res = getResources();
        final String[] articleTypeArray = res.getStringArray(R.array.article_types);
        ArrayList<?> temp = currentUser.getUserFilter();
        checkedList = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < articleTypeArray.length; i++) { // initializes checked list
            if (index < temp.size()) {
                Log.e(TAG, "temp: " + temp.get(index) + " article: " + articleTypeArray[i]);
                if (temp.get(index).equals(articleTypeArray[i])) {
                    checkedList.add(true);
                    index++;
                } else {
                    checkedList.add(false);
                }
            } else {
                checkedList.add(false);
            }
        }
        checkAdapter = new CheckAdapter(this, checkedList, articleTypeArray);
        filterList.setAdapter(checkAdapter);

        filterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * checks topic in list and updates checkedLIst
             * @param parent parent view
             * @param view current view
             * @param position index
             * @param id id
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // change the checkbox state
                Log.e(TAG, articleTypeArray[position] + " before pos: " + position + "state" + checkedList.get(position));
                checkedList.set(position, !checkedList.get(position));
                Log.e(TAG, articleTypeArray[position] + " after pos: " + position + "state" + checkedList.get(position));
                checkAdapter.notifyDataSetChanged();
                Log.e(TAG, "position: " + position);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.navMenuEdit);
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
                        intent = new Intent(EditFeedActivity.this, TipsActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.bookmarkButton:
                        intent = new Intent(EditFeedActivity.this, FavoriteActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.myFeedButton: // this was called from feed
                        intent = new Intent();
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                        return true;
                    case R.id.newsButton:
                        intent = new Intent(EditFeedActivity.this, MainActivity.class);
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
     * creates the main_menu
     * @param menu menu.xml file.
     * @return true if action is successful.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.setting_menu, menu);
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
        switch (menuId) {
            case R.id.backButton:
                Intent settingsIntent = new Intent();
                saveUser();
                setResult(Activity.RESULT_OK, settingsIntent);
                finish();
                return true;
            case R.id.saveButton:
                saveListener();
                return true;
            case R.id.resetButton:
                for (int i = 0; i < checkedList.size(); i++) {
                    if (checkedList.get(i)) {
                        filterList.performItemClick(filterList.getChildAt(i), i, i);
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * updates user filter and passes it back to FeedActivity to be saved in local storage.
     * the method looks through checkedList and updates filters accordingly
     */
    public void saveListener() {
        ArrayList<String> newFilter = new ArrayList<>();
        Resources res = getResources();
        String[] articleArray = res.getStringArray(R.array.article_types);
        for (int i = 0; i < articleArray.length; i++) {
            if (checkedList.get(i)) {
                newFilter.add(articleArray[i]);
            }
        }
        currentUser.updateFilter(newFilter);
        Log.e(TAG, "alert " + currentUser.getStringFilter());
        Intent settingsIntent = new Intent();
        saveUser();
        setResult(Activity.RESULT_OK, settingsIntent);
        finish();
    }
}





