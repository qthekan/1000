package com.qthekan.qhere.radar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qthekan.qhere.MainActivity;
import com.qthekan.qhere.R;
import com.qthekan.qhere.radar.listview.CustomAdapter;
import com.qthekan.util.qlog;
import com.qthekan.util.qutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class RadarActivity extends AppCompatActivity {

    private PokeDict mPokeDict;
    public static ListView mListViewPoke;
    private EditText mEtCP;
    private EditText mEtIV;
    private EditText mEtLV;

    private RadioGroup mRadioGroup;
    private RadioButton mRbSeoul, mRbNewYork, mRbLondon; // 포케맵이 다 다운되어 현재 살아난 싱가폴맵을 서울맵인스턴스에 연결해서 사용.

    private TextView mTvResult;


    /**
     * set member variables relative with view
     */
    private void initView()
    {
        mListViewPoke = findViewById(R.id.listViewPoke);

        mEtCP = findViewById(R.id.etMinCP);
        mEtIV = findViewById(R.id.etMinIV);
        mEtLV = findViewById(R.id.etMinLv);

        mRadioGroup = findViewById(R.id.rgRegion);
//        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                qutil.showToast(getApplicationContext(), "Warning!! \nWhen move between countries, more than 2 hours of rest is required.");
//            }
//        });

        mRbSeoul = findViewById(R.id.rbSG);
        mRbLondon = findViewById(R.id.rbLondon);
        mRbNewYork = findViewById(R.id.rbNewYork);

        mTvResult = findViewById(R.id.tvResult);
    }


    private void initViewValue()
    {
        //===========================================================
        // init Poke Dictionary
        //===========================================================
        //mPokeDict = new PokeDict(this);
        mPokeDict = new PokeDict();
        mPokeDict.getListByID();

        //===========================================================
        // draw List View
        //===========================================================
        CustomAdapter adapter = new CustomAdapter();
        mListViewPoke.setAdapter(adapter);

        for(Poke p: PokeDict.mPokeList)
        {
            String id = String.format("%03d", p.mID);
            AssetManager assetManager = getAssets();
            InputStream is = null;
            try {
                is = assetManager.open("poke_img/" + id + ".png");
            }
            catch (IOException e) {
                try {
                    is = assetManager.open("poke_img/unknown.png");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            Bitmap bitmap = BitmapFactory.decodeStream(is);
            adapter.addItem(bitmap, id, p.mName);
        }

        //===========================================================
        // load previous user input data
        //===========================================================
        mAppData = getSharedPreferences("appData", MODE_PRIVATE);
        loadUserInput();

    }


    private void checkListViewByDefault()
    {
        for(int i = 0 ; i < mPokeDict.mPokeList.size() ; i++)
        {
            Poke p = mPokeDict.mPokeList.get(i);
            if(p.mFlag)
            {
                mListViewPoke.setItemChecked(i, true);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);

        initView();
        initViewValue();

        MainActivity.getIns().mAds.showInterAds();
        qutil.showToast(this, "Lv.30 User Olny Correct Data!!");
    }


    /**
     * SelectAll button click event handler
     */
    public void onAll(View v)
    {
        for(int i = 0 ; i < mListViewPoke.getCount() ; i++)
        {
            mListViewPoke.setItemChecked(i, true);
        }
    }


    /**
     * clear button click event handler
     */
    public void onClear(View v)
    {
        qlog.i("Clear");
        mListViewPoke.clearChoices();
        mEtCP.setText(null);
        mEtIV.setText(null);
        mEtLV.setText(null);
    }


    public void onSave(View v)
    {
        saveUserInput();
    }


    public void onLoad(View v)
    {
        loadUserInput();
    }


    /**
     * default button click event handler
     */
    public void onDefault(View v)
    {
        qlog.i("Default");

        onClear(null);
        checkListViewByDefault();
    }


    String mSite = "";
    /**
     * search button click event handler
     */
    public void onSearch(View v)
    {
        qlog.i("Search");
        saveUserInput();
        mTvResult.setVisibility(View.VISIBLE);
        if(mSelectedPokeIDs.length() < 1)
        {
            qutil.showToast(this, "Select Pokemon Please.");
            return;
        }

        // send search request to server
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //String site = "https://seoulpokemap.com";
                mSite = "https://sgpokemap.com";

                if( mRbLondon.isChecked() )
                {
                    mSite = "https://londonpogomap.com";
                }
                else if( mRbNewYork.isChecked() )
                {
                    mSite = "https://nycpokemap.com";
                }

                sendHttpReq(mSite, mSelectedPokeIDs);

                responseJsonToObject();
            }
        });
    }


    private String mSelectedPokeIDs = "";
    private void getSelectedPokeIDs()
    {
        mSelectedPokeIDs = "";

        SparseBooleanArray booleanArray = mListViewPoke.getCheckedItemPositions();

        for(int i = 0; i < PokeDict.mPokeList.size() ; i++)
        {
            if( booleanArray.get(i) )
            {
                mSelectedPokeIDs += PokeDict.mPokeList.get(i).mID + ",";
            }
        }

        qlog.i("mSelectedPokeIDs:" + mSelectedPokeIDs);
    }


    /**
     * get filter value from user input
     */
    private int mMinCP = 0;
    private int mMinIV = 0;
    private int mMinLV = 0;
    private void getFilterValue()
    {
        mMinCP = qutil.parseInt( mEtCP.getText().toString(), 0 );
        mMinIV = qutil.parseInt( mEtIV.getText().toString(), 0 );
        mMinLV = qutil.parseInt( mEtLV.getText().toString(), 0 );

        qlog.i("CP:"+mMinCP + " IV:"+mMinIV + " LV:"+mMinLV);
    }


    /**
     *
     * @param ids : Poke mName list (ex: 3,6,9,59,65);
     */
    private String mJsonResponse = "";
    private void sendHttpReq(String site, String ids)
    {
        try {
            getCookies(site);

            String cmd = site + "/query2.php?since=0&mons=" + ids;
            URL url = new URL(cmd);
            qlog.i("url:" + cmd);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Referer", mSite);
            connection.setRequestProperty("Cookie", mCookie);
            connection.connect();

            BufferedReader in = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
            String inputLine;
            StringBuffer response = new StringBuffer();

            while( (inputLine = in.readLine()) != null )
            {
                response.append(inputLine);
            }
            in.close();
            connection.disconnect();

            qlog.i("response\n" + response);
            mJsonResponse = response.toString();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * parsing json response to Poke list
     */
    private void responseJsonToObject()
    {
        MainActivity.mListPoke.clear();

        if(mJsonResponse == null)
        {
            qlog.e("mJsonResponse is null");
            return;
        }

        JsonObject jsonObject = new JsonParser().parse(mJsonResponse).getAsJsonObject();

        JsonArray pokeList = jsonObject.get("pokemons").getAsJsonArray();
        qlog.i("pokeList.size():" + pokeList.size() );

        final int total = pokeList.size();
        for(int i = 0 ; i < pokeList.size() ; i++)
        {
            final int index = i;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvResult.setText( index + " / " + total );
                }
            });

            JsonElement element = pokeList.get(i);
            JsonObject o = element.getAsJsonObject();

            Poke p = new Poke();
            try {
                p.mID = o.get("pokemon_id").getAsInt();
                p.mName = PokeDict.getNameByID(p.mID);
                p.mLat = o.get("lat").getAsDouble();
                p.mLng = o.get("lng").getAsDouble();
                p.mDespawn = o.get("despawn").getAsLong();
                p.mDisguise = o.get("disguise").getAsInt();
                p.mAtt = o.get("attack").getAsInt();
                p.mDef = o.get("defence").getAsInt();
                p.mHp = o.get("stamina").getAsInt();
                p.mMove1 = o.get("move1").getAsInt();
                p.mMove2 = o.get("move2").getAsInt();
                p.mCostume = o.get("costume").getAsInt();
                p.mGender = o.get("gender").getAsInt();
                p.mShiny = o.get("shiny").getAsInt();
                p.mForm = o.get("form").getAsInt();
                p.mCP = o.get("cp").getAsInt();
                p.mLevel = o.get("level").getAsInt();
                p.mWeather = o.get("weather").getAsInt();
            }
            catch(Exception e) {
                qlog.e("error occurred index:" + i);
                e.printStackTrace();
                continue;
            }

            // apply filter
            if( checkFilter(p) < 0 )
            {
                qlog.i("exclude index:" + i + "\n" + p.toStr() );
                continue;
            }

            qlog.i("index:" + i + "\n" + p.toStr() );
            MainActivity.mListPoke.add(p);
        }

        MainActivity.printListPoke();

        /**
         * GUI 관련된 작업은 main thread 에서 수행해야 되서 다음과 같이 처리함.
         */
        MainActivity.getIns().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvResult.setVisibility(View.INVISIBLE);
                MainActivity.getIns().invisibleSubMenu();
                MainActivity.getIns().drawPokeListInMap( getCheckedSite() );
            }
        });

        finish();
    }


    private int checkFilter(Poke p)
    {
        if( p.mLevel < mMinLV )
        {
            return -1;
        }

        if( p.mCP < mMinCP )
        {
            return -1;
        }

        if( calculateIV(p.mAtt, p.mDef, p.mHp) < mMinIV )
        {
            return -1;
        }

        return 0;
    }


    public static double calculateIV(int att, int def, int hp)
    {
        int max = 45;
        double sumOfAbility = att + def + hp;
        double iv = sumOfAbility / max * 100;
        qlog.i("iv:" + iv);
        return iv;
    }


    /**
     * https 세션 유지를 해야 서버에서 제대로된 contents 를 보내기 때문에
     * 최초 홈페이지 접속시 cookie 를 저장후 request 보낼때 사용할 목적.
     */
    private String mCookie = "";
    private void getCookies(String site)
    {
        try {
            //HttpsURLConnection conn = (HttpsURLConnection) new URL("https://seoulpokemap.com/").openConnection();
            HttpsURLConnection conn = (HttpsURLConnection) new URL(site).openConnection();
            mCookie = conn.getHeaderField("Set-Cookie");
            qlog.i("cookie\n" + mCookie);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    //===============================================================
    // site (nation, region)
    //===============================================================
    public static final int mSEOUL = 0;
    public static final int mNEWYORK = 1;
    public static final int mRONDON = 2;

    private int getCheckedSite()
    {
        if( mRbNewYork.isChecked() )
        {
            return mNEWYORK;
        }
        else if( mRbLondon.isChecked() )
        {
            return mRONDON;
        }

        return mSEOUL;
    }


    //===============================================================
    // change activity
    //===============================================================
    public void goMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    //===============================================================
    // save, load user input data
    //===============================================================
    SharedPreferences mAppData;
    private void saveUserInput()
    {
        getFilterValue();
        getSelectedPokeIDs();

        SharedPreferences.Editor editor = mAppData.edit();
        //editor.clear();

        //-----------------------------------------------------------
        // list view info
        //-----------------------------------------------------------
        SparseBooleanArray booleanArray = mListViewPoke.getCheckedItemPositions();
        for(int i = 0; i < PokeDict.mPokeList.size() ; i++)
        {
            // poke id start with 1. then, mID - 1
            String id = PokeDict.mPokeList.get(i).mID - 1 + "";

            if( booleanArray.get(i) )
            {
                editor.putBoolean(id, true);
                qlog.e("save id:" + id);
            }
            else
            {
                editor.putBoolean(id, false);
                qlog.e("save id:" + id);
            }
        }

        //-----------------------------------------------------------
        // edit text info
        //-----------------------------------------------------------
        editor.putInt("cp", mMinCP);
        editor.putInt("iv", mMinIV);
        editor.putInt("lv", mMinLV);

        //-----------------------------------------------------------
        // radio button info
        //-----------------------------------------------------------
        editor.putInt("site", getCheckedSite() );

        editor.commit();
    }


    private void loadUserInput()
    {
        //-----------------------------------------------------------
        // list view info
        //-----------------------------------------------------------
        for(int i = 0 ; i < PokeDict.mPokeList.size() ; i++)
        {
            boolean flag = mAppData.getBoolean(i + "", false);
            if(flag) {
                mListViewPoke.setItemChecked(i, flag);
                qlog.e("load id:" + i);
            }
        }

        //-----------------------------------------------------------
        // edit text info
        //-----------------------------------------------------------
        mMinCP = mAppData.getInt("cp", 0);
        mMinIV = mAppData.getInt("iv", 0);
        mMinLV = mAppData.getInt("lv", 0);

        mEtCP.setText(mMinCP + "");
        mEtIV.setText(mMinIV + "");
        mEtLV.setText(mMinLV + "");

        //-----------------------------------------------------------
        // radio button info
        //-----------------------------------------------------------
        RadioButton radio = (RadioButton) mRadioGroup.getChildAt( mAppData.getInt("site", 0) );
        radio.toggle();

    }

}
