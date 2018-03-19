package com.qthekan.qhere.talk;

import android.app.Service;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qthekan.qhere.MainActivity;
import com.qthekan.qhere.R;


public class ChatService extends Service
{
    public static ChatService ins;

    WindowManager mWindowMgr;
    WindowManager.LayoutParams mWinMgrParam;
    View mView;

    LinearLayout mLayoutChatWindow;
    Button mBtnStop;
    Button mBtnSend;
    Button mBtnHide;
    Button mBtnShow;
    TextView mTvNickName;
    TextView mTvRoomName;
    TextView mTvContents;
    EditText mEtUserInput;

    String mRoomName;
    String mNickName;

    FirebaseMgr mFireMgr;


    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    @Override
    public void onCreate()
    {
        ins = this;

        super.onCreate();
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.chat_window, null);

        initWindowMgr();

        mLayoutChatWindow = mView.findViewById(R.id.layoutChatWindow);

        mTvNickName = mView.findViewById(R.id.tvNickName);
        mTvRoomName = mView.findViewById(R.id.tvRoomName);
        mTvContents = mView.findViewById(R.id.tvContents);

        mBtnSend = mView.findViewById(R.id.btnSendMsg);
        mBtnStop = mView.findViewById(R.id.btnStopService);
        mBtnHide = mView.findViewById(R.id.btnHideWindow);
        mBtnShow = mView.findViewById(R.id.btnShowWindow);
        mEtUserInput = mView.findViewById(R.id.etUserInput);

        setEventHandler();
    }


    private void initWindowMgr()
    {
        // 안드로이드 버전에 따라 overlay 설정.
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            //LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        mWinMgrParam = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,    // width
                ViewGroup.LayoutParams.WRAP_CONTENT,    // height
                LAYOUT_FLAG,
                //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,  // layout 이외의 영역은 터치가능. 뒤로가기 이벤트도 팝업창이 먹음. edittext 키보드올라옴.
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  // window manager layout 이외의 영역은 터치 가능. 화면 뒤의 어플에서 뒤로가기 이벤트 처리. edit text 에 키보드가 안올라옴.
                //WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,  // 팝업창에 터치가 안통하고, 아래 어플에 터치가 통과됨.
                PixelFormat.TRANSLUCENT);

        // 좌측 상단이 0,0
        mWinMgrParam.gravity = Gravity.TOP | Gravity.LEFT;

        mWindowMgr = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowMgr.addView(mView, mWinMgrParam);
    }


    /**
     * 각 component 들의 event 를 등록한다.
     */
    private void setEventHandler()
    {
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("setEventHandler()", "send message");
                mFireMgr.insert(mRoomName, mNickName, mEtUserInput.getText().toString());
                mEtUserInput.setText("");
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("setEventHandler()", "stop chat service");
                MainActivity.getIns().stopChatService();
            }
        });

        mBtnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("setEventHandler()", "hide chat service");
                hideChatWindow();
            }
        });

        mBtnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("setEventHandler()", "show chat service");
                showChatWindow();
            }
        });

        // 롱 클릭 시 버튼 위치 이동
        mBtnShow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setTouchListener();
                return false;
            }
        });

        /**
         * edit text 에 포커스가 있을 때만 이벤트 처리하도록 설정.
         * 왜냐하면 기본적으로 home, back 이벤트는 백그라운드 앱에 전달하기 위함.
         */
        mEtUserInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mWinMgrParam.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;  // 기본값으로 원복
                mWindowMgr.updateViewLayout(mView, mWinMgrParam);
                return false;
            }
        });

        mEtUserInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                {
                    setEventOnEditText();
                }
                else
                {
                    unsetEventOnEditText();
                }
            }
        });

    }


    /**
     * 롱 클릭 시 터치 리스너를 등록하여 손가락을 따라가면서 winMgr 의 위치를 이동하고
     * 손가락을 떼면 터치 리스너를 해제한다.
     */
    private float prevx, prevy;
    private void setTouchListener()
    {
        mBtnShow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                switch (e.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        //Log.i("user", e.getRawX() + "  " + e.getRawY());
                        //Log.i("button", mBtnShow.getX() + "  " + mBtnShow.getY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //Log.i("user", e.getRawX() + "  " + e.getRawY());
                        //Log.i("button", mBtnShow.getX() + "  " + mBtnShow.getY());

                        mWinMgrParam.x = (int) (e.getRawX() - (mBtnShow.getWidth()/2));
                        mWinMgrParam.y = (int) (e.getRawY() - (mBtnShow.getHeight()));
                        mWindowMgr.updateViewLayout(mView, mWinMgrParam);
                        break;
                    case MotionEvent.ACTION_UP:
                        // clear touch listener when end touch event
                        mBtnShow.setOnTouchListener(null);
                        break;
                }
                return false;
            }
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mRoomName = intent.getStringExtra("RoomName");
        mNickName = intent.getStringExtra("NickName");

        mTvNickName.setText(mNickName);
        mTvRoomName.setText(mRoomName);

        mFireMgr = new FirebaseMgr();
        mFireMgr.connect(mRoomName);
        mFireMgr.select(mRoomName);

        return START_STICKY;
    }


    @Override
    public void onDestroy()
    {
        Log.i("onDestroy()", "");
        super.onDestroy();

        if(mWindowMgr != null)
        {
            if(mView != null)
            {
                mWindowMgr.removeView(mView);
                mView = null;
            }

            mWindowMgr = null;
        }

        // 창닫기 버튼 클릭 시, firebase 연결이 해제되지 않고,
        // rejoin 시 중복 이벤트 수신이 발생하여 프로그램을 종료해버리도록 수정.
        //System.exit(0);
        mFireMgr.unregievent(mRoomName);
    }


    public void appendContents(String msg)
    {
        mTvContents.append(msg);
    }


    /**
     * popup window 를 최소화하여 버튼만 보여주고 이동할 수 있도록 한다.
     */
    public void hideChatWindow()
    {
        mLayoutChatWindow.setVisibility(View.INVISIBLE);
        mBtnShow.setVisibility(View.VISIBLE);

        mWinMgrParam.width = mBtnShow.getWidth();
        mWinMgrParam.height = mBtnShow.getHeight();

        unsetEventOnEditText();
    }


    /**
     * 채팅 내용을 볼 수 있도록 popup window 를 크게한다.
     */
    public void showChatWindow()
    {
        mLayoutChatWindow.setVisibility(View.VISIBLE);
        mBtnShow.setVisibility(View.INVISIBLE);

        mWinMgrParam.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mWinMgrParam.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        setEventOnEditText();
    }


    /**
     * popup window 에 포커스를 주고 터치 이벤트를 처리하도록 설정.
     */
    private void setEventOnEditText()
    {
        mWinMgrParam.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;  // 기본값으로 원복
        mWindowMgr.updateViewLayout(mView, mWinMgrParam);
    }


    /**
     * popup window 에 포커스를 해제하고 home, back 이벤트를 다른 어플로 전달.
     */
    private void unsetEventOnEditText()
    {
        mWinMgrParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;  // back key 이벤트를 다른 어플에서 처리
        mWindowMgr.updateViewLayout(mView, mWinMgrParam);
    }

}
