package com.qthekan.qhere.radar;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qthekan.qhere.MainActivity;
import com.qthekan.util.qlog;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * All Pokemon Base Information (dictionary)
 */
public class PokeDict {
    public static ArrayList<Poke> mPokeList = new ArrayList<>();
    //private Context mContext = null;


//    /**
//     * @param context : main activity's context
//     */
//    public PokeDict(Context context)
//    {
//        mContext = context;
//    }


    public static int init()
    {
        AssetManager am = MainActivity.getIns().getAssets();

        try
        {
            InputStream is = am.open("PokeDict.json");
            Reader reader = new InputStreamReader(is);

            Gson gson = new Gson();
            mPokeList = gson.fromJson( reader, new TypeToken<ArrayList<Poke>>(){}.getType() );

//            qlog.i("count:" + mPokeList.size());
//            for(int i = 0 ; i < mPokeList.size() ; i++)
//            {
//                Poke p = mPokeList.get(i);
//                qlog.d(p.toStr());
//            }
        }
        catch (IOException e)
        {
            qlog.e( e.toString() );
            System.exit(0);
            return -1;
        }

        return 0;
    }


    /**
     * ID 로 정렬된 포켓몬 리스트를 가져온다.
     */
    public void getListByID()
    {
        mPokeList.clear();
        init();
        Collections.sort(mPokeList, new Comparator<Poke>() {
            @Override
            public int compare(Poke t1, Poke t2) {
                int t1id = t1.mID;
                int t2id = t2.mID;

                return Integer.compare(t1id, t2id);
            }
        });
    }


    /**
     * Name 으로 정렬된 포켓몬 리스트를 가져온다.
     */
    public void getListByName()
    {
        mPokeList.clear();
        init();
        Collections.sort(mPokeList, new Comparator<Poke>() {
            @Override
            public int compare(Poke t1, Poke t2) {
                return t1.mName.compareTo(t2.mName);
            }
        });
    }


    public static String getNameByID(int id)
    {
        if( mPokeList.size() == 0 )
        {
            init();
        }

        for(int i = 0 ; i < mPokeList.size() ; i++)
        {
            Poke p = mPokeList.get(i);
            if(p.mID == id)
            {
                return p.mName;
            }
        }

        return "";
    }


    public static int getWeatherByID(int id)
    {
        if( mPokeList.size() == 0 )
        {
            init();
        }

        for(int i = 0 ; i < mPokeList.size() ; i++)
        {
            Poke p = mPokeList.get(i);
            if(p.mID == id)
            {
                return p.mWeather;
            }
        }

        return 0;
    }

    private void add(int id, String name)
    {
        mPokeList.add( new Poke(id, name) );
    }

}
