package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

public class Glszl_UserDataAdapter {
    public static String heart_zipper_DataFileName = "IDFU.data";
    public static SharedPreferences heart_zipper_Tpdata;
    public static boolean isPreview = false;
    public static void setIsPreview(boolean preview) {
        isPreview = preview;
    }
    public static boolean getIsPreview() {
        return isPreview;
    }

    public static int LoadPref(String str, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(heart_zipper_DataFileName, 0);
        heart_zipper_Tpdata = sharedPreferences;
        try {
            return Integer.parseInt(sharedPreferences.getString(str, "0"));
        } catch (Exception unused) {
            return 0;
        }
    }

    public static int LoadPrefDefault1(String str, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(heart_zipper_DataFileName, 0);
        heart_zipper_Tpdata = sharedPreferences;
        try {
            return Integer.parseInt(sharedPreferences.getString(str, "1"));
        } catch (Exception unused) {
            return 0;
        }
    }

    public static String LoadPrefString(String str, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(heart_zipper_DataFileName, 0);
        heart_zipper_Tpdata = sharedPreferences;
        try {
            return sharedPreferences.getString(str, "");
        } catch (Exception unused) {
            return "";
        }
    }

    public static void SavePref(final String str, final String str2, final Context context) {
        StrictMode.ThreadPolicy threadPolicy = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(threadPolicy).permitDiskWrites().build());
        new Thread() {

            public void run() {
                if (Glszl_UserDataAdapter.heart_zipper_Tpdata == null) {
                    Glszl_UserDataAdapter.heart_zipper_Tpdata = context.getSharedPreferences(Glszl_UserDataAdapter.heart_zipper_DataFileName, 0);
                }
                SharedPreferences.Editor edit = Glszl_UserDataAdapter.heart_zipper_Tpdata.edit();
                edit.putString(str, str2);
                edit.commit();
            }
        }.start();
        StrictMode.setThreadPolicy(threadPolicy);
    }

}
