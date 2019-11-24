package com.crcodings.backgroundservice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public  static final int APP_PERMISSION_SETTINGS = 7;
    public  String  Dialog_Network_Title = "Unable to connect";
    public  String Dialog_Network_Message = "You need internet connection for this app. " +
            "Please turn on mobile network " +
            "or Wi-Fi in Settings.";
    public String Dialog_OK_Button = "Ok";
    public  String Dialog_Network = "Network";
    public String Dialog_Cancel_Button = "Cancel";

    @Override
    public void onReceive(Context context, Intent intent) {
        try
        {
            if(context != null){
                if(intent != null){

                    //            alertDialogBuilder = new AlertDialog.Builder(context);
                    if (!isConnectingToInternet(context)) {

                        showDialog(context, Dialog_Network_Title,
                                Dialog_Network_Message, Dialog_OK_Button,
                                Dialog_Cancel_Button, Dialog_Network);

                    }

                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
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
                                ((AppCompatActivity) context).startActivityForResult(i, APP_PERMISSION_SETTINGS);

                            }else {

                                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                i.setData(Uri.parse("package:" + context.getPackageName()));
                                ((Activity) context).startActivityForResult(i, APP_PERMISSION_SETTINGS);

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


}
