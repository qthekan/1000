package com.qthekan.qhere;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;


class MockUpdateGPSThread extends Thread {
    final int TIME_UPDATES_MS = 5000;
    public boolean Running;
    //private double curLat = 34.526456 , curLong = 127.3298;

    Context mContext;
    private LatLng mLatLng;


    public MockUpdateGPSThread(Context c)
    {
        mContext = c;
    }

    @Override
    public void run() {
        Log.i("zzz", "Starting Mock GPSThread");
        Running = true;

        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        addSetProvider(locationManager,"gps");
        addSetProvider(locationManager,"network");
        while (Running) {
            setPLocation(locationManager ,"gps", mLatLng.latitude, mLatLng.longitude);
            setPLocation(locationManager ,"network", mLatLng.latitude, mLatLng.longitude);
            try {
                Thread.sleep(TIME_UPDATES_MS);
            } catch (Exception e) {
                Running = false;
                break;
            }
            //curLat = curLat + 0.01;
        }
        delProvider(locationManager, "gps");
        delProvider(locationManager, "network");
        Log.i("zzz", "Ending Mock GPSThread");
    }


    private void addSetProvider(LocationManager locationManager,String provider) {
        locationManager.addTestProvider(provider, false, false, false, false, false, false, false, 1, 1);
        if( !locationManager.isProviderEnabled(provider) )
            locationManager.setTestProviderEnabled(provider, true);
    }

    private void setPLocation(LocationManager locationManager,String provider, double curLat, double curLong) {
        Location loc = new Location(provider);
        loc.setTime(System.currentTimeMillis());
        loc.setLatitude(curLat);
        loc.setLongitude(curLong);
        loc.setBearing(0);
        loc.setAccuracy(1.0f);
        loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        loc.setSpeed((float) 0);
        locationManager.setTestProviderLocation(provider, loc);
    }

    private void delProvider(LocationManager locationManager, String provider) {
        if( locationManager.isProviderEnabled(provider) )
            locationManager.setTestProviderEnabled(provider, false);
        locationManager.removeTestProvider(provider);
    }

    public void setLocation(LatLng loc)
    {
        mLatLng = loc;
    }
}

