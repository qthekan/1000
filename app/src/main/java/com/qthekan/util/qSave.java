package com.qthekan.util;

import android.content.SharedPreferences;
import android.os.Environment;

import com.qthekan.qhere.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class qSave
{
    private String mDirName = "qhere";


    public void save(String filename, String data)
    {
        if( isExternalStorageWritable() == false )
        {
            qlog.e("external storage not writable!!");
            return;
        }

        File f = new File( getSaveDir(), filename);
        try {
            FileWriter w = new FileWriter(f, false);
            w.write(data);
            w.close();
            MainActivity.getIns().showToast("save file:\n" + f.getAbsolutePath() );
            qlog.e("save success: " + f.getAbsolutePath() );
        }
        catch (IOException e) {
            qlog.e("file write fail: " + e.getMessage() );
        }
    }


    public String load(String filename)
    {
        if( isExternalStorageReadable() == false )
        {
            qlog.e("external storage not readable!!");
            return null;
        }

        File f = new File( getSaveDir(), filename);
        if( f.exists() == false )
        {
            qlog.e("save file not exists: " + f.getAbsolutePath() );
            return null;
        }

        String strData = "";
        try {
            BufferedReader r = new BufferedReader(new FileReader(f));
            String line;
            while( (line = r.readLine()) != null )
            {
                strData += line;
            }
            qlog.e("strData: " + strData);
            r.close();
        }
        catch (Exception e) {
            qlog.e("read fail: " + e.getMessage() );
            return null;
        }

        return strData;
    }


    public File getSaveDir()
    {
        File f = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), mDirName);
        if( !f.mkdirs() )
        {
            qlog.e("mkdirs() fail: " + f.getAbsolutePath() );
        }

        return f;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
