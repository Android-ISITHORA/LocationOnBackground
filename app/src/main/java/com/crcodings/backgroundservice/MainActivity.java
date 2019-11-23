package com.crcodings.backgroundservice;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private static final int MY_IGNORE_OPTIMIZATION_REQUEST = 1 ;
    boolean isPermissionAllowed = false;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    protected LocationManager locationManager;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Button tv_start = findViewById(R.id.tv_start);
        final Button tv_stop = findViewById(R.id.tv_stop);


        sharedPreferences = getSharedPreferences("ServiceDetails",MODE_PRIVATE);

        if(sharedPreferences.getBoolean("isServiceStarted",false)) {
            sharedPreferences.edit().putBoolean("isServiceStarted", true).apply();
            tv_start.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg));
            tv_stop.setBackground(ContextCompat.getDrawable(this, R.drawable.button));
            tv_stop.setEnabled(true);
        }else {
            sharedPreferences.edit().putBoolean("isServiceStarted", false).apply();
            tv_start.setBackground(ContextCompat.getDrawable(this, R.drawable.button));
            tv_stop.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg));
            tv_stop.setEnabled(false);
        }

        isPermissionAllowed = checkLocationPermission(MainActivity.this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        Log.d("LocationService", " " + isGPSEnabled + " " + isNetworkEnabled);

        tv_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkEnabled) {
                    if(isGPSEnabled) {
                        if (isPermissionAllowed) {
                            Intent intent = new Intent(getApplicationContext(), LocationService.class);
                            startService(intent);
                            sharedPreferences.edit().putBoolean("isServiceStarted", true).apply();
                            tv_start.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_bg));
                            tv_stop.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button));
                            tv_stop.setEnabled(true);
                        } else {
                            isPermissionAllowed = checkLocationPermission(MainActivity.this);
                        }
                    }else {
                        Toast.makeText(getApplicationContext(), "Please enable gps", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        tv_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocationService.class);
                stopService(intent);

                sharedPreferences.edit().putBoolean("isServiceStarted", false).apply();
                tv_start.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button));
                tv_stop.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_bg));
                tv_stop.setEnabled(false);
            }
        });

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        boolean isIgnoringBatteryOptimizations = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (pm != null) {
                isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());
            }
            if(!isIgnoringBatteryOptimizations){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, MY_IGNORE_OPTIMIZATION_REQUEST);
            }else {
                Toast.makeText(getApplicationContext(), "power allowed", Toast.LENGTH_LONG).show();
            }
        }

    }

    public static boolean checkLocationPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= 23) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                        Manifest.permission.ACCESS_FINE_LOCATION) &&
                        ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Location Permission necessary");
                    alertBuilder.setMessage("Access Location permission is necessary!");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            if (context instanceof AppCompatActivity) {

                                ActivityCompat.requestPermissions((AppCompatActivity) context,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);

                            } else {

                                ActivityCompat.requestPermissions((Activity) context,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);

                            }

                        }
                    });

                    AlertDialog alert = alertBuilder.create();

                    if (context instanceof AppCompatActivity) {

                        if (!((AppCompatActivity) context).isFinishing()) {

                            if (alert != null && !alert.isShowing()) {

                                alert.show();

                            }

                        }

                    } else {

                        if (!((Activity) context).isFinishing()) {

                            if (alert != null && !alert.isShowing()) {

                                alert.show();


                            }
                        }
                    }

                } else {

                    if (context instanceof AppCompatActivity) {

                        ActivityCompat.requestPermissions((AppCompatActivity) context,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);

                    } else {

                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);

                    }
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_IGNORE_OPTIMIZATION_REQUEST) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isIgnoringBatteryOptimizations = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (pm != null) {
                    isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());
                }
                if (isIgnoringBatteryOptimizations) {
                    Toast.makeText(getApplicationContext(), "power allowed", Toast.LENGTH_LONG).show();
                } else {
                    // Not ignoring battery optimization
                }
            }

        }
    }


}
