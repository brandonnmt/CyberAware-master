package com.example.cyberaware2;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * first time user creation activity. This only runs when a user initially opens the app.
 * Created by Vhl2 on 6/14/2019.
 */

public class SignUpActivity extends AppCompatActivity {

    private final String TAG = "SignUpActivity";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private int role;

    /**
     * On create method for the page
     * @param savedInstanceState current state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        role = 0;
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setNegativeButton(R.string.no, null);
        alertBuilder.setTitle(R.string.location).setMessage(R.string.location_prompt);
        alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions(SignUpActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        });
        alertBuilder.show();
        //Toast.makeText(this, R.string.usage_prompt, Toast.LENGTH_SHORT).show();
    }

    /**
     * onClick listener that sets users skill level. This method also highlights the selected
     * button.
     * @param view current view
     * @return true
     */
    public Boolean roleClick(View view){
        Button button = findViewById(R.id.button8);
        button.setBackgroundResource(android.R.drawable.btn_default);
        button = findViewById(R.id.button9);
        button.setBackgroundResource(android.R.drawable.btn_default);
        button = findViewById(R.id.button10);
        button.setBackgroundResource(android.R.drawable.btn_default);
        button = findViewById(R.id.button11);
        button.setBackgroundResource(android.R.drawable.btn_default);

        view.setBackgroundResource(R.color.colorAccent);
        switch (view.getTag().toString()) {
            case "Beginner":
                role = 1;
                break;
            case "Intermediate":
                role = 2;
                break;
            case "Advanced":
                role = 3;
                break;
            case "Expert":
                role = 4;
                break;
        }
       return true;
    }


    /**
     * This method checks for valid user and starts MainActivity
     * @return True if action is successful
     */
    public boolean signUpListener(View view){
        Intent intent = new Intent(SignUpActivity.this, ActivityTopic.class);

        EditText nameText = findViewById(R.id.newUsername);
        String username = nameText.getText().toString();
        if(role != 0) {
            User newUser = new User(username, role);
            if (!username.equals("")) { // checks for valid usernames
                getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                        .putBoolean("isFirstRun", false).apply(); // sets it so the activity only happens when a user first downloads the app
                try { // creates a new file
                    FileOutputStream fOut = openFileOutput("mySettings.txt", MODE_PRIVATE);
                    ObjectOutputStream oOut = new ObjectOutputStream(fOut);
                    oOut.writeObject(newUser);
                    oOut.close();
                    fOut.close();
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "file not found");

                } catch (IOException e) {
                    Log.e(TAG, "Error initializing stream");
                }

                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.blank_name, Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

}