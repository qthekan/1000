package com.qthekan.qhere.radar;


import android.support.annotation.NonNull;

/**
 * Each Pokemon Information
 */
public class Poke{
    public boolean mFlag;
    public int mID;
    public String mName;
    public double mLat;
    public double mLng;
    public long mDespawn;
    public int mDisguise;  // 변장
    public int mAtt;
    public int mDef;
    public int mHp;
    public int mMove1;    // skill
    public int mMove2;
    public int mCostume;  // 복장?
    public int mGender;
    public int mShiny;    // ?
    public int mForm;     // ?
    public int mCP;
    public int mLevel;
    public int mWeather;


    public Poke()
    {

    }


    public Poke(int id, String name)
    {
        mFlag = true;
        mID = id;
        mName = name;
    }


    public String toStr()
    {
        return mID + " " + mName + " " + mLevel + " " + mCP + " " + mAtt + " " + mDef + " " + mHp + " " + mDespawn + " " + mLat + " " + mLng + " " + mFlag;
    }


    @Override
    public String toString()
    {
        return String.format("%03d    %s", mID, mName);
    }
}
