package com.qthekan.qhere.raid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qthekan.qhere.MainActivity;
import com.qthekan.qhere.R;
import com.qthekan.qhere.radar.Poke;
import com.qthekan.qhere.radar.PokeDict;
import com.qthekan.util.qlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;


public class RaidActivity extends AppCompatActivity
{
    private RadioGroup mRadioGroup;
    private RadioButton mRbSG, mRbNewYork, mRbLondon;

    private CheckBox mCbY, mCbR, mCbB;
    private CheckBox mCbL1, mCbL2, mCbL3, mCbL4, mCbL5;
    private CheckBox mCbClear, mCbRain, mCbPCloud, mCbCloud, mCbWind, mCbSnow, mCbFog;
    private CheckBox mCbExGym;

    private TextView mTvResult; // Search 클릭후 진행도 표시 xxx/999


    /**
     * set member variables relative with view
     */
    private void initView()
    {
        mRadioGroup = findViewById(R.id.rgRegion);

        mRbSG = findViewById(R.id.rbSG);
        mRbLondon = findViewById(R.id.rbLondon);
        mRbNewYork = findViewById(R.id.rbNewYork);

        mCbY = findViewById(R.id.cbYellow);
        mCbR = findViewById(R.id.cbRed);
        mCbB = findViewById(R.id.cbBlue);

        mCbL1 = findViewById(R.id.cbLv1);
        mCbL2 = findViewById(R.id.cbLv2);
        mCbL3 = findViewById(R.id.cbLv3);
        mCbL4 = findViewById(R.id.cbLv4);
        mCbL5 = findViewById(R.id.cbLv5);

        mCbClear = findViewById(R.id.cbClear);
        mCbRain = findViewById(R.id.cbRainy);
        mCbPCloud = findViewById(R.id.cbPCloudy);
        mCbCloud = findViewById(R.id.cbCloudy);
        mCbWind = findViewById(R.id.cbWindy);
        mCbSnow = findViewById(R.id.cbSnow);
        mCbFog = findViewById(R.id.cbFog);

        mCbExGym = findViewById(R.id.cbExGym);

        mTvResult = findViewById(R.id.tvResult);
    }


    private void initViewValue()
    {

        //===========================================================
        // load previous user input data
        //===========================================================
        mAppData = getSharedPreferences("appData", MODE_PRIVATE);
        loadUserInput();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raid);

        initView();
        initViewValue();

