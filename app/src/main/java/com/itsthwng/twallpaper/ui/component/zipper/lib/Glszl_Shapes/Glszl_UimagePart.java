package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

public class Glszl_UimagePart extends Glszl_Uimage {
    public Glszl_Urect heart_zipper_ImageRect;
    public boolean heart_zipper_LogedImageRecycled;

    public Glszl_UimagePart(double d, double d2, double d3, double d4, Glszl_Urect urect, Bitmap bitmap) {
        super(d, d2, d3, d4, bitmap);
        this.heart_zipper_LogedImageRecycled = false;
        this.heart_zipper_ImageRect = urect;
    }

    @Override
    public void Draw(Canvas canvas) {
        int save = canvas.save();
        canvas.rotate((int) getRotate(), (int) GetCenterX(), (int) getCenterY());
        this.heart_zipper_paint.setAlpha((int) getAlpha());
        canvas.skew((int) this.heart_zipper_skewX, (int) this.heart_zipper_skewY);
        if (this.heart_zipper_image.isRecycled() && !this.heart_zipper_LogedImageRecycled) {
            this.heart_zipper_LogedImageRecycled = true;
            Log.i("img recycled UimagePart", "image recycled UimagePart");
        } else {
            canvas.drawBitmap(this.heart_zipper_image, this.heart_zipper_ImageRect.GetRect(), GetRect(), this.heart_zipper_paint);
        }
        canvas.restoreToCount(save);
        drawChildrens(canvas);
    }

}
