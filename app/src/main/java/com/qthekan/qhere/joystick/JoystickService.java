package com.qthekan.qhere.joystick;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;

import com.google.android.gms.maps.model.LatLng;
import com.qthekan.qhere.MainActivity;
import com.qthekan.qhere.R;
import com.qthekan.util.qlog;


public class JoystickService extends Service {
    WindowManager wm;
    View mView;
    Joystick mJoystick;

    Button mBtnStop;
    SeekBar mSbMovePower;
    //private int mMovePower = 5;
    private int mMovePower = 6;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        int winType = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            winType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        else
        {
            winType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                winType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        //|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        mView = inflate.inflate(R.layout.activity_joystick, null);

        mJoystick = mView.findViewById(R.id.joystick);
        mJoystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {

            }

            @Override
            public void onDrag(float x, float y, float offset) {
                //qlog.i("x:" + x + ", y:" + y + ", offset:" + offset);
                setJoystickValue(x, y, offset);
            }

            @Override
            public void onUp() {
                setJoystickValue(0, 0, 0);
            }
        });

        //===========================================================
        // stop button
        //===========================================================
        mBtnStop = mView.findViewById(R.id.btnStop);
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qlog.i("move stop click");
                MainActivity.getIns().stopJoystick();
            }
        });

        //===========================================================
        // seek bar move power
        // UI 가 너무 지저분해져서 삭제함
        //===========================================================
//        mSbMovePower = mView.findViewById(R.id.sbMovePower);
//        mSbMovePower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                qlog.i("seekbar: " + i);
//                mMovePower = i;
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });

        wm.addView(mView, params);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        startThread();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopThread();

        super.onDestroy();

        if(wm != null) {
            if(mView != null) {
                wm.removeView(mView);
                mView = null;
            }
            wm = null;
        }
    }



    public Thread mThread = null;
    private boolean mRunning = false;

    public void startThread()
    {
        if(mThread != null)
        {
            return;
        }

        qlog.i("");
        mThread = startJoystickThread();

    }


    public void stopThread()
    {
        qlog.i("");
        mRunning = false;

        if(mThread != null)
        {
            qlog.i("");
            mThread.interrupt();
            mThread = null;
        }
    }


    public Thread startJoystickThread() {
        final Thread t = new Thread() {
            @Override
            public void run() {
                mRunning = true;

                while(mRunning)
                {
                    moveMockLocation();

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        qlog.e(e.toString());
                        break;
                    }
                }

                qlog.i("joysitck thread end");
            }
        };

        t.start();
        return t;
    }





    private float mX = 0;
    private float mY = 0;
    private float mOffset = 0;
    private final double CONST = 0.0000001;

    private void moveMockLocation()
    {
        //qlog.i("x:" + mX + ", y:" + mY + ", offset:" + mOffset);
        Log.i("moveMockLocation()", "x:" + mX + ", y:" + mY + ", offset:" + mOffset);
        double lat = MainActivity.getIns().mNewPosition.latitude;
        double lng = MainActivity.getIns().mNewPosition.longitude;

        double c = CONST * mMovePower;
        //double c = CONST * 5;
        lng += (mX * c); // 가로
        lat += (mY * c); // 세로

        LatLng newPos = new LatLng(lat, lng);
        MainActivity.getIns().mNewPosition = newPos;

        /**
         * GUI 관련된 작업은 main thread 에서 수행해야 되서 다음과 같이 처리함.
         */
        MainActivity.getIns().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.getIns().setMockLoc();
            }
        });

        //setJoystickValue(0, 0, 0);
    }


    public void setJoystickValue(float x, float y, float offset)
    {
        mX = x;
        mY = y;
        mOffset = offset;
    }

}
