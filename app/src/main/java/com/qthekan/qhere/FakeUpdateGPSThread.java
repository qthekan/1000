package com.qthekan.qhere;


import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

class FakeUpdateGPSThread extends Thread {
    final int TIME_UPDATES_MS = 15 * 1000;
    public boolean Running;

    Context mContext;
    private LatLng mLatLng;


    public FakeUpdateGPSThread(Context c)
    {
        mContext = c;
    }

    @Override
    public void run() {
        Log.i("zzz", "Starting fake GPSThread");
        Running = true;

        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        addSetProvider(locationManager,"network");
        while (Running) {
            setFakeLocation(locationManager ,"gps","network", mLatLng.latitude, mLatLng.longitude);
            try {
                Thread.sleep(TIME_UPDATES_MS);
            } catch (Exception e) {
                Running = false;
                break;
            }
        }
        delProvider(locationManager, "network");
        Log.i("zzz", "Ending fake GPSThread");
    }

    private void setFakeLocation(LocationManager locationManager,String provider1, String provider2, double curLat, double curLong) {
        Location loc = new Location("");
        loc.setProvider(provider1);
        loc.setTime(System.currentTimeMillis());
        loc.setLatitude(curLat);
        loc.setLongitude(curLong);
        loc.setBearing(0);
        loc.setAccuracy(1.0f);
        loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        loc.setSpeed((float) 0);
        locationManager.setTestProviderLocation(provider2, loc);
    }

    private void addSetProvider(LocationManager locationManager,String provider) {
        locationManager.addTestProvider(provider, false, false, false, false, false, false, false, 1, 1);
        if( !locationManager.isProviderEnabled(provider) )
            locationManager.setTestProviderEnabled(provider, true);
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