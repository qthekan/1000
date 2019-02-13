package com.qthekan.qhere.raid;

public class RaidWeather
{
    public String mCellID;
    public int mWeather;


    public static String getWeatherStr(int i)
    {
        switch(i) {
            case 0:
                return "Unknown";
            case 1:
                return "Clear";
            case 2:
                return "Rainy";
            case 3:
                return "Partly Cloudy";
            case 4:
                return "Cloudy";
            case 5:
                return "Windy";
            case 6:
                return "Snow";
            case 7:
                return "Fog";
            default:
                return "Unknown";
        }
    }

}
