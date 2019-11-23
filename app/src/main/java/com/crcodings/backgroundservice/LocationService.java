package com.crcodings.backgroundservice;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service {
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 0; // Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 500; // in Milliseconds
    protected LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private final String TAG = "LocationService";

    public static final int notify = 120000;  //interval between two services(Here Service run every 5 Minute)
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling
    DatabaseHandler dbHandler;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
                Log.d(TAG, "GPS Service created ...");
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        Log.d("LocationService", " " + isGPSEnabled + " " + isNetworkEnabled);

        if (mTimer != null)
            mTimer.cancel();
        else
            mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);

    }

    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // display toast
                    getLatLang();

                }
            });
        }
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    // return TODO;
                }
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private void getLatLang() {
        dbHandler = new DatabaseHandler(this);
        if (ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location Permission is declined");
        } else {

            if (isNetworkEnabled && isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MINIMUM_TIME_BETWEEN_UPDATES,
                        MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, new MyLocationListener());

                Location location = getLastKnownLocation();
                LocationDetailAPI(location);
                Log.d(TAG, "onCreate: " + location);
                Log.d(TAG, "onCreate:lat " + location.getLatitude());
                Log.d(TAG, "onCreate:long " + location.getLongitude());
            }
        }

    }

    private void LocationDetailAPI(Location location) {

        String device_name = android.os.Build.MODEL;

        Log.d(TAG, "LocationDetailAPI:device_name " + device_name);
        Log.d(TAG, "LocationDetailAPI:lat " + location.getLatitude());
        Log.d(TAG, "LocationDetailAPI:long " + location.getLongitude());
        String lat = String.valueOf(location.getLatitude());
        String lang = String.valueOf(location.getLongitude());
        LocationModel locationModel = new LocationModel();
        locationModel.setLatitude(lat);
        locationModel.setLongitude(lang);
        dbHandler.insertLatLang(locationModel);
        if(isNetworkEnabled){
            new SaveLatLang(device_name,lat,lang).execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "GPS Service destroyed ...");
        mTimer.cancel();    //For Cancel Timer
        stopSelf();
        Toast.makeText(this, "Service is Destroyed", Toast.LENGTH_SHORT).show();
    }


    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            String message = String.format("New Location \n Longitude: %1$s \n Latitude: %2$s",
                    location.getLongitude(), location.getLatitude());
            String device_name = android.os.Build.MODEL;
//            Toast.makeText(LocationService.this, device_name+" (lat: "+location.getLatitude()+" long: "+location.getLongitude()+")", Toast.LENGTH_SHORT).show();

            Log.d(TAG, device_name);
            Log.d(TAG, location.getLatitude() + " " + location.getLongitude());
            Log.d(TAG, message);
            LocationDetailAPI(location);

        }

        public void onStatusChanged(String s, int i, Bundle b) {
        }

        public void onProviderDisabled(String s) {
        }

        public void onProviderEnabled(String s) {
        }
    }



    @SuppressLint("StaticFieldLeak")
    private class SaveLatLang extends AsyncTask<String,String,String> {
        String message = "", user_id, address_id, access_token;
        String Product_Exception = "false";
        String resServer = "";
        String latitude = "";
        String longitude = "";
        String device_name = "";
        boolean status = false;

        SaveLatLang(String device_name, String latitude, String longitude) {

            this.device_name = device_name;
            this.latitude = latitude;
            this.longitude = longitude;

        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            try{

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("activeFlag", "false")
                        .appendQueryParameter("id", address_id);
                String formData = builder.build().getEncodedQuery();

                URL url = new URL("https://portal.spassh.com/WebApi/Api/Home/StoreValues?deviceId=sriniTamilDevice&lat="+latitude+
                        "&longt="+longitude);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("PUT");
                conn.setRequestProperty ("Authorization", access_token);
                conn.setFixedLengthStreamingMode(formData.getBytes().length);

                OutputStream os = conn.getOutputStream();
                PrintWriter out = new PrintWriter(os);
                out.print(formData);
                out.flush();
                out.close();
                os.close();

                conn.connect();

                int responseCode = conn.getResponseCode();

                BufferedReader in;
                if (responseCode == HttpURLConnection.HTTP_OK
                        || responseCode == HttpURLConnection.HTTP_CREATED) {

                    in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    Product_Exception = "true";

                } else {

                    in = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream()));

                    Product_Exception = "false";
                }

                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                resServer = response.toString();


            }catch (Exception e){
                e.printStackTrace();
            }

            return resServer;
        }

        @Override
        protected void onPostExecute(String d) {
            super.onPostExecute(d);

            if(resServer != null){
//
//                Toast.makeText(LocationService.this, device_name+" (lat: "+latitude+" long: "+longitude+")", Toast.LENGTH_SHORT).show();
            }else{
//                Toast.makeText(LocationService.this,"Please Try Again! Unable to Delete",Toast.LENGTH_SHORT).show();
            }
        }
    }


}