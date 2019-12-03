package com.crcodings.backgroundservice;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
    private BroadcastReceiver mNetworkReceiver;


    public String Dialog_Location_Title = "GPS Required";
    public String Dialog_Location_Message = "Your GPS seems to be disabled, " +
            "do you want to enable it?";
    public String Dialog_Yes_Button = "Yes";
    public String Dialog_No_Button = "No";
    public String Dialog_Location = "Location";

    DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Button tv_start = findViewById(R.id.tv_start);
        final Button tv_stop = findViewById(R.id.tv_stop);

        databaseHandler = new DatabaseHandler(this);

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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        isPermissionAllowed = checkLocationPermission(MainActivity.this);
        isNetworkEnabled = isConnectingToInternet(this);

    //    isGPSEnabled = isLocationEnabled(MainActivity.this);


        Log.d("LocationService", " " + isGPSEnabled + " " + isNetworkEnabled);

        tv_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isNetworkEnabled = isConnectingToInternet(MainActivity.this);
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

                int count = databaseHandler.getLocationData().size();

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
//                Toast.makeText(getApplicationContext(), "power allowed", Toast.LENGTH_LONG).show();
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
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);

                            } else {

                                ActivityCompat.requestPermissions((Activity) context,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);

                    } else {

                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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
    public void onResume(){
        super.onResume();

        //Network Connectivity Change Receiver
        mNetworkReceiver = new NetworkChangeReceiver();

//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//        }
        isPermissionAllowed = checkLocationPermission(MainActivity.this);
        isNetworkEnabled = isConnectingToInternet(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        if (isGPSEnabled) {

        } else {
            showLocationEnable(MainActivity.this, Dialog_Location_Title,
                    Dialog_Location_Message, Dialog_Yes_Button,
                    Dialog_No_Button);
        }

        Log.d("LocationService", " " + isGPSEnabled + " " + isNetworkEnabled);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNetworkReceiver != null)
            unregisterNetworkChanges();
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
            mNetworkReceiver = null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
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


        if (requestCode == MY_IGNORE_OPTIMIZATION_REQUEST) {
            isGPSEnabled = isLocationEnabled(MainActivity.this);
            if(!isGPSEnabled){
                isGPSEnabled = false;
                showLocationEnable(MainActivity.this, Dialog_Location_Title,
                        Dialog_Location_Message, Dialog_Yes_Button,
                        Dialog_No_Button);
            }else {
                isGPSEnabled = true;
            }
        }



        }

    public static boolean isConnectingToInternet(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {

            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        Log.d("Network", "NETWORK_NAME: " + anInfo.getTypeName());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isLocationEnabled(Context context) {

        LocationManager lm;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            if (lm != null) {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }
        } catch (Exception ex) {
            Log.d("canGetLocation", "Exception : " + ex.getMessage());
        }

        return gps_enabled || network_enabled;
    }

    public static void showDialog(final Context context,
                                  String title,
                                  String message,
                                  String positiveButton,
                                  String negativeButton,
                                  final String function) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent i = new Intent();

                            if (context instanceof AppCompatActivity) {

                                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                i.setData(Uri.parse("package:" + context.getPackageName()));
                                ((AppCompatActivity) context).startActivityForResult(i, 1);

                            }else {

                                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                i.setData(Uri.parse("package:" + context.getPackageName()));
                                ((Activity) context).startActivityForResult(i, 1);

                            }

                    }
                })
                .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = alertDialogBuilder.create();

        if (context instanceof AppCompatActivity) {

            if (!((AppCompatActivity) context).isFinishing()) {

                if (alert != null && !alert.isShowing()) {

                    alert.show();

//                    customiseAlert(alert, context);
                }

            }

        } else {

            if (!((Activity) context).isFinishing()) {

                if (alert != null && !alert.isShowing()) {

                    alert.show();

//                    customiseAlert(alert, context);
                }
            }
        }


    }


    private void showLocationEnable(final Context context,
                                    String title,
                                    String message,
                                    String positiveButton,
                                    String negativeButton){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent i = new Intent();
                        if (context instanceof AppCompatActivity) {

                            i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            ((AppCompatActivity) context).startActivityForResult(i, MY_PERMISSIONS_REQUEST_LOCATION);

                        }else {

                            i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            ((Activity) context).startActivityForResult(i, MY_PERMISSIONS_REQUEST_LOCATION);

                        }

                    }
                })
                .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                /* Create an Intent that will start the Menu-Activity. */
                                dialog.dismiss();
//                }

                            }
                        }, 1000);

                    }
                });

        AlertDialog alert = alertDialogBuilder.create();

        if (!MainActivity.this.isFinishing()) {
            if (!alert.isShowing()) {
                alert.show();

            }
        }

        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
//                moveToNextActivity();
                dialog.dismiss();

            }
        });

    }


}
