package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Resource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

public class Glszl_Resources {

    public static Rect GetRectOfImage(Bitmap bitmap) {
        return new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    public static Bitmap CreateBitmap(int i, Context context) {
        if (context == null) {
            Log.i("ctx null cls Resources", "context is null on class resource createRectofimage");
            return null;
        }
        try {
            return BitmapFactory.decodeResource(context.getResources(), i);
        } catch (Exception unused) {
            return null;
        }
    }

}
