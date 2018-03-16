package com.qthekan.qhere.talk;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;


@IgnoreExtraProperties
public class Message
{
    // millisec
    public long mTime;
    public String mName;
    public String mMsg;


    public Message()
    {

    }


    public Message(long time, String name, String msg)
    {
        mTime = time;
        mName = name;
        mMsg = msg;
    }


    public String getTime()
    {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        //String time = format.format(new Date(System.currentTimeMillis()));
        String time = format.format(new Date(mTime));

        return time;
    }


    public String toString()
    {
        String ret = String.format("[%s] [%s] %s\n", getTime(), mName, mMsg);
        return ret;
    }
}
