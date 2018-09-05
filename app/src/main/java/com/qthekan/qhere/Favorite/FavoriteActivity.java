package com.qthekan.qhere.Favorite;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qthekan.qhere.MainActivity;
import com.qthekan.qhere.R;
import com.qthekan.util.qlog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class FavoriteActivity extends AppCompatActivity
{
    // main view
    private Button mBtnAdd;
    private ListView mLvFavorite;
    private FavAdapter mAdapter;

    // popup view
    private LinearLayout mInputView;
    private EditText mEtName;
    private EditText mEtLatLng;
    private Button mBtnOk;
    private Button mBtnCancel;

    // runtime data
    public static int mCurrIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // init view
        mBtnAdd = findViewById(R.id.FavBtnAdd);
        mLvFavorite = findViewById(R.id.listViewFavorite);

        mInputView = findViewById(R.id.FavUserInputView);
        mEtName = findViewById(R.id.FavEtName);
        mEtLatLng = findViewById(R.id.FavEtLatLng);
        mBtnOk = findViewById(R.id.FavBtnOK);
        mBtnCancel = findViewById(R.id.favBtnCancel);

        // set data to view
        mAdapter = new FavAdapter();
        mLvFavorite.setAdapter(mAdapter);

        // 밀어서 삭제 적용
        final SwipeDetector swipe = new SwipeDetector();
        mLvFavorite.setOnTouchListener(swipe);

        mLvFavorite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mCurrIndex = position;

                if(swipe.swipeDetected())
                {
                    onDelete(view);
                }
                else
                {
                    mInputView.setVisibility(View.VISIBLE);
                    mEtName.setText(mAdapter.getItem(mCurrIndex).mName);
                    mEtLatLng.setText(mAdapter.getItem(mCurrIndex).mLatLng);
                }
            }
        });

        // 롱 클릭시 좌표 복사
        mLvFavorite.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                mCurrIndex = position;

                if(swipe.swipeDetected())
                {
                    onDelete(view);
                }
                else
                {
                    onCopy();
                }

                // 터치 이벤트를 여기에서 소멸시키도록 true 반환.
                return true;
            }
        });

        //===========================================================
        // load previous user input data
        //===========================================================
        mAppData = getSharedPreferences("appData", MODE_PRIVATE);
        loadLatLng();
    }


    public void onAdd(View v)
    {
        mInputView.setVisibility(View.VISIBLE);
        mEtName.setText("");
        mEtLatLng.setText("");
        mCurrIndex = mAdapter.getCount();
    }


    public void onDelete(View v)
    {
        mInputView.setVisibility(View.INVISIBLE);

        mAdapter.delItem(mCurrIndex);
        saveLatLng();
    }


    public void onCopy()
    {
        String latlng = mAdapter.getItem(mCurrIndex).mLatLng;
        ClipData data = ClipData.newPlainText("latlng", latlng);

        ClipboardManager board = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        board.setPrimaryClip(data);

        Toast.makeText(this, "clipboard copied: " + latlng, Toast.LENGTH_LONG).show();
    }


    public void onOk(View v)
    {
        mInputView.setVisibility(View.INVISIBLE);

        if(mCurrIndex < mAdapter.getCount() )
        {
            mAdapter.updateItem(mCurrIndex, new Data(mEtName.getText().toString(), mEtLatLng.getText().toString()));
        }
        else
        {
            mAdapter.addItem( mEtName.getText().toString(), mEtLatLng.getText().toString() );
        }

        saveLatLng();
    }


    public void onCancel(View v)
    {
        mInputView.setVisibility(View.INVISIBLE);
    }


    //===============================================================
    // save, load user input data
    //===============================================================
    SharedPreferences mAppData;
    private String mSaveDirName = "qhere";
    private String mSaveFileName = "fav.txt";

    private void saveLatLng()
    {
//        SharedPreferences.Editor editor = mAppData.edit();
//        editor.clear();
//
//        //-----------------------------------------------------------
//        // list view info
//        //-----------------------------------------------------------
//        for(int i = 0 ; i < mAdapter.getCount() ; i++)
//        {
//            editor.putString(i+"name", mAdapter.getItem(i).mName);
//            editor.putString(i+"latlng", mAdapter.getItem(i).mLatLng);
//        }
//
//        editor.commit();

        if( isExternalStorageWritable() == false )
        {
            qlog.e("external storage not writable!!");
            return;
        }

        String strJson = new Gson().toJson( mAdapter.mItemList );
        qlog.e("strJson" + strJson);

        File f = new File( getSaveDir(), mSaveFileName);
        try {
            FileWriter w = new FileWriter(f, false);
            w.write(strJson);
            w.close();
            MainActivity.getIns().showToast("save file:\n" + f.getAbsolutePath() );
            qlog.e("save success: " + f.getAbsolutePath() );
        }
        catch (IOException e) {
            qlog.e("file write fail: " + e.getMessage() );
        }
    }


    private void loadLatLng()
    {
        if( isExternalStorageReadable() == false )
        {
            qlog.e("external storage not readable!!");
            return;
        }

        File f = new File( getSaveDir(), mSaveFileName );
        if( f.exists() == false )
        {
            loadOldVersion();
            return;
        }

        String strData = "";
        try {
            BufferedReader r = new BufferedReader(new FileReader(f));
            String line;
            while( (line = r.readLine()) != null )
            {
                strData += line;
            }
            qlog.e("strData: " + strData);
            r.close();
        }
        catch (Exception e) {
            qlog.e("read fail: " + e.getMessage() );
            return;
        }

        Gson gson = new Gson();
        mAdapter.mItemList = gson.fromJson(strData, new TypeToken< ArrayList<Data> >(){}.getType() );
    }


    private void loadOldVersion()
    {
        //-----------------------------------------------------------
        // list view info
        //-----------------------------------------------------------
        for(int i = 0 ; i < 1000 ; i++)
        {
            String name = mAppData.getString(i+"name", null);
            String latlng = mAppData.getString(i+"latlng", null);

            if(name == null)
            {
                Log.i("loadLatLng()", "name is null");
                break;
            }

            mAdapter.addItem(name, latlng);
        }
    }


    public File getSaveDir()
    {
        File f = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), mSaveDirName);
        if( !f.mkdirs() )
        {
            qlog.e("mkdirs() fail: " + f.getAbsolutePath() );
        }

        return f;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
