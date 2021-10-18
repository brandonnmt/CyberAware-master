package com.example.cyberaware2;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;

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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * updates listViews with information from news api
 */
public class ArticleLoader extends AsyncTask <String, String, String> {
    private ImageAdapter imageAdapter;        // custom arrayAdapter
    private ArrayList<Article> articleList;   // list of articles
    private ArrayList<Drawable> drawableList; // list of drawables
    private final String TAG = "ArticleLoader";   // Log TAG
    private InputStream myInput;

    /**
     * constructor
     * @param imageAdapter current adapter
     * @param articleList current article list
     * @param drawableList current drawable list
     */
    public ArticleLoader(ImageAdapter imageAdapter, ArrayList<Article> articleList, ArrayList<Drawable> drawableList) {
        this.imageAdapter = imageAdapter;
        this.articleList = articleList;
        this.drawableList = drawableList;
        myInput = null;
        Log.e(TAG, "hello");
    }

    /**
     * constructor for adding certificate
     * @param imageAdapter current adapter
     * @param articleList current article list
     * @param drawableList current drawable list
     * @param crt current input stream for the certificate
     */
    public ArticleLoader(ImageAdapter imageAdapter, ArrayList<Article> articleList, ArrayList<Drawable> drawableList, InputStream crt) {
        this.imageAdapter = imageAdapter;
        this.articleList = articleList;
        this.drawableList = drawableList;
        this.myInput = crt;
    }



