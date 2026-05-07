package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui;

import android.content.Context;

public class Glszl_CheckBoxUpdater {
    public static boolean toggleAndPersistState(boolean currentState, String prefKey, Context context, boolean shouldPersist) {
        boolean nextState = !currentState;
        if (shouldPersist) {
            Glszl_UserDataAdapter.SavePref(prefKey, nextState ? "1" : "0", context);
        }
        return nextState;
    }

    public static boolean isStateEnabled(String prefKey, Context context) {
        return Glszl_UserDataAdapter.LoadPref(prefKey, context) != 0;
    }
}
