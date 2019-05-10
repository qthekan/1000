package com.qthekan.qhere;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.provider.Settings;

import com.google.android.gms.maps.model.LatLng;
import com.qthekan.qhere.joystick.JoystickService;
import com.qthekan.util.qlog;
import com.qthekan.util.qutil;


class MockUpdateGPSThread extends Thread
{
    final int TIME_UPDATES_MS = 2000;
    public boolean Running;

    Context mContext;
    private LatLng mLatLng;
    private double mCurrLat = 0;
    private double mCurrLng = 0;
    private float mCurrAcc = 0;
    // 포고에서는 1000 보다 커지면 '신호를 찾을수 없음' 에러 발생.
    private final float mACCURACY_MAX = 1000;
    // 목표좌표와 현재좌표의 오차 허용범위
    private final double mErrorRange = 0.0001;

    private LocationManager mLocMgr;

    public Location mCurrentLocation;


    @SuppressLint("MissingPermission")
    public MockUpdateGPSThread(Context c)
    {
        mContext = c;

        mLocMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }


    public void stopThread()
    {
        Running = false;
        interrupt();
    }


    boolean mbMoveMockLoc = false;
    @Override
    public void run() {
        qlog.i("Starting Mock GPSThread");
        Running = true;

        while (Running)
        {
            MainActivity.getIns().getCurrentLocation();

            try
            {
                Thread.sleep(TIME_UPDATES_MS);
            }
            catch (Exception e)
            {
                Running = false;
                break;
            }

            try {
                if (JoystickService.mIsMoveing) {
                    moveToMockLocation();
                    continue;
                }

                if (Math.abs(mCurrLng - mLatLng.longitude) > mErrorRange
                        || Math.abs(mCurrLat - mLatLng.latitude) > mErrorRange
                        || mCurrAcc > mACCURACY_MAX) {
                    qlog.e("current: " + mCurrLat + "," + mCurrLng + " destination: " + mLatLng.latitude + "," + mLatLng.longitude + " accuracy: " + mCurrAcc);
                    moveToMockLocation();
                }

                Running = false;
                break;

//                if( mbMoveMockLoc || mCurrAcc > mACCURACY_MAX )
//                {
//                    qlog.e("111");
//                    moveToMockLocation();
//                    qlog.e("222");
//                    mbMoveMockLoc = false;
//                }
            }
            catch(Exception e)
            {
                qlog.e("MockUpdateGPSThread", e );
            }

        }

        qlog.e("Mock GPSThread end");
    }


    private void moveToMockLocation()
    {
        qlog.e("new lat: " + mLatLng.latitude + ", lng: " + mLatLng.longitude);
        int value = setMockLocationSettings();
        addSetProvider(mLocMgr, "gps");
        addSetProvider(mLocMgr,"network");

        setPLocation(mLocMgr, "gps", mLatLng.latitude, mLatLng.longitude);
        setPLocation(mLocMgr, "network", mLatLng.latitude, mLatLng.longitude);

        delProvider(mLocMgr, "gps");
        delProvider(mLocMgr, "network");
        restoreMockLocationSettings(value);
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

    public void setLocation(LatLng loc)
    {
        mLatLng = new LatLng(qutil.getDouble(loc.latitude, 4), qutil.getDouble(loc.longitude, 4));
        mbMoveMockLoc = true;
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

