package com.example.cyberaware2;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * onBoarding topic selection class
 */
public class ActivityTopic extends AppCompatActivity {
    private User currentUser; // currentUser
    private MyLocationListener location; // current location
    private CheckAdapter checkAdapter; // custom adapter
    private ListView filterList;
    private ArrayList<Boolean> checkedList;
    private final String TAG = "ActivityTopic";


    /**
     * On create method for the page
     * @param savedInstanceState current state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
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
        filterList = findViewById(R.id.topicList);
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
                try { // saves user data to internal storage
                    ArrayList<String> newFilter = new ArrayList<>();
                    Resources res = getResources();
                    String[] articleArray = res.getStringArray(R.array.article_types);
                    for (int i = 0; i < articleArray.length; i++) {
                        if (checkedList.get(i)) { // creates a new filter
                            newFilter.add(articleArray[i]);
                        }
                    }

                    currentUser.updateFilter(newFilter); // updates user info
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
                intent = new Intent(ActivityTopic.this, StartActivity.class); // returns to start class
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
