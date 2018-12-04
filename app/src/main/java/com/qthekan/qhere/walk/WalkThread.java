package com.qthekan.qhere.walk;

import com.qthekan.qhere.MainActivity;
import com.qthekan.util.qlog;
import com.qthekan.util.qutil;

public class WalkThread extends Thread
{
    private boolean mFlag = false;
    public int mSec = 0;


    public void run() {
        mFlag = true;

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
        qlog.e("WalkThread end");
    }

}
