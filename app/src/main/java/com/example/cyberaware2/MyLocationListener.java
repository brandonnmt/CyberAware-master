package com.example.cyberaware2;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * This class tracks current longitude, latitude, and city.
 */

// crammer longitude and latitude
// 34.0663234, -106.9050666
// 34.0662234, -106.9050263

public class MyLocationListener implements LocationListener {
    private final String TAG = "MyLocationListener"; // log tag
    private Context context; // current context
    private String currentCity; // current city
    private String currentState; //current state
    private String currentCountry; //current country
    private double longitude;
    private double latitude;


    /**
     * constructor
     * @param context current context
     */
    MyLocationListener(Context context){
        this.context = context;
        currentCity = "location unavailable";
        longitude = 0.0;
        latitude = 0.0;
    }

    /**
     * current city's getter
     * @return current city
     */
    public String getCurrentCity() {
        return currentCity;
    }

    /**
     * current state's getter
     * @return current state
     */
    public String getCurrentState() {
        return currentState;
    }

    /**
     * current country's getter
     * @return current country
     */
    public String getCurrentCountry() {
        return currentCountry;
    }

    /**
     * Longitude getter
     * @return double representing longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * latitude getter
     * @return double representing latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * gets longitude and latitude to find current city
     * @param loc current location
     */
    @Override
    public void onLocationChanged(Location loc) {
        longitude = loc.getLongitude();
        latitude = loc.getLatitude();
        Log.e(TAG, "Longitude: " + longitude);
        Log.e(TAG, "Latitude: " + latitude);

        // crammer longitude and latitude
        // 34.0670488, -106.9051413 center
        // 34.0666752, -106.9051282 south
        // 34.0673835, -106.9051980 north
        // 34.0670491, -106.9048989 east
        // 34.0670702, -106.9054109 west

        //              north                          south                      west                      east
        if(longitude >= -106.9051980  && longitude <= -106.9051282 && latitude <= 34.0670702 && latitude >= 34.0670135) {
            context.startService(new Intent(context, LocationReceiver.class));
        }

        // get city name
        String cityName = null;
        String stateName = null;
        String countryName = null;
        Geocoder gcd = new Geocoder(context,  Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(latitude,
                    longitude, 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
                stateName = addresses.get(0).getAdminArea();
                countryName = addresses.get(0).getCountryName();
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.currentCity = cityName;
        this.currentState = stateName;
        this.currentCountry = countryName;
    }


    /**
     * stub
     * @param s
     * @param i
     * @param bundle
     */
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    /**
     * stub
     * @param s
     */
    @Override
    public void onProviderEnabled(String s) { }

    /**
     * stub
     * @param s
     */
    @Override
    public void onProviderDisabled(String s) { }
}
