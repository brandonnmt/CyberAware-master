package com.example.cyberaware2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class WifiActivity extends AppCompatActivity {
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private User currentUser;
    private final String TAG = "WifiActivity"; // log tag

    /**
     * On create method for the page
     * @param savedInstanceState current state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        listView = (ListView) findViewById(R.id.wifiList);
        listView.setLongClickable(true);
        final ArrayList<String> whitelist = new ArrayList<>();
        TextView wifiName = findViewById(R.id.wifiName);
        TextView wifiState = findViewById(R.id.wifiState);

        // https://stackoverflow.com/questions/25662761/how-to-differentiate-open-and-secure-wifi-networks-without-connecting-to-it-in-a
        WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> networkList = wifiMan.getScanResults();

        String ssid = "";

        //https://stackoverflow.com/questions/53944755/how-to-get-current-wifi-connection-name-in-android-pie9-devices
        ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            ssid = wifiInfo.getSSID();
            ssid = ssid.replace("\"", "");
            wifiName.setText(ssid);
        }

        readUser();
        if (networkList != null) {
            for (ScanResult network : networkList) {
                if (network.SSID.equals(ssid)) {
                    String capabilities = network.capabilities;
                    if (capabilities.toUpperCase().contains("WEP")) {
                        wifiState.setText("Your wifi network uses WEP encryption and is secure");
                    } else if (capabilities.toUpperCase().contains("WPA")) {
                        wifiState.setText("Your wifi network uses WPA encryption and is secure");
                    } else {
                        wifiState.setText("Your wifi network is insecure \n No encryption for your network was been detected");
                        if (!currentUser.getWhitelist().contains(ssid)) {
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                            alertBuilder.setNegativeButton(R.string.no, null);
                            alertBuilder.setTitle("Insecure Wifi").setMessage(
                                    "We have detected that your current wifi \"" + ssid + "\" is an insecure network. " +
                                    "Would you like to whitelist the network?"
                            );
                            final String finalSsid = ssid;
                            alertBuilder.setNegativeButton(R.string.no, null);
                            alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (!currentUser.getWhitelist().contains(finalSsid)) {
                                        currentUser.addToWhitelist(finalSsid);
                                        saveUser();
                                        whitelist.add("Wifi Network: " + finalSsid);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                            alertBuilder.show();
                        }
                    }
                    break;
                }
            }
        }

        if (currentUser.getWhitelist() != null) {
            for (String wifi : currentUser.getWhitelist()) {
                whitelist.add("Wifi Network: " + wifi);
            }
        }

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, long id) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(WifiActivity.this);
                alertBuilder.setNegativeButton(R.string.no, null);
                alertBuilder.setTitle("Remove Wifi?").setMessage(
                        "Would you like to remove \"" + currentUser.getWhitelist().get(pos) + "\" from your Whitelist?"
                );
                alertBuilder.setNegativeButton(R.string.no, null);
                alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        currentUser.getWhitelist().remove(pos);
                        saveUser();
                        whitelist.remove(pos);
                        adapter.notifyDataSetChanged();

                    }
                });
                alertBuilder.show();
                Log.v("long clicked","pos: " + pos);
                return true;
            }
        });

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, whitelist);
        listView.setAdapter(adapter);

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
                intent = new Intent(WifiActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

}
