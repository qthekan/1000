package com.qthekan.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class qutil {

    /**
     * @param value : string value to want to change integer
     * @return : int value. if occurred exception non-int-string that will return defaultVal;
     */
    public static int parseInt(String value, int defaultVal)
    {
        try {
            return Integer.parseInt(value);
        }
        catch(NumberFormatException e) {
            return defaultVal;
        }
    }


    /**
     * @param context : activity context to show toast message
     * @param msg : contents to show toast message
     */
    public static void showToast(Context context, String msg)
    {
        Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }


    /**
     * @param unixTimeStamp : unix timestamp. not java timestame.
     * @return HH:mm
     */
    public static String unixtimeToHourMin(long unixTimeStamp)
    {
        long javaTimeStamp = unixTimeStamp * 1000;
        Date date = new Date(javaTimeStamp);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }


    /**
     * double 데이터에서 원하는 소수점 자리수만큼만 반환한다.
     */
    public static double getDouble(double value, int decimalPoint)
    {
        String point = "%." + decimalPoint + "f";
        return Double.parseDouble( String.format(point, value) );
    }


    /**
     * float 데이터에서 원하는 소수점 자리수만큼만 반환한다.
     */
    public static float getFloat(float value, int decimalPoint)
    {
        String point = "%." + decimalPoint + "f";
        return Float.parseFloat( String.format(point, value) );
    }


    public static void showDialog(Activity activity, String title, String message, DialogInterface.OnClickListener yes, DialogInterface.OnClickListener no)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(title).setMessage(message);

        if( yes == null && no == null )
        {
            dialog.setPositiveButton("ok", null);
        }

        if( yes != null )
        {
            dialog.setPositiveButton("yes", yes);
        }

        if( no != null )
        {
            dialog.setNegativeButton("no", no);
        }

        dialog.create().show();
    }


    /**
     * String 위경도를 LatLng 형으로 반환한다.
     * 37.1234,127.1234 => LatLng type
     */
    public static LatLng stringToLatlng(String latLng)
    {
        String latitude = latLng.split(",")[0].trim();
        String longitude = latLng.split(",")[1].trim();

        double lat = Double.valueOf(latitude);
        double lng = Double.valueOf(longitude);

        return new LatLng(lat, lng);
    }


    public static String intToStr(int i, String defaultStr)
    {
        String ret = defaultStr;

        try{
            ret = String.valueOf(i);
        }
        catch (Exception e)
        {
            qlog.e(e.getMessage());
        }
        return ret;
    }


    public static String floatToStr(float f, String defaultStr)
    {
        String ret = defaultStr;

        try{
            ret = String.valueOf(f);
        }
        catch (Exception e)
        {
            qlog.e(e.getMessage());
        }
        return ret;
    }


    /**
     *
     * @param fileName : ex) qherelog.txt
     * @param data : ex) 2019-01-01 ERROR contents....
     */
    public static void writeFile(String fileName, String data)
    {
        if( !isExternalStorageWritable() )
        {
            qlog.e("external storage not writable!!");
            return;
        }

        File f = new File( getSaveDir(), fileName);
        try {
            FileWriter w = new FileWriter(f, false);
            w.write(data);
            w.close();
        }
        catch (IOException e) {
            Log.e("", e.getMessage() );
        }

    }


    private static File getSaveDir()
    {
        File f = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "qHere");
        if( !f.mkdirs() )
        {
            qlog.e("mkdirs() fail: " + f.getAbsolutePath() );
        }

        return f;
    }


    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

}
