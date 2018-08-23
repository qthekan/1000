package com.qthekan.qhere;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.qthekan.qhere.joystick.JoystickService;
import com.qthekan.util.qlog;
import com.qthekan.util.qutil;


class MockUpdateGPSThread extends Thread {
    final int TIME_UPDATES_MS = 1000;
    public boolean Running;

    Context mContext;
    private LatLng mLatLng;
    private double mCurrLat = 0;
    private double mCurrLng = 0;
    private float mCurrAcc = 0;
    private final float mACCURACY_MAX = 1100;

    private LocationManager mLocMgr;

    public Location mCurrentLocation;


    @SuppressLint("MissingPermission")
    public MockUpdateGPSThread(Context c)
    {
        mContext = c;

        mLocMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        addSetProvider(mLocMgr, "gps");
        //addSetProvider(mLocMgr,"network");
    }


    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        qlog.i("Starting Mock GPSThread");
        Running = true;

        while (Running)
        {
            if( JoystickService.mIsMoveing )
            {
                moveToMockLocation();

                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    Running = false;
                    break;
                }

                continue;
            }

            MainActivity.getIns().getCurrentLocation();

            if( mCurrLng != mLatLng.longitude || mCurrLat != mLatLng.latitude || mCurrAcc > mACCURACY_MAX )
            {
                moveToMockLocation();
            }

            try
            {
                Thread.sleep(TIME_UPDATES_MS);
            }
            catch (Exception e)
            {
                Running = false;
                break;
            }
        }

        delProvider(mLocMgr, "gps");
        //delProvider(mLocMgr, "network");
        qlog.e("Mock GPSThread end");
    }


    private void moveToMockLocation()
    {
        qlog.e("new lat: " + mLatLng.latitude + ", lng: " + mLatLng.longitude);
        setPLocation(mLocMgr, "gps", mLatLng.latitude, mLatLng.longitude);
        //setPLocation(mLocMgr, "network", mLatLng.latitude, mLatLng.longitude);
    }


    private void setPLocation(LocationManager locationManager, String provider, double curLat, double curLong)
    {
        Location loc = new Location(provider);
        loc.setTime(System.currentTimeMillis());
        loc.setLatitude(curLat);
        loc.setLongitude(curLong);
        loc.setBearing(0);
        loc.setAccuracy(0);   // 클 수록 맵에서 반경이 커짐.
        loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        loc.setSpeed((float) 0);
        locationManager.setTestProviderLocation(provider, loc);
    }


    private void addSetProvider(LocationManager locationManager, String provider) {
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
        mLatLng = new LatLng(qutil.getDouble(loc.latitude, 4), qutil.getDouble(loc.longitude, 4));
    }


    public void setCurrentLocation(Location loc)
    {
        mCurrentLocation = loc;
        mCurrLat = qutil.getDouble( loc.getLatitude(), 4 );
        mCurrLng = qutil.getDouble( loc.getLongitude(), 4 );
        mCurrAcc = loc.getAccuracy();
        qlog.e("provider: " + loc.getProvider() + ", lat: " + mCurrLat + ", lng: " + mCurrLng + ", acc: " + mCurrAcc + ", ismock: " + loc.isFromMockProvider() );
    }

}

