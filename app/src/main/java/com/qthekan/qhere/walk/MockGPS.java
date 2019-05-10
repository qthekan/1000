package com.qthekan.qhere.walk;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.provider.Settings;

import com.google.android.gms.maps.model.LatLng;
import com.qthekan.qhere.MainActivity;
import com.qthekan.util.qlog;
import com.qthekan.util.qutil;


public class MockGPS
{
    private LocationManager mLocMgr;

    // 목표좌표와 현재좌표의 오차 허용범위
    static private final double mErrorRange = 0.0001;


    public MockGPS(Context c)
    {
        mLocMgr = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
    }


    static public boolean isSameLoc(LatLng a, Location b)
    {
        if( Math.abs(a.latitude - b.getLatitude()) > mErrorRange
                || Math.abs(a.longitude - b.getLongitude()) > mErrorRange )
        {
            return false;
        }

        return true;
    }


    public void moveToMockLocation(LatLng latlng)
    {
        qlog.e("START!!!!!!!!!");
        qlog.e("new lat: " + latlng.latitude + ", lng: " + latlng.longitude);
        int value = setMockLocationSettings();
        addSetProvider(mLocMgr, "gps");
        addSetProvider(mLocMgr,"network");

        setPLocation(mLocMgr, "gps", latlng.latitude, latlng.longitude);
        setPLocation(mLocMgr, "network", latlng.latitude, latlng.longitude);

        delProvider(mLocMgr, "gps");
        delProvider(mLocMgr, "network");
        restoreMockLocationSettings(value);
        qlog.e("END!!!!!!!!!");
    }


    private void setPLocation(LocationManager locationManager, String provider, double curLat, double curLong)
    {
        Location loc = new Location(provider);
        loc.setTime(System.currentTimeMillis());
        loc.setLatitude(curLat);
        loc.setLongitude(curLong);
        loc.setBearing(0);
        loc.setAccuracy(10);   // 클 수록 맵에서 반경이 커짐.
        loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        loc.setSpeed((float) 100);

        //int value = setMockLocationSettings();
        locationManager.setTestProviderLocation(provider, loc);
        //restoreMockLocationSettings(value);
    }


    private int setMockLocationSettings() {
        int value = 1;
        try {
            value = Settings.Secure.getInt(MainActivity.getIns().getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION);
            Settings.Secure.putInt(MainActivity.getIns().getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION, 1);
        } catch (Exception e) {
            //qlog.e("", e);
        }
        return value;
    }


    private void restoreMockLocationSettings(int restore_value) {
        try {
            Settings.Secure.putInt(MainActivity.getIns().getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION, restore_value);
        } catch (Exception e) {
            //qlog.e("", e);
        }
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

}
