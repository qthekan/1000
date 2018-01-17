package com.qthekan.util;


import android.util.Log;

public class qlog
{
    public static void e(String log)
    {
        Log.e( getClassName(), getLogMsg(log) );
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

        String strContents = String.format("%s %s: %s", strMethod, strLine, log);
        return strContents;
    }
}
