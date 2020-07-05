package com.statbike.valtan.statbike;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GestioneService extends Service
{
    private static final String TAG = "GPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 100;
    private static final float LOCATION_DISTANCE = 1;

    private double lat, lon, lat_old, lon_old;
    String time, time_old;

    private boolean firstexe = true;

    private String data = null;

    private static final String LOG_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "StatApp" + File.separator;

    private File file = null;

    private class LocationListener implements android.location.LocationListener{
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        /*
        @parameter Location location
        called every time the location change and write the position
        and the velocity
        */
        @Override
        public void onLocationChanged(Location location) {
            DateFormat df = new SimpleDateFormat("mm:ss");
            String speed;

            Log.d(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            if(firstexe){
                lat = mLastLocation.getLatitude();
                lon = mLastLocation.getLongitude();
                lat_old = lat;
                lon_old = lon;
                time = df.format(Calendar.getInstance().getTime());
                time_old = time;
                firstexe = false;
            }
            else{
                lat_old = lat;
                lon_old = lon;
                time_old = time;
                time = df.format(Calendar.getInstance().getTime());
                lat = mLastLocation.getLatitude();
                lon = mLastLocation.getLongitude();
            }
            double tm,tmo,ts,tso;
            String[] tt = time.split(":");
            tm = Double.parseDouble(tt[0]);
            ts = Double.parseDouble(tt[1]);
            tt = time_old.split(":");
            tmo = Double.parseDouble(tt[0]);
            tso = Double.parseDouble(tt[1]);

            speed = velocity(lat, lon, lat_old, lon_old, tm, tmo, ts, tso);

            data = lat + "," + lon + "," + time + "," + speed;
            Log.d(TAG, data);

            try {
                write(data);
            } catch (IOException e) {
                Log.d("Eccezione: ", "Eccezionale! ho provato a scrivere un dato");
            }
        }

        /*
        @param double lat   latitute
        @param double lon   longitude
        @param double lat_old   previous latitude
        @param double lon_old   previous longitude
        @param double m minutes of the sampling time
        @param double m_old previous minutes of the sampling time
        @param double s seconds of the sampling time
        @param double s_old previous seconds of teh sampling time
        return a string of velocity
        */
        public String velocity(double lat, double lon, double lat_old, double lon_old, double m, double m_old, double s, double s_old){
            double space = Math.sqrt(Math.pow((lat-lat_old), 2) + Math.pow(lon-lon_old,2));
            double tt = (int)s-(int)s_old + 60*(m-m_old);
            double speed = (space/tt)*3.6*Math.pow(10,5); // TRANSFORM THE SPEED TO Km/h
            Log.d(TAG, "Velocity: " + speed);
            if (String.valueOf(speed).equals("Infinity"))
                    return String.valueOf(0); // "0"
            else
                return String.valueOf(speed);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }
        @Override
        public void onProviderEnabled(String provider)
        {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.d(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    /*
    * started at the creation of the service:
    * start the  tracking
    * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand");
        Toast.makeText(this, "Start Tracking", Toast.LENGTH_SHORT).show();
        super.onStartCommand(intent, flags, startId);

        DateFormat df = new SimpleDateFormat("ddMMyyyy_H:mm");
        String date = df.format(Calendar.getInstance().getTime());
        String path = LOG_PATH + date + ".txt";
        file = new File(LOG_PATH, date + ".txt");
        if(!file.exists()) {
            try {
                Log.d("SCRIVO IL FILE", path);
                file.createNewFile();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }
/*
    public void close() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(fOut);
        Log.d(TAG, "CHIUDO STREAM ");
        osw.flush();
        try {
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*
    * @param String data
    * given the string, create a buffer,
    * write the string on a file
    * and close the buffer
    * */
    public void write(String data) throws IOException {
        Log.d(TAG, "SCRIVO STREAM "+ data);

        FileWriter fw = new FileWriter(file.getAbsolutePath(), true);
        BufferedWriter bw = new BufferedWriter(fw);

        bw.write(data);
        bw.newLine();
        bw.close();
    }

    public void close() throws IOException {
        //printer.close();
        Log.d(TAG, "Log file closed");
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "onDestroy");
        Toast.makeText(this, "Stop Tracking", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }
    private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
