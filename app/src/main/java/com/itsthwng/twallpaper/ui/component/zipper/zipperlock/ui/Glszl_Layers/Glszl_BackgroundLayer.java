package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_GameAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_Screen;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Uimage;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_LockScreenService;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Media.Glszl_C0013Media;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_servutils.Glszl_utils.Glszl_LockscreenService;


public class Glszl_BackgroundLayer {

    public static Glszl_Uimage heart_zipper_f0bg = null;
    public static boolean heart_zipper_notNugat = true;

    public static void Inicial() {
        if (Build.VERSION.SDK_INT < 26) {
            heart_zipper_notNugat = true;
        }
        double d = Glszl_Screen.Width;
        double d2 = Glszl_Screen.Height;
        if (heart_zipper_notNugat) {
            if (Glszl_LockScreenService.f10546cc.getApplicationContext().getResources().getConfiguration().orientation == 2) {
                d = Glszl_Screen.Height;
                d2 = Glszl_Screen.Width;
            }
        } else if (Glszl_LockscreenService.heart_zipper_instance.getApplicationContext().getResources().getConfiguration().orientation == 2) {
            d = Glszl_Screen.Height;
            d2 = Glszl_Screen.Width;
        }
        Log.e("Inicial", "Inicial: background");
        if(Glszl_C0013Media.heart_zipper_SelectedBackBg != null){
            Bitmap rawBitmap = Glszl_C0013Media.scaleCenterCrop(Glszl_C0013Media.heart_zipper_SelectedBackBg, (int) d2, (int) d);
            heart_zipper_f0bg = new Glszl_Uimage(0.0d, 0.0d, d, d2, rawBitmap);
            Glszl_GameAdapter.GetMainRect().AddChild(heart_zipper_f0bg);
        }
    }
}
