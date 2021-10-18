package com.example.cyberaware2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Created by Vhl2 on 6/13/2019.
 * This page allows users to change the apps settings like notifications
 * and their username
 */

public class SettingsActivity extends AppCompatActivity {

    private User currentUser; // currentUser
    private final String TAG = "SettingsActivity"; // log tag
    private EditText editText; // username text
    private int level;

    /**
     * initializes class variables
     * @param savedInstanceState current state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        readUser();

        editText = findViewById(R.id.changeUsername);
        editText.setText(currentUser.getUserName());

        Switch usageSwitch = findViewById(R.id.usageCheck);
        usageSwitch.setChecked(currentUser.isUsage());
        usageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * switch listener for usage
             * @param buttonView current view
             * @param isChecked true if the switch is on
             */
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentUser.setUsage(isChecked);
            }
        });

        Switch locationSwitch = findViewById(R.id.locationCheck);
        locPref(locationSwitch.isChecked(), false);
        locationSwitch.setChecked(currentUser.isLocation());
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * switch listener for location
             * @param buttonView current view
             * @param isChecked true if the switch is on
             */
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentUser.setLocation(isChecked);
            }
        });

        Switch notificationsSwitch = findViewById(R.id.notificationCheck);
        notificationsSwitch.setChecked(currentUser.isNotifications());
        notificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * switch listener for notifications
             * @param buttonView current view
             * @param isChecked true if the switch is on
             */
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentUser.setNotifications(isChecked);
            }
        });

        level = currentUser.getLevel();
        Spinner roleSpinner = findViewById(R.id.levelSpinner);
        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_filter_types, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(spinAdapter);
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * changes user data
             * @param adapterView parent
             * @param view current view
             * @param i pos
             * @param l id
             */
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String target = adapterView.getItemAtPosition(i).toString();
                if (target.equals("Beginner") && currentUser.getLevel() != 1) {
                    currentUser.resetLevel(1);
                    level = 1;
                } else if (target.equals("Intermediate") && currentUser.getLevel() != 2) {
                    level = 2;
                } else if (target.equals("Advanced") && currentUser.getLevel() != 3) {
                    level = 3;
                } else if (target.equals("Expert") && currentUser.getLevel() != 4) {
                    level = 4;
                }
            }

            /**
             * stub method
             * @param adapterView parent view
             */
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        roleSpinner.setSelection(currentUser.getLevel() - 1);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navMenuSettings);
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
                        intent = new Intent(SettingsActivity.this, TipsActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.bookmarkButton:
                        intent = new Intent(SettingsActivity.this, FavoriteActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.myFeedButton:
                        intent = new Intent(SettingsActivity.this, FeedActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.newsButton:
                        intent = new Intent();
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                        return true;
                }
                return true;
            }
        });

    }

    /**
     * saves the location switch value into a shared preference file
     * @param switchValue location switch value (on or off)
     * @param changed if location switch value changed (from on to off or vice versa)
     */
    public void locPref(boolean switchValue, boolean changed) {
        SharedPreferences settings = getSharedPreferences("preferenceFile", 0);
        SharedPreferences.Editor editor = settings.edit();
        if (changed) {
            editor.remove("locationSwitch");
        }
        if (switchValue) {
            editor.putBoolean("locationSwitch", true);
        } else {
            editor.putBoolean("locationSwitch", false);
        }
        editor.apply();
    }

    /**
     * erases user history
     * @param view current view
     * @return true
     */
    public boolean wipeListener(View view){
        currentUser.eraseHistory();
        return true;
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
     * inflates exit menu
     * @param menu current menu
     * @return true if successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.exit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * onclick listener for the exit button which returns the current user
     * @param item button clicked
     * @return true if successful
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case R.id.doneButton:
                currentUser.setUserName(editText.getText().toString());
                if (currentUser.getLevel() != level){
                    currentUser.resetLevel(level);
                }
                Intent settingsIntent = new Intent();
                saveUser();
                setResult(Activity.RESULT_OK, settingsIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}



