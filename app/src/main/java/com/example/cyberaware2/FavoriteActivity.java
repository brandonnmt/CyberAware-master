package com.example.cyberaware2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * displays user favorites
 */
public class FavoriteActivity extends AppCompatActivity {

    private ListView listView; // listView
    private ArrayList<Article> favList; // list of articles
    private ArrayList<Drawable> drawableList; // drawable list
    private ImageAdapter imageAdapter; // custom array adapter
    private User currentUser; // current user
    private final String TAG = "FavoriteActivity"; // log tag
    private final int BROWSER_CODE_FAV = 12; // browser code
    private LoadImage loader;

    /**
     * initializes UI
     * @param savedInstanceState current state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        listView = findViewById(R.id.favoriteList);
        readUser();
        drawableList = new ArrayList<>();
        favList = currentUser.getFavoriteList();
        if (favList.size() == 0){ // get user favorites
            favList = new ArrayList<>();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No favorites to display at this time. Please save articles to add them to favorites.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        imageAdapter = new ImageAdapter(this, favList, drawableList);
        listView.setAdapter(imageAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * start browser activity
             * @param adapterView parent
             * @param view current view
             * @param i index
             * @param l id
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(FavoriteActivity.this, BrowserActivity.class);
                String target = adapterView.getItemAtPosition(i).toString();
                Article article = null;
                for (Article temp : favList) { // finds article
                    if (temp.getArticleTitle().equals(target)) {
                        article = temp;
                    }
                }
                if (article != null) { // starts browser
                    saveUser();
                    loader.cancel(true);
                    drawableList.clear();
                    favList.clear();
                    imageAdapter.notifyDataSetChanged();
                    intent.putExtra("article", article);
                    startActivityForResult(intent, BROWSER_CODE_FAV);
                } else {
                    Log.e(TAG, "invalid url");
                }
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            /**
             * asks user if they want to delete article when long clicked
             * @param adapterView parent
             * @param view current view
             * @param pos index
             * @param id id
             * @return true
             */
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                final int index = pos;
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(FavoriteActivity.this);
                alertBuilder.setNegativeButton(R.string.no, null);
                alertBuilder.setTitle(R.string.delete).setMessage(R.string.delete_warning);
                alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    /**
                     * deletes article
                     * @param dialogInterface interface
                     * @param i id
                     */
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.e(TAG, "" + index);
                        favList.remove(index);
                        imageAdapter.notifyDataSetChanged();

                    }
                });
                alertBuilder.show(); // shows alert
                return true;
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.navMenuFav);
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
                        intent = new Intent(FavoriteActivity.this, TipsActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.myFeedButton:
                        loader.cancel(true);
                        intent = new Intent(FavoriteActivity.this, FeedActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.newsButton:
                        loader.cancel(true);
                        intent = new Intent(FavoriteActivity.this, MainActivity.class);
                        saveUser();
                        startActivity(intent);
                        finish();
                        return true;
                }
                return true;
            }
        });
        loader = new LoadImage();
        loader.execute();
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
     * on results method
     * @param requestCode request code
     * @param resultCode result code
     * @param data intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == BROWSER_CODE_FAV) {
                readUser();
                loader = new LoadImage();
                loader.execute();
            }
        }
    }

    /**
     * Dummied down version ArticleLoader since we only need image urls
     */
    public class LoadImage extends AsyncTask<String, String, String> {
        /**
         * updates drawableList
         * @param strings input not used
         * @return null
         */
        @Override
        protected String doInBackground(String... strings) {
            for(Article article : favList){ // for all articles in favList
                if(isCancelled()){
                    break;
                }
                String linkImage = article.getImage();
                InputStream is = null;
                try { // gets input stream
                    is = (InputStream) new URL(linkImage).getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Drawable drawable = Drawable.createFromStream(is, "src name");
                drawableList.add(drawable); // updates drawable list
            }
            return null;
        }

        /**
         * updates image adapter
         * @param s
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            imageAdapter.notifyDataSetChanged();
        }
    }



}
