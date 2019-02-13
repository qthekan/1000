package com.qthekan.util;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class qlog
{
    public static void e(String log)
    {
        Log.e( getClassName(), getLogMsg(log) );
    }


    public static void e(String log, Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        Log.e( getClassName(), getLogMsg(log), e );
        qutil.writeFile("qHere.txt", getLogMsg(log) + "\n" + sw.toString() );
    }


    public static void w(String log)
    {
        Log.w( getClassName(), getLogMsg(log) );
    }

    public static void i(String log)
    {
        Log.i( getClassName(), getLogMsg(log) );
    }

    public static void d(String log)
    {
        Log.d( getClassName(), getLogMsg(log) );
    }


    private static String getClassName()
    {
        StackTraceElement stack = Thread.currentThread().getStackTrace()[4];
        //String strClass = stack.getClassName();
        String strClass = stack.getFileName().replace(".java", "");

        return strClass;
    }


    private static String getLogMsg(String log)
    {
        StackTraceElement stack = Thread.currentThread().getStackTrace()[4];

        String strMethod = stack.getMethodName() + "()";
        String strLine = String.valueOf(stack.getLineNumber());

        String strContents = String.format("%s %s %s: %s", getCurrTime(), strMethod, strLine, log);
        return strContents;
    }


    private static String getCurrTime()
    {
        String now = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        return now;
    }
}
