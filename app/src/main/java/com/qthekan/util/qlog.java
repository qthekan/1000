package com.qthekan.util;


import android.util.Log;

public class qlog
{
    public static void e(String log)
    {
        Log.e("", getLogMsg(log) );
    }

    public static void w(String log)
    {
        Log.w("", getLogMsg(log) );
    }

    public static void i(String log)
    {
        Log.i("", getLogMsg(log) );
    }

    private static String getLogMsg(String log)
    {
        StackTraceElement stack = Thread.currentThread().getStackTrace()[4];
        //String strClass = stack.getClassName();
        String strClass = stack.getFileName().replace(".java", "");
        String strMethod = stack.getMethodName();
        String strLine = String.valueOf(stack.getLineNumber());

        String strContents = String.format("%s %s %s: %s", strClass, strMethod, strLine, log);
        return strContents;
    }
}
