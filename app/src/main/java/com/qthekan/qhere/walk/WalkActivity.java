package com.qthekan.qhere.walk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;
import com.qthekan.qhere.MainActivity;
import com.qthekan.qhere.R;
import com.qthekan.util.qSave;
import com.qthekan.util.qlog;
import com.qthekan.util.qutil;


public class WalkActivity extends AppCompatActivity
{
    EditText mEtInterval;
    EditText mEtPos1, mEtPos2, mEtPos3, mEtPos4, mEtPos5, mEtPos6;

    RadioGroup mRgWalk;
    RadioButton mRbWalk1, mRbWalk2, mRbWalk3, mRbWalk4, mRbWalk5;

    Button mBtStart;

    qSave mSave = new qSave();


    private void initView()
    {
        mEtInterval = findViewById(R.id.etInterval);
        mEtPos1 = findViewById(R.id.etPos1);
        mEtPos2 = findViewById(R.id.etPos2);
        mEtPos3 = findViewById(R.id.etPos3);
        mEtPos4 = findViewById(R.id.etPos4);
        mEtPos5 = findViewById(R.id.etPos5);
        mEtPos6 = findViewById(R.id.etPos6);

        mRgWalk = findViewById(R.id.rgWalk);

        mRbWalk1 = findViewById(R.id.rbWalk1);
        mRbWalk2 = findViewById(R.id.rbWalk2);
        mRbWalk3 = findViewById(R.id.rbWalk3);
        mRbWalk4 = findViewById(R.id.rbWalk4);
        mRbWalk5 = findViewById(R.id.rbWalk5);

        mBtStart = findViewById(R.id.btWalkStart);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk);

        initView();

        loadRadioGroup();
        load();
    }


    private void getUserInput()
    {
        String interval = mEtInterval.getText().toString();
        String pos1 = mEtPos1.getText().toString();
        String pos2 = mEtPos2.getText().toString();
        String pos3 = mEtPos3.getText().toString();
        String pos4 = mEtPos4.getText().toString();
        String pos5 = mEtPos5.getText().toString();
        String pos6 = mEtPos6.getText().toString();

        MainActivity.getIns().mInterval = qutil.parseInt(interval, 60);
        MainActivity.getIns().mListPosition.clear();
        if(!"".equalsIgnoreCase(pos1))
        {
            MainActivity.getIns().mListPosition.add(pos1);
        }

        if(!"".equalsIgnoreCase(pos2))
        {
            MainActivity.getIns().mListPosition.add(pos2);
        }

        if(!"".equalsIgnoreCase(pos3))
        {
            MainActivity.getIns().mListPosition.add(pos3);
        }

        if(!"".equalsIgnoreCase(pos4))
        {
            MainActivity.getIns().mListPosition.add(pos4);
        }

        if(!"".equalsIgnoreCase(pos5))
        {
            MainActivity.getIns().mListPosition.add(pos5);
        }

        if(!"".equalsIgnoreCase(pos6))
        {
            MainActivity.getIns().mListPosition.add(pos6);
        }
    }


    private int getWalkNum()
    {
        if(mRbWalk1.isChecked())
        {
            return 1;
        }
        else if(mRbWalk2.isChecked())
        {
            return 2;
        }
        else if(mRbWalk3.isChecked())
        {
            return 3;
        }
        else if(mRbWalk4.isChecked())
        {
            return 4;
        }
        else
        {
            return 5;
        }
    }


    private String getSaveFileName()
    {
        return "walk" + getWalkNum() + ".sav";
    }


    private void loadRadioGroup()
    {
        mAppData = getSharedPreferences("walkData", MODE_PRIVATE);

        try
        {
            RadioButton rb = findViewById( mAppData.getInt("walkNum", 0) );
            rb.toggle();
        }
        catch(Exception e)
        {
            mRbWalk1.toggle();
        }
    }

    private void load()
    {
        String strJson = mSave.load( getSaveFileName() );
        if(strJson == null)
        {
            setDefaultValue();
            return;
        }

        Gson gson = new Gson();
        WalkData data = gson.fromJson(strJson, WalkData.class);

        try {
            mEtInterval.setText(data.mInterval);
            mEtPos1.setText(data.mPosition1);
            mEtPos2.setText(data.mPosition2);
            mEtPos3.setText(data.mPosition3);
            mEtPos4.setText(data.mPosition4);
            mEtPos5.setText(data.mPosition5);
            mEtPos6.setText(data.mPosition6);

        } catch (Exception e) {
            qlog.e(e.getMessage());
        }
    }


    private void setDefaultValue()
    {
        mEtInterval.setText("150");
        mEtPos1.setText("");
        mEtPos2.setText("");
        mEtPos3.setText("");
        mEtPos4.setText("");
        mEtPos5.setText("");
        mEtPos6.setText("");
    }


    //=========================================================================
    // 버튼 이벤트 처리 함수
    //=========================================================================
    SharedPreferences mAppData;

    public void onSave(View v)
    {
        mAppData = getSharedPreferences("walkData", MODE_PRIVATE);
        SharedPreferences.Editor editor = mAppData.edit();
        editor.putInt("walkNum", mRgWalk.getCheckedRadioButtonId() );
        editor.commit();

        getUserInput();

        WalkData data = new WalkData();
        try {
            data.mInterval = mEtInterval.getText().toString();
            data.mPosition1 = mEtPos1.getText().toString();
            data.mPosition2 = mEtPos2.getText().toString();
            data.mPosition3 = mEtPos3.getText().toString();
            data.mPosition4 = mEtPos4.getText().toString();
            data.mPosition5 = mEtPos5.getText().toString();
            data.mPosition6 = mEtPos6.getText().toString();
        }
        catch (Exception e)
        {
            qlog.e( e.getMessage() );
        }

        String strJson = new Gson().toJson(data);
        qlog.e("strJson" + strJson);

        mSave.save(getSaveFileName(), strJson);
    }


    public void onLoad(View v)
    {
        load();
    }


    public void onStart(View v)
    {
        onSave(v);

        MainActivity.getIns().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.getIns().startWalk();
            }
        });
        finish();
    }


}
