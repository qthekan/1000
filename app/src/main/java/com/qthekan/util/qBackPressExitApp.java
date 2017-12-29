package com.qthekan.util;


import android.app.Activity;
import android.widget.Toast;

public class qBackPressExitApp {
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;


    public qBackPressExitApp(Activity context) {
        this.activity = context;
    }


    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.finish();
            toast.cancel();
        }
    }


    private void showGuide() {
        toast = Toast.makeText(activity, "Press BACK button to Exit App", Toast.LENGTH_SHORT);
        toast.show();
    }

}
