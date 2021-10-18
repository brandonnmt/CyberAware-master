package com.example.cyberaware2;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

/**
 * Activity displays all tips
 */
public class TipsActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<Tip> tipList;
    private ArrayAdapter<Tip> arrayAdapter;
    private final int TIP_CODE = 200;

    /**
     * On create method for the page
     * @param savedInstanceState current state unused
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);
        listView = findViewById(R.id.tipsList);
        Resources res = getResources();
        String[] tipNameArray = res.getStringArray(R.array.tip_names);
        String[] tipContentArray = res.getStringArray(R.array.tip_content);
        tipList = new ArrayList<>();
        for(int i = 0; i < tipNameArray.length; i++){
            tipList.add(new Tip(tipNameArray[i], tipContentArray[i]));
        }

        arrayAdapter = new ArrayAdapter<Tip>(this, // listView adapter
                android.R.layout.simple_list_item_1, tipList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             *
             * @param adapterView parentView
             * @param view currentView
             * @param i position
             * @param l id
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(TipsActivity.this, TipActivity.class);
                String target = adapterView.getItemAtPosition(i).toString().trim();

                for (Tip tip: tipList) { // finds target article and starts browser activity
                    if (tip.getTitle().equals(target)) {
                        intent.putExtra("tip", tip);
                        startActivity(intent);
                        finish();
                        break;
                    }
                }
            }
        });
        listView.setAdapter(arrayAdapter);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navMenuTips);
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
                    case R.id.bookmarkButton:
                        intent = new Intent(TipsActivity.this, FavoriteActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.myFeedButton:
                        intent = new Intent(TipsActivity.this, FeedActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.newsButton:
                        intent = new Intent(TipsActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                }
                return true;
            }
        });
    }
}
