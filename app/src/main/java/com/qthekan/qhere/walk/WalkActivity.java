package com.qthekan.qhere.walk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.qthekan.qhere.MainActivity;
import com.qthekan.qhere.R;
import com.qthekan.util.qSave;
import com.qthekan.util.qlog;
import com.qthekan.util.qutil;


public class WalkActivity extends AppCompatActivity
{
    EditText mEtInterval;
    EditText mEtPos1;
    EditText mEtPos2;
    EditText mEtPos3;
    EditText mEtPos4;
    Button mBtStart;

    qSave mSave = new qSave("walk.sav");


    private void initView()
    {
        mEtInterval = findViewById(R.id.etInterval);
        mEtPos1 = findViewById(R.id.etPos1);
        mEtPos2 = findViewById(R.id.etPos2);
        mEtPos3 = findViewById(R.id.etPos3);
        mEtPos4 = findViewById(R.id.etPos4);
        mBtStart = findViewById(R.id.btWalkStart);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk);

        initView();

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


    private void getUserInput()
    {
        String interval = mEtInterval.getText().toString();
        String pos1 = mEtPos1.getText().toString();
        String pos2 = mEtPos2.getText().toString();
        String pos3 = mEtPos3.getText().toString();
        String pos4 = mEtPos4.getText().toString();

        MainActivity.getIns().mInterval = qutil.parseInt(interval, 3);
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
    }


    public void onSave(View v)
    {
        getUserInput();

        WalkData data = new WalkData();
        try {
            data.mInterval = mEtInterval.getText().toString();
            data.mPosition1 = mEtPos1.getText().toString();
            data.mPosition2 = mEtPos2.getText().toString();
            data.mPosition3 = mEtPos3.getText().toString();
            data.mPosition4 = mEtPos4.getText().toString();
        }
        catch (Exception e)
        {
            qlog.e( e.getMessage() );
        }

        String strJson = new Gson().toJson(data);
        qlog.e("strJson" + strJson);

        mSave.save(strJson);
    }


    public void load()
    {
        String strJson = mSave.load();
        Gson gson = new Gson();
        WalkData data = gson.fromJson(strJson, WalkData.class );

        try {
            mEtInterval.setText(data.mInterval);
            mEtPos1.setText(data.mPosition1);
            mEtPos2.setText(data.mPosition2);
            mEtPos3.setText(data.mPosition3);
            mEtPos4.setText(data.mPosition4);
        }
        catch (Exception e)
        {
            qlog.e( e.getMessage() );
        }
    }

}
