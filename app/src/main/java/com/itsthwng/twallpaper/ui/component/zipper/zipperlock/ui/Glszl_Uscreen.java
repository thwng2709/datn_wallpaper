package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class Glszl_Uscreen {
    public static int heart_zipper_height;
    public static int heart_zipper_width;

    public static void Init(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        activity.getWindow().getDecorView().setSystemUiVisibility(2);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getMetrics(displayMetrics);
        heart_zipper_width = displayMetrics.widthPixels;
        heart_zipper_height = displayMetrics.heightPixels;
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 27) {
            try {
                heart_zipper_width = ((Integer) Display.class.getMethod("getRawWidth", new Class[0]).invoke(defaultDisplay, new Object[0])).intValue();
                heart_zipper_height = ((Integer) Display.class.getMethod("getRawHeight", new Class[0]).invoke(defaultDisplay, new Object[0])).intValue();
            } catch (Exception unused) {
            }
        }
        if (Build.VERSION.SDK_INT >= 27) {
            try {
                Point point = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(defaultDisplay, point);
                heart_zipper_width = point.x;
                heart_zipper_height = point.y;
            } catch (Exception unused2) {
            }
        }
    }
}
