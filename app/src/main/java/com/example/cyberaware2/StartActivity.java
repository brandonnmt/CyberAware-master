package com.example.cyberaware2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;

/**
 * This class is a base class that makes recommendations at the start of hte activity
 */
public class StartActivity extends AppCompatActivity {

    /**
     * On create method for the page
     * @param savedInstanceState current state unused
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this boolean checks shared preferences for the onBoarding activity
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        Intent intent;
        if(isFirstRun){
            intent = new Intent(StartActivity.this, SignUpActivity.class);
            startActivity(intent);
        } else {
            makeRecommendation();
            intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
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





}
