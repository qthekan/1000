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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.qthekan.qhere.MainActivity;
import com.qthekan.qhere.R;
import com.qthekan.util.qlog;
import com.qthekan.util.qutil;


public class JoystickService extends Service {
    WindowManager mWinMgr;
    View mView;
    Joystick mJoystick;
    WindowManager.LayoutParams mParams;

    TextView mTvTitle;
    Button mBtnStop;
    TextView mTvAccuracy;
    TextView mTvWalkSec;
    LinearLayout mViewJoyContens;
    SeekBar mSbMovePower;
    //private int mMovePower = 5;
    private int mMovePower = 8;

    public static boolean mIsMoveing = false;


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

        mWinMgr = (WindowManager) getSystemService(WINDOW_SERVICE);

        int winType = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            winType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        else
        {
            winType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        mParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                winType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        mParams.gravity = Gravity.LEFT | Gravity.TOP;

        mView = inflate.inflate(R.layout.activity_joystick, null);

        mJoystick = mView.findViewById(R.id.joystick);
        mJoystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
                mIsMoveing = true;
            }

            @Override
            public void onDrag(float x, float y, float offset) {
                //qlog.i("x:" + x + ", y:" + y + ", offset:" + offset);
                setJoystickValue(x, y, offset);
                mIsMoveing = true;
            }

            @Override
            public void onUp() {
                setJoystickValue(0, 0, 0);
                mIsMoveing = false;
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

        mTvTitle = mView.findViewById(R.id.tvJoyTitle);
        mViewJoyContens = mView.findViewById(R.id.viewJoyContents);
        mTvAccuracy = mView.findViewById(R.id.tvJoyAcc);
        mTvWalkSec = mView.findViewById(R.id.tvJoyWalkSec);

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

        mWinMgr.addView(mView, mParams);

        mView.setOnTouchListener(mWindowTouchListener);
    }


    private float prevX;
    private float prevY;
    private View.OnTouchListener mWindowTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    prevX = event.getRawX();
                    prevY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 이동한 위치에서 처음 위치를 빼서 이동한 거리를 구한다.
                    float x = event.getRawX() - prevX;
                    float y = event.getRawY() - prevY;

                    mParams.x += x;
                    mParams.y += y;

                    prevX = event.getRawX();
                    prevY = event.getRawY();

                    mWinMgr.updateViewLayout(mView, mParams);
                    break;
                case MotionEvent.ACTION_BUTTON_PRESS:
                    onHide(view);
                    break;
            }
            return true;
        }
    };


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

        if(mWinMgr != null) {
            if(mView != null) {
                mWinMgr.removeView(mView);
                mView = null;
            }
            mWinMgr = null;
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
                    setInfoStr();

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


    private void setInfoStr()
    {
        MainActivity.getIns().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float accuracy = qutil.getFloat( MainActivity.getIns().mAccuracy, 0);
                int color = 0x64FF1212;
                if( MainActivity.getIns().mIsMockLoc || accuracy > 1000 )
                {
                    accuracy = 1000;
                    color = 0x64FF1212;
                }
                else if( accuracy > 500)
                {
                    color = 0x64FFCC14;
                }
                else if( accuracy > 0 )
                {
                    color = 0x644AFF0E;
                }
                mTvAccuracy.setBackgroundColor(color);
                mTvTitle.setBackgroundColor(color);

                float accpercent = (1000 - accuracy) / 10;
                if(accpercent < 0)
                {
                    accpercent = 0;
                }
                String strAcc = qutil.floatToStr(accpercent, "0");
                mTvAccuracy.setText( "Acc: " + strAcc + " %");
//                qlog.e("accuracy: " + accuracy + ", accpercent: " + accpercent + ", strAcc: " + strAcc);

                if( MainActivity.getIns().mWalkThread != null ) {
                    mTvWalkSec.setText("Walk: " + qutil.intToStr(MainActivity.getIns().mWalkThread.mSec, "0"));
                }
                else
                {
                    mTvWalkSec.setText("Walk: ");
                }
            }
        });
    }


    boolean mHide = false;
    public void onHide(View v)
    {
        if(mHide)
        {
            mViewJoyContens.setVisibility(View.VISIBLE);
            mHide = false;
        }
        else
        {
            mViewJoyContens.setVisibility(View.GONE);
            mHide = true;
        }

    }



    private float mX = 0;
    private float mY = 0;
    private float mOffset = 0;
    private final double CONST = 0.0000001;

    private void moveMockLocation()
    {
        //qlog.i("x:" + mX + ", y:" + mY + ", offset:" + mOffset);
        //Log.i("moveMockLocation()", "x:" + mX + ", y:" + mY + ", offset:" + mOffset);
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
