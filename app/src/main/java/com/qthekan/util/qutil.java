package com.qthekan.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class qutil {

    /**
     * @param value : string value to want to change integer
     * @return : int value. if occurred exception non-int-string that will return 0;
     */
    public static int parseInt(String value)
    {
        try {
            return Integer.parseInt(value);
        }
        catch(NumberFormatException e) {
            return 0;
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
     * @return
     */
    public static String unixtimeToHourMin(long unixTimeStamp)
    {
        long javaTimeStamp = unixTimeStamp * 1000;
        Date date = new Date(javaTimeStamp);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String hourMin = format.format(date);
        return hourMin;
    }
}
