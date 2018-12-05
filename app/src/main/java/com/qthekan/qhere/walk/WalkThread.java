package com.qthekan.qhere.walk;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.qthekan.qhere.MainActivity;
import com.qthekan.util.qlog;
import com.qthekan.util.qutil;

import java.util.ArrayList;


public class WalkThread extends Thread
{
    private boolean mFlag = false;
    public int mSec = 0;
    ArrayList<Marker> mListMarker = new ArrayList<>();


    public void run() {
        mFlag = true;
        printMarker();

        while (mFlag) {
            for (String position : MainActivity.getIns().mListPosition) {
                qlog.e("position: " + position);
                MainActivity.getIns().mNewPosition = qutil.stringToLatlng(position);
                MainActivity.getIns().mIsMockLoc = true;

                MainActivity.getIns().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (MainActivity.getIns().mMockRunning) {
                            MainActivity.getIns().setMockLoc();
                        }
                        else {
                            //MainActivity.getIns().startMock();
                            MainActivity.getIns().startJoystick();
                        }
                    }
                });

                try {
                    // main 에서 가상위치인지 체크하는것을 기다림
                    sleep(3000);

                    while( MainActivity.getIns().mIsMockLoc )
                    {
                        sleep(1000);
                    }

                    mSec = MainActivity.getIns().mInterval;
                    while(mSec-- > 0)
                    {
                        sleep(1000);
                    }
                }
                catch (InterruptedException e) {
                    mFlag = false;
                    break;
                }
            }
        }
        removeMarker();
        qlog.e("WalkThread end");
    }


    private void removeMarker()
    {
        MainActivity.getIns().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(Marker m : mListMarker)
                {
                    m.remove();
                }
            }
        });
    }


    private void printMarker()
    {
        MainActivity.getIns().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                for (String position : MainActivity.getIns().mListPosition) {
                    i++;
                    LatLng latlng = qutil.stringToLatlng(position);
                    String title = "walk-" + (i);

                    MarkerOptions mo = new MarkerOptions();
                    mo.position(latlng).title(title).alpha(0.4f);
                    Marker marker = MainActivity.getIns().mMap.addMarker(mo);
                    marker.showInfoWindow();
                    mListMarker.add(marker);
                }
            }
        });
    }

}
