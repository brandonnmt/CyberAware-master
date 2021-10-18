package com.example.cyberaware2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * displays tips
 */
public class TipActivity extends AppCompatActivity {
    /**
     * On create method for the page
     * @param savedInstanceState current state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip);
        Intent intent = getIntent();
        Tip currentTip = (Tip) intent.getSerializableExtra("tip");
        TextView contentView = (TextView) findViewById(R.id.tipInfo);
        TextView titleView = (TextView) findViewById(R.id.tipTitle);
        titleView.setText(currentTip.getTitle());
        contentView.setText(currentTip.getContent());
        BottomNavigationView bottomNavigationView = findViewById(R.id.navMenuTip);
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
                        intent = new Intent(TipActivity.this, TipsActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.bookmarkButton:
                        intent = new Intent(TipActivity.this, FavoriteActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.myFeedButton:
                        intent = new Intent(TipActivity.this, FeedActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.newsButton:
                        intent = new Intent(TipActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                }
                return true;
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
        switch(menuId) {
            case R.id.doneButton:
                intent = new Intent(TipActivity.this, TipsActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
