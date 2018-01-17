package com.qthekan.qhere.radar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qthekan.qhere.MainActivity;
import com.qthekan.qhere.R;
import com.qthekan.util.qlog;
import com.qthekan.util.qutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class RadarActivity extends AppCompatActivity {

    private PokeDict mPokeDict;
    private ListView mListViewPokeSelect;
    private EditText mEtCP;
    private EditText mEtIV;
    private EditText mEtLV;


    /**
     * set member variables relative with view
     */
    private void initView()
    {
        mListViewPokeSelect = findViewById(R.id.listViewPoke);

        mEtCP = findViewById(R.id.etMinCP);
        mEtIV = findViewById(R.id.etMinIV);
        mEtLV = findViewById(R.id.etMinLv);
    }


    private void initViewValue()
    {
        //===========================================================
        // init Poke Dictionary
        //===========================================================
        mPokeDict = new PokeDict(this);
        mPokeDict.getListByID();

        //===========================================================
        // draw List View
        //===========================================================
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, mPokeDict.mPokeList);
        mListViewPokeSelect.setAdapter(adapter);

        checkListViewByDefault();
    }


    private void checkListViewByDefault()
    {
        for(int i = 0 ; i < mPokeDict.mPokeList.size() ; i++)
        {
            Poke p = mPokeDict.mPokeList.get(i);
            if(p.mFlag)
            {
                mListViewPokeSelect.setItemChecked(i, true);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);

        initView();
        initViewValue();

        qutil.showToast(this, "Lv.30 User Olny Correct Data!!");
    }


    /**
     * clear button click event handler
     */
    public void onClear(View v)
    {
        qlog.i("start");
        mListViewPokeSelect.clearChoices();
        mEtCP.setText(null);
        mEtIV.setText(null);
        mEtLV.setText(null);
    }


    /**
     * default button click event handler
     */
    public void onDefault(View v)
    {
        qlog.i("start");

        onClear(null);
        checkListViewByDefault();
    }


    /**
     * search button click event handler
     */
    public void onSearch(View v)
    {
        qlog.i("onSearch() start");

        getFilterValue();
        getSelectedPokeIDs();

        // send search request to server
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                sendHttpReq(mSelectedPokeIDs);

                responseJsonToObject();
            }
        });
    }


    private String mSelectedPokeIDs = "";
    private void getSelectedPokeIDs()
    {
        mSelectedPokeIDs = "";

        SparseBooleanArray booleanArray = mListViewPokeSelect.getCheckedItemPositions();

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
        mMinCP = qutil.parseInt( mEtCP.getText().toString() );
        mMinIV = qutil.parseInt( mEtIV.getText().toString() );
        mMinLV = qutil.parseInt( mEtLV.getText().toString() );

        qlog.i("CP:"+mMinCP + " IV:"+mMinIV + " LV:"+mMinLV);
    }


    /**
     *
     * @param ids : Poke ID list (ex: 3,6,9,59,65);
     */
    private String mJsonResponse = "";
    private void sendHttpReq(String ids)
    {
        try {
            getCookies();

            String cmd = "https://seoulpokemap.com/query2.php?since=0&mons=" + ids;
            URL url = new URL(cmd);
            qlog.i("url:" + cmd);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Referer", "https://seoulpokemap.com/");
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

        for(int i = 0 ; i < pokeList.size() ; i++)
        {
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
                MainActivity.getIns().invisibleSubMenu();
                MainActivity.getIns().drawPokeListInMap();
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
    private void getCookies()
    {
        try {
            HttpsURLConnection conn = (HttpsURLConnection) new URL("https://seoulpokemap.com/").openConnection();
            mCookie = conn.getHeaderField("Set-Cookie");
            qlog.i("cookie\n" + mCookie);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    //===============================================================
    // change activity
    //===============================================================
    public void goMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
