package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui;

import android.content.Context;

public class Glszl_PasswordAdapter {
    public static String Password = null;

    public static String LoadPassword(Context context) {
        if (Glszl_UserDataAdapter.LoadPref("PasswordSet", context) == 0) {
            Password = "";
        } else {
            Password = Glszl_UserDataAdapter.LoadPrefString("pass", context);
        }
        return Password;
    }

    public static void SavePassword(Context context, String str) {
        Glszl_UserDataAdapter.SavePref("PasswordSet", "1", context);
        Glszl_UserDataAdapter.SavePref("pass", str, context);
    }
}