    /**
     * this method parses json from the news api
     * @param strings keywords or keyword
     * @return a string containing output
     */
    @Override
    protected String doInBackground(String... strings) {
        String keyword = strings[0]; // gets a keyword
        String[] keywordSplit = null;
        String beginner = "&domains=cybersecurity-insiders.com,thehackernews.com";
        String intermediate = "&domains=thecyberwire.com,helpnetsecurity.com";
        String advanced = "&domains=securityweek.com,threatpost.com";
        String expert = "&domains=csoonline.com,bankinfosecurity.com";
        int levelIndex = 0;
        int stateIndex = 0;
        ArrayList<Article> tempAList = new ArrayList<>();
        ArrayList<Drawable> tempDList = new ArrayList<>();
        HttpsURLConnection connection = null;
        URL url = null;

        //implements levels for personal feed
        if (keyword.contains("/")) {
            keywordSplit = keyword.split("/");
            if (keywordSplit.length == 3) {
                keywordSplit[0] = keywordSplit[0].substring(0, keywordSplit[0].length() - 4);
                keywordSplit[1] = keywordSplit[1].substring(0, keywordSplit[1].length() - 4);
                if (keywordSplit[1].contains("beginner")) {
                    levelIndex = 1;
                    stateIndex = 2;
                } else if (keywordSplit[1].contains("intermediate")) {
                    levelIndex = 1;
                    stateIndex = 2;
                } else if (keywordSplit[1].contains("advanced")) {
                    levelIndex = 1;
                    stateIndex = 2;
                } else if (keywordSplit[1].contains("expert")) {
                    levelIndex = 1;
                    stateIndex = 2;
                } else {
                    levelIndex = 2;
                    stateIndex = 1;
                }
                keywordSplit[0] = keywordSplit[0] + " OR +" + keywordSplit[stateIndex];
            }
            if (keywordSplit.length == 2) {
                keywordSplit[0] = keywordSplit[0].substring(0, keywordSplit[0].length() - 4);
            }
        }

        try{
            //for personalized feed
            if (keywordSplit != null) {
                //for location and level
                if (keywordSplit.length == 3) {
                    switch (keywordSplit[levelIndex]) {
                        case "beginner":
                        default:
                            url = new URL("https://newsapi.org/v2/everything?" + "qInTitle=+" + keywordSplit[stateIndex] + "&q=" + keywordSplit[0] +
                                    beginner +
                                    "&language=en&sortBy=relevancy,publishedAt&pageSize=20&apiKey=ef94cb8dc7df45e5ab0cd190a281e762"); // my url
                            break;
                        case "intermediate":
                            url = new URL("https://newsapi.org/v2/everything?" + "qInTitle=+" + keywordSplit[stateIndex] + "&q=" + keywordSplit[0] +
                                    intermediate +
                                    "&language=en&sortBy=relevancy,publishedAt&pageSize=20&apiKey=ef94cb8dc7df45e5ab0cd190a281e762"); // my url
                            break;
                        case "advanced":
                            url = new URL("https://newsapi.org/v2/everything?" + "qInTitle=+" + keywordSplit[stateIndex] + "&q=" + keywordSplit[0] +
                                    advanced +
                                    "&language=en&sortBy=relevancy,publishedAt&pageSize=20&apiKey=ef94cb8dc7df45e5ab0cd190a281e762"); // my url
                            break;
                        case "expert":
                            url = new URL("https://newsapi.org/v2/everything?" + "qInTitle=+" + keywordSplit[stateIndex] + "&q=" + keywordSplit[0] +
                                    expert +
                                    "&language=en&sortBy=relevancy,publishedAt&pageSize=20&apiKey=ef94cb8dc7df45e5ab0cd190a281e762"); // my url
                            break;
                    }
                }
                //for just level
                if (keywordSplit.length == 2) {
                    switch (keywordSplit[1]) {
                        case "beginner":
                        default:
                            url = new URL("https://newsapi.org/v2/everything?q=" + keywordSplit[0] +
                                    beginner +
                                    "&language=en&sortBy=relevancy,publishedAt&pageSize=20&apiKey=ef94cb8dc7df45e5ab0cd190a281e762"); // my url
                            break;
                        case "intermediate":
                            url = new URL("https://newsapi.org/v2/everything?q=" + keywordSplit[0] +
                                    intermediate +
                                    "&language=en&sortBy=relevancy,publishedAt&pageSize=20&apiKey=ef94cb8dc7df45e5ab0cd190a281e762"); // my url
                            break;
                        case "advanced":
                            url = new URL("https://newsapi.org/v2/everything?q=" + keywordSplit[0] +
                                    advanced +
                                    "&language=en&sortBy=relevancy,publishedAt&pageSize=20&apiKey=ef94cb8dc7df45e5ab0cd190a281e762"); // my url
                            break;
                        case "expert":
                            url = new URL("https://newsapi.org/v2/everything?q=" + keywordSplit[0] +
                                    expert +
                                    "&language=en&sortBy=relevancy,publishedAt&pageSize=20&apiKey=ef94cb8dc7df45e5ab0cd190a281e762"); // my url
                            break;
                    }
                }
            } else {  //the main feed - (i.e., no personalized filter)
                url = new URL("https://newsapi.org/v2/everything?q=cybersecurity" +
                            "&domains=thehackernews.com,threatpost.com" +
                            "&language=en&sortBy=relevancy,publishedAt&pageSize=20&apiKey=ef94cb8dc7df45e5ab0cd190a281e762"); // my url
            }

            connection = (HttpsURLConnection) url.openConnection();
            // https://stackoverflow.com/questions/26969550/httpurlconnection-server-return-http-403-forbidden
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:221.0) Gecko/20100101 Firefox/31.0");

            // https://developer.android.com/training/articles/security-ssl.html?source=post_page---------------------------#UnknownCa
            /*
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509"); //check get instance
                Certificate ca;
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
            String message = connection.getResponseMessage();

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
                if(isCancelled()){ // kills the thread
                    rd.close();
                    return null;
                }
            }
            Log.e(TAG, "response1 " + response.toString());
            JSONObject json = new JSONObject(response.toString()); // create json object from input
            JSONArray data = json.getJSONArray("articles"); // gets articles from json
            for(int i = 0; i < data.length(); i++){
                Log.e(TAG, "hello2");

                try { // this should handle bad urls for images
                    String name = data.getJSONObject(i).getString("title");
                    String link = data.getJSONObject(i).getString("url");
                    String linkImage = data.getJSONObject(i).getString("urlToImage");
                    InputStream is = (InputStream) new URL(linkImage).getContent();
                    Drawable drawable = Drawable.createFromStream(is, "src name");
                    String content = data.getJSONObject(i).getString("description");
                    tempDList.add(drawable);
                    tempAList.add(new Article(name, link, linkImage, content));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                    Log.e(TAG, "moving onto next url");
                }
                if (isCancelled()) { // kills the thread
                    Log.e(TAG, "bye");
                    rd.close();
                    return null;
                }

            }

            articleList.clear(); // clear arrayLists
            drawableList.clear();
            articleList.addAll(tempAList);
            drawableList.addAll(tempDList);
            Log.e(TAG, "drawable " + drawableList.size() + " article " + articleList.size());
            rd.close();
            return response.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "drawable " + drawableList.size() + " article " + articleList.size());
            return null;
        } catch (IOException e) {
            Log.e(TAG, "foxtrot");
            Log.e(TAG, "drawable " + drawableList.size() + " article " + articleList.size());

            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            Log.e(TAG, "echo");
            e.printStackTrace();
        /*
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
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
     * updates the imageAdapter
     * @param s
     */
    @Override
    protected void onPostExecute(String s) {
        imageAdapter.notifyDataSetChanged();
        super.onPostExecute(s);
        Log.e(TAG, "test2");
    }

}