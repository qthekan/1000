package com.qthekan.qhere.Favorite;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.qthekan.qhere.R;


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

    private void saveLatLng()
    {
        SharedPreferences.Editor editor = mAppData.edit();
        editor.clear();

        //-----------------------------------------------------------
        // list view info
        //-----------------------------------------------------------
        for(int i = 0 ; i < mAdapter.getCount() ; i++)
        {
            editor.putString(i+"name", mAdapter.getItem(i).mName);
            editor.putString(i+"latlng", mAdapter.getItem(i).mLatLng);
        }

        editor.commit();
    }


    private void loadLatLng()
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

}