        MainActivity.getIns().mAds.showInterAds();
    }


    /**
     * SelectAll button click event handler
     */
    public void onAll(View v)
    {
        qlog.e("All");
        setall(true);
        mCbExGym.setChecked(false);
    }


    /**
     * clear button click event handler
     */
    public void onClear(View v)
    {
        qlog.e("Clear");
        setall(false);
    }


    private void setall(boolean b)
    {
        mCbY.setChecked(b);
        mCbR.setChecked(b);
        mCbB.setChecked(b);

        mCbL1.setChecked(b);
        mCbL2.setChecked(b);
        mCbL3.setChecked(b);
        mCbL4.setChecked(b);
        mCbL5.setChecked(b);

        mCbClear.setChecked(b);
        mCbRain.setChecked(b);
        mCbPCloud.setChecked(b);
        mCbCloud.setChecked(b);
        mCbWind.setChecked(b);
        mCbSnow.setChecked(b);
        mCbFog.setChecked(b);

        mCbExGym.setChecked(b);
    }


    public void onSave(View v)
    {
        saveUserInput();
    }


    public void onLoad(View v)
    {
        loadUserInput();
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

        // send search request to server
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mSite = "https://sgpokemap.com/raids.php";

                if( mRbNewYork.isChecked() )
                {
                    mSite = "https://nycpokemap.com/raids.php";
                }

                sendHttpReq(mSite);

                responseJsonToObject();
            }
        });
    }


    private String mJsonResponse = "";
    private void sendHttpReq(String site)
    {
        try {
            getCookies(site);

            URL url = new URL(site);
            qlog.i("url:" + site);

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

            qlog.e("response\n" + response);
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


    ArrayList<RaidWeather> mListWeather = new ArrayList<>();
    /**
     * parsing json response to Poke list
     */
    private void responseJsonToObject()
    {
        MainActivity.mListRaid.clear();

        if(mJsonResponse == null)
        {
            qlog.e("mJsonResponse is null");
            return;
        }

        JsonObject jsonObject = new JsonParser().parse(mJsonResponse).getAsJsonObject();

        // 날씨 처리
        mListWeather.clear();
        JsonArray weatherList = jsonObject.get("weathers").getAsJsonArray();
        for(int i = 0 ; i < weatherList.size() ; i++)
        {
            JsonElement e = weatherList.get(i);
            JsonObject o = e.getAsJsonObject();

            RaidWeather w = new RaidWeather();
            w.mCellID = o.get("cell_id").getAsString();
            w.mWeather = o.get("weather").getAsInt();

            mListWeather.add(w);
        }

        // 레이드 처리
        JsonArray raidList = jsonObject.get("raids").getAsJsonArray();

        final int total = raidList.size();
        for(int i = 0 ; i < raidList.size() ; i++)
        {
            final int index = i;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvResult.setText( index + " / " + total );
                }
            });

            JsonElement element = raidList.get(i);
            JsonObject o = element.getAsJsonObject();

            RaidInfo r = new RaidInfo();
            try {
                r.mCellID = o.get("cell_id").getAsString();
                r.mExRaid = ( o.get("ex_raid_eligible").getAsInt() == 1 ? true : false );
                r.mGymName = o.get("gym_name").getAsString();
                r.mLat = o.get("lat").getAsDouble();
                r.mLng = o.get("lng").getAsDouble();
                r.mLevel = o.get("level").getAsInt();
                r.mPokemonID = o.get("pokemon_id").getAsInt();
                r.mEnd = o.get("raid_end").getAsLong();
                r.mSpawn = o.get("raid_spawn").getAsLong();
                r.mStart = o.get("raid_start").getAsLong();
                r.mTeam = o.get("team").getAsInt();
                r.mID = o.get("id").getAsLong();

                r.mWeather = getWeatherByCellID(r.mCellID);
                r.mPokemonName = PokeDict.getNameByID(r.mPokemonID);
            }
            catch(Exception e) {
                qlog.e("error occurred index:" + i);
                e.printStackTrace();
                continue;
            }

            // apply filter
            if( checkFilter(r) < 0 )
            {
                qlog.i("exclude index:" + i + "\n" + r.toStr() );
                continue;
            }

            qlog.i("index:" + i + " " + r.toStr() );
            if( MainActivity.mListRaid.contains(r) )
            {
                if(r.mPokemonID != 0)
                {
                    MainActivity.mListRaid.remove(r);
                }
            }
            MainActivity.mListRaid.add(r);
        }

        /**
         * GUI 관련된 작업은 main thread 에서 수행해야 되서 다음과 같이 처리함.
         */
        MainActivity.getIns().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.getIns().invisibleSubMenu();
                MainActivity.getIns().drawRaidListInMap( getCheckedSite() );
            }
        });

        finish();
    }


    private int getWeatherByCellID(String cellID)
    {
        for(RaidWeather w : mListWeather)
        {
            if( cellID.equalsIgnoreCase(w.mCellID) )
            {
                return w.mWeather;
            }
        }

        return -1;
    }


    private int checkFilter(RaidInfo r)
    {
        // check team
        if(r.mTeam == Team.BLUE)
        {
            if( !mCbB.isChecked() )
            {
                qlog.e("except Blue");
                return -1;
            }
        }

        if(r.mTeam == Team.RED)
        {
            if( !mCbR.isChecked() )
            {
                qlog.e("except Red");
                return -1;
            }
        }

        if(r.mTeam == Team.YELLOW)
        {
            if( !mCbY.isChecked() )
            {
                qlog.e("except Yellow");
                return -1;
            }
        }

        // check raid level
        switch (r.mLevel)
        {
            case 1:
                if( !mCbL1.isChecked() )
                {
                    qlog.e("except Lv1");
                    return -1;
                }
                break;
            case 2:
                if( !mCbL2.isChecked() )
                {
                    qlog.e("except Lv2");
                    return -1;
                }
                break;
            case 3:
                if( !mCbL3.isChecked() )
                {
                    qlog.e("except Lv3");
                    return -1;
                }
                break;
            case 4:
                if( !mCbL4.isChecked() )
                {
                    qlog.e("except Lv4");
                    return -1;
                }
                break;
            case 5:
                if( !mCbL5.isChecked() )
                {
                    qlog.e("except Lv5");
                    return -1;
                }
                break;
            default:
                break;
        }

        // check raid weather
        switch (r.mWeather)
        {
            case 1:
                if( !mCbClear.isChecked() )
                {
                    qlog.e("except Clear");
                    return -1;
                }
                break;
            case 2:
                if( !mCbRain.isChecked() )
                {
                    qlog.e("except Rainy");
                    return -1;
                }
                break;
            case 3:
                if( !mCbPCloud.isChecked() )
                {
                    qlog.e("except Partly Cloudy");
                    return -1;
                }
                break;
            case 4:
                if( !mCbCloud.isChecked() )
                {
                    qlog.e("except Cloudy");
                    return -1;
                }
                break;
            case 5:
                if( !mCbWind.isChecked() )
                {
                    qlog.e("except Windy");
                    return -1;
                }
                break;
            case 6:
                if( !mCbSnow.isChecked() )
                {
                    qlog.e("except Snow");
                    return -1;
                }
                break;
            case 7:
                if( !mCbFog.isChecked() )
                {
                    qlog.e("except Fog");
                    return -1;
                }
                break;
            default:
                break;
        }

        // ExGym check
        if( mCbExGym.isChecked() )
        {
            if( !r.mExRaid )
            {
                qlog.e("not ExGym");
                return -1;
            }
        }

        return 0;
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
    public static final int mSG = 0;
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

        return mSG;
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
        SharedPreferences.Editor editor = mAppData.edit();
        //editor.clear();

        //-----------------------------------------------------------
        // radio button info
        //-----------------------------------------------------------
        editor.putInt("raid_site", getCheckedSite() );

        editor.putBoolean("yellow", mCbY.isChecked() );
        editor.putBoolean("red", mCbR.isChecked() );
        editor.putBoolean("blue", mCbB.isChecked() );

        editor.putBoolean("lv1", mCbL1.isChecked() );
        editor.putBoolean("lv2", mCbL2.isChecked() );
        editor.putBoolean("lv3", mCbL3.isChecked() );
        editor.putBoolean("lv4", mCbL4.isChecked() );
        editor.putBoolean("lv5", mCbL5.isChecked() );

        editor.putBoolean("clear", mCbClear.isChecked() );
        editor.putBoolean("rain", mCbRain.isChecked() );
        editor.putBoolean("pcloud", mCbPCloud.isChecked() );
        editor.putBoolean("cloud", mCbCloud.isChecked() );
        editor.putBoolean("wind", mCbWind.isChecked() );
        editor.putBoolean("snow", mCbSnow.isChecked() );
        editor.putBoolean("fog", mCbFog.isChecked() );

        editor.putBoolean("exgym", mCbExGym.isChecked() );

        editor.commit();
    }


    private void loadUserInput()
    {
        //-----------------------------------------------------------
        // radio button info
        //-----------------------------------------------------------
        RadioButton radio = (RadioButton) mRadioGroup.getChildAt( mAppData.getInt("raid_site", 0) );
        radio.toggle();

        mCbY.setChecked( mAppData.getBoolean("yellow", true));
        mCbR.setChecked( mAppData.getBoolean("red", true));
        mCbB.setChecked( mAppData.getBoolean("blue", true));

        mCbL1.setChecked( mAppData.getBoolean("lv1", true));
        mCbL2.setChecked( mAppData.getBoolean("lv2", true));
        mCbL3.setChecked( mAppData.getBoolean("lv3", true));
        mCbL4.setChecked( mAppData.getBoolean("lv4", true));
        mCbL5.setChecked( mAppData.getBoolean("lv5", true));

        mCbClear.setChecked( mAppData.getBoolean("clear", true));
        mCbRain.setChecked( mAppData.getBoolean("rain", true));
        mCbPCloud.setChecked( mAppData.getBoolean("pcloud", true));
        mCbCloud.setChecked( mAppData.getBoolean("cloud", true));
        mCbWind.setChecked( mAppData.getBoolean("wind", true));
        mCbSnow.setChecked( mAppData.getBoolean("snow", true));
        mCbFog.setChecked( mAppData.getBoolean("fog", true));

        mCbExGym.setChecked( mAppData.getBoolean("exgym", true));
    }

}
