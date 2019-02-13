package com.qthekan.qhere.raid;


/**
 * Each Raid Information
 */
public class RaidInfo {
    public String mCellID;
    public String mCP;
    public boolean mExRaid;
    public String mGymName;
    public long mID;
    public double mLat;
    public double mLng;
    public int mLevel;
    public String mMove1;
    public String mMove2;
    public int mPokemonID;
    public long mEnd;
    public long mSpawn;
    public long mStart;
    public int mSponsor;
    public int mTeam;

    public int mWeather;
    public String mPokemonName;


    public String toStr()
    {
        return "mCellID: " + mCellID + ", ID: " + mID + ", mExRaid: " + mExRaid + ", mGymName: " + mGymName
                + ", mLat: " + mLat + ", mLng: " + mLng + ", mLevel: " + mLevel
                + ", mPokemonID: " + mPokemonID + ", mStart: " + mStart + ", mEnd: " + mEnd
                + ", mTeam: " + mTeam + ", mWeather: " + mWeather;
    }


    @Override
    public boolean equals(Object o)
    {
        if( o != null && o instanceof RaidInfo )
        {
            return this.mGymName.equalsIgnoreCase( ((RaidInfo) o).mGymName );
        }

        return false;
    }
}
