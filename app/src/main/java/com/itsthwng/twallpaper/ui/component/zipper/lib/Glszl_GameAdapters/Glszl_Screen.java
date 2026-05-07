package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowMetrics;


public class Glszl_Screen {
    public static double Height;
    public static double Width;

    @SuppressLint("WrongConstant")
    public static void Inicialize() {
        WindowManager wm = (WindowManager) Glszl_GameAdapter.ctx.getSystemService(Context.WINDOW_SERVICE);
        // ===== LẤY KÍCH THƯỚC FULL SCREEN =====

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) Glszl_GameAdapter.ctx.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        if (Build.VERSION.SDK_INT >= 30) {
            // bounds đã là full; KHÔNG trừ system bars
            final WindowMetrics metrics = wm.getCurrentWindowMetrics();
            final Rect b = metrics.getBounds();
            Width  = b.width();
            Height = b.height();
        } else {
            // getRealMetrics => full; KHÔNG dùng getSize
            Display display = wm.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getRealMetrics(dm);
            Width  = dm.widthPixels;
            Height = dm.heightPixels;
        }
    }

}
