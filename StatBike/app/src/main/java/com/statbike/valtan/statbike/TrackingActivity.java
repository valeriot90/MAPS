package com.statbike.valtan.statbike;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class TrackingActivity extends Activity{

    private static final int PERMISSION_REQUEST_CODE = 200;

    Button button1;
    Button button2;

    /*
    public TrackingActivity(Button button1, Button button2) {
        this.button1 = button1;
        this.button2 = button2;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking);

        button1 = findViewById(R.id.start);
        button2 = findViewById(R.id.stop);

        requestPermission();
        gpson();

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                1);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startService(new Intent(TrackingActivity.this , GestioneService.class));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startService(new Intent(TrackingActivity.this , GestioneService.class));
                    }
                }, 3000); // start the service with 3 sec of delay
                showNotification();
                button1.setEnabled(false);
                button2.setEnabled(true);

                button1.setTextColor(Color.parseColor("#000000"));
                button2.setTextColor(Color.parseColor("#ff0000"));
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(TrackingActivity.this , GestioneService.class));
                removeNotification();
                button1.setEnabled(true);
                button2.setEnabled(false);
                button1.setTextColor(Color.parseColor("#ff0000"));
                button2.setTextColor(Color.parseColor("#000000"));
            }
        });

        //button2.setEnabled(false);
        //SavePreferences();
        LoadPreferences();
    }

    public void gpson(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("GPS not enabled");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            AlertDialog alert = dialog.create();
            alert.show();

        }
    }

    public void showNotification() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("StatBike")
                .setContentText("Tracking is running!")
                .setSmallIcon(R.drawable.favicon)
                .setOngoing(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public void removeNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(0);
        //manager.cancelAll();  REMOVE ALL NOTIFICATION OF THIS APP
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted);
                   //     Toast.makeText(this, "Permission Granted, Now you can access location data", Toast.LENGTH_SHORT).show();
                    else {
                    //    Toast.makeText(this, "Permission Denied, You cannot access location data and camera.", Toast.LENGTH_SHORT).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        SavePreferences();
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void SavePreferences(){
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putBoolean("state1", button1.isEnabled());
        editor.putBoolean("state2", button2.isEnabled());
        editor.apply();   // I missed to save the data to preference here,.
    }

    private void LoadPreferences(){
        SharedPreferences prefs = getSharedPreferences("data", MODE_PRIVATE);
        boolean state1 = prefs.getBoolean("state1", true);
        boolean state2 = prefs.getBoolean("state2", false);
        button1.setEnabled(state1);
        if(state1)
            button1.setTextColor(Color.parseColor("#ff0000"));
        else
            button1.setTextColor(Color.parseColor("#000000"));
        button2.setEnabled(state2);
        if(state2)
            button2.setTextColor(Color.parseColor("#ff0000"));
        else
            button2.setTextColor(Color.parseColor("#000000"));
    }

    @Override
    public void onBackPressed() {
        SavePreferences();
        super.onBackPressed();
    }

}
