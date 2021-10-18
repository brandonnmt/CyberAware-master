package com.example.cyberaware2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * class for making article recommendations
 */
public class RecommendationReceiver extends BroadcastReceiver {
    private User currentUser;
    private boolean isNotification;

    private void setNotification(boolean notification) {
        isNotification = notification;
    }

    private boolean getNotification() {
        return this.isNotification;
    }

    /**
     * onReceive method
     * @param context current context
     * @param intent current intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        try { // read user info from local storage
            FileInputStream fIn = context.openFileInput("mySettings.txt");
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


        Context myContext = context.getApplicationContext();
        if (currentUser != null) {
            if (currentUser.isUsage()) { // makes recommendation with search history
                new RLoader(myContext).execute(currentUser.getFilterQuery());
            } else { // recommendation without search history
                new RLoader(myContext).execute(currentUser.getStringFilter());
            }

            if (currentUser.isNotifications()) {
                setNotification(true);
            } else {
                setNotification(false);
            }

        }
    }

    /**
     * class nearly identical to ArticleLoader but without images
     */
    public class RLoader extends AsyncTask<String, String, String> {
        private ArrayList<Article> articleList;   // list of articles
        private final String TAG = "RecommendLoader";   // Log TAG
        private Context myContext; // current context

        /**
         * constructor
         * @param context current context
         */
        public RLoader(Context context) {
            this.articleList = new ArrayList<>();
            myContext = context;
        }

        /**
         * this method parses json from the news api
         * @param strings keywords or keyword
         * @return a string containing output
         */
        protected String doInBackground(String... strings) {
            String keyword = strings[0]; // gets a keyword
            String[] keywordSplit = keyword.split(" ");
            URL url = null;
            HttpsURLConnection connection = null;
            try{
                if (!keywordSplit[0].contains("/")) {
                    url = new URL("https://newsapi.org/v2/everything?q=" + keywordSplit[0] +
                            "&domains=thehackernews.com,threatpost.com" +
                            "&language=en&sortBy=relevancy,publishedAt&pageSize=50&apiKey=ef94cb8dc7df45e5ab0cd190a281e762"); // my url
                } else {
                    url = new URL("https://newsapi.org/v2/everything?q=cybersecurity" +
                            "&domains=thehackernews.com,threatpost.com" +
                            "&language=en&sortBy=relevancy,publishedAt&pageSize=50&apiKey=ef94cb8dc7df45e5ab0cd190a281e762");
                }
                connection = (HttpsURLConnection) url.openConnection();
                //https://stackoverflow.com/questions/26969550/httpurlconnection-server-return-http-403-forbidden
                connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:221.0) Gecko/20100101 Firefox/31.0");


                // https://developer.android.com/training/articles/security-ssl.html?source=post_page---------------------------#UnknownCa
                /*
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate ca;
                    InputStream myInput = myContext.getResources().openRawResource(R.raw.newsapi_org);
                    InputStream testInput = myContext.getResources().openRawResource(R.raw.test);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(testInput));
                    Log.e(TAG, "test: " + reader.readLine());
                    try{
                        ca = cf.generateCertificate(myInput);
                    } finally {
                        myInput.close();
                    }
                    String keyStoreType = KeyStore.getDefaultType();
                    KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("ca", ca);

                    String algorithm = TrustManagerFactory.getDefaultAlgorithm();
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
                    tmf.init(keyStore);
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, tmf.getTrustManagers(), null);
                    connection.setSSLSocketFactory(context.getSocketFactory());
                }
                */
                connection.setRequestProperty("content-type", "application/json;  charset=utf-8");
                connection.setRequestProperty("Content-Language", "en-US"); // sets language to english
                connection.setUseCaches (false);
                connection.setDoInput(true);
                connection.setDoOutput(false);
                InputStream inputStream;
                int status = connection.getResponseCode();
                if(status != HttpsURLConnection.HTTP_OK){
                    inputStream = connection.getErrorStream();
                } else {
                    inputStream = connection.getInputStream();
                }
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuffer response = new StringBuffer();
                // gets input from url
                while((line = rd.readLine()) != null){
                    response.append(line);
                    response.append('\r');
                }
                Log.e(TAG, "response2 " + response.toString());
                JSONObject json = new JSONObject(response.toString()); // create json object from input
                JSONArray data = json.getJSONArray("articles"); // gets articles from json
                for(int i = 0; i < data.length(); i++){
                    String name = data.getJSONObject(i).getString("title");
                    String link = data.getJSONObject(i).getString("url");
                    String linkImage = data.getJSONObject(i).getString("urlToImage");
                    String content = data.getJSONObject(i).getString("description");
                    if(linkImage != null) {
                        articleList.add(new Article(name, link, linkImage, content));
                    } else {
                        Log.e(TAG, "error");
                    }
                }
                rd.close();
                return response.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                /*
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
                */
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
            }
            return null;
        }


        /**
         * sends notification based on user input
         * @param s,
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Random rand = new Random();
            Intent intent = new Intent(myContext, SpecialBrowser.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(myContext);

            if (getNotification()) {

                if (articleList.size() > 0) {
                    Article article = articleList.get(rand.nextInt(articleList.size()));
                    intent.putExtra("article", article);
                    stackBuilder.addNextIntentWithParentStack(intent);
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationManager notificationManager = (NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    String title = myContext.getString(R.string.recommendation_title);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel("recommendation", "recommendation", importance);
                        channel.setDescription("Recommended Article");
                        // Register the channel with the system; you can't change the importance
                        // or other notification behaviors after this
                        notificationManager.createNotificationChannel(channel);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(myContext, channel.getId())
                                .setSmallIcon(android.R.drawable.btn_star)
                                .setContentTitle(title)
                                .setContentText(article.getArticleTitle())
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(pendingIntent)
                                .setDefaults(NotificationCompat.DEFAULT_VIBRATE);
                        notificationManager.notify(1003, builder.build());
                    } else {

                        Notification.Builder builder = new Notification.Builder(myContext);
                        builder.setSmallIcon(android.R.drawable.btn_star);
                        builder.setContentTitle(title);
                        builder.setContentText(article.getArticleTitle());
                        builder.setDefaults(Notification.DEFAULT_VIBRATE);
                        builder.setContentIntent(pendingIntent);
                        builder.setPriority(Notification.PRIORITY_MAX); // this is deprecated
                        notificationManager.notify(1003, builder.build());
                    }
                }
            }
        }

    }

}
