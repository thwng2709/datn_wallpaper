package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Resource.Glszl_Resources;
import com.itsthwng.twallpaper.ui.component.zipper.lib.scripts.ImageSizeType;

public class Glszl_Uimage extends Glszl_Urect {
    protected Bitmap heart_zipper_image;
    public ImageSizeType heart_zipper_sizeType;

    public Bitmap getImage() {
        return this.heart_zipper_image;
    }

    public void setImage(Bitmap bitmap) {
        this.heart_zipper_image = bitmap;
    }

    public ImageSizeType getSizeType() {
        return this.heart_zipper_sizeType;
    }

    public Glszl_Uimage(double d, double d2, double d3, double d4, Bitmap bitmap) {
        super(d, d2, d3, d4);
        this.heart_zipper_sizeType = ImageSizeType.FitXY;
        this.heart_zipper_paint.setColor(0);
        this.heart_zipper_image = bitmap;
    }

    public Glszl_Uimage(Bitmap bitmap) {
        super(0.0d, 0.0d, bitmap.getWidth(), bitmap.getHeight());
        this.heart_zipper_sizeType = ImageSizeType.FitXY;
        this.heart_zipper_image = bitmap;
    }

    public Glszl_Uimage(double d, double d2, double d3, double d4, int i) {
        super(d, d2, d3, d4, i);
        this.heart_zipper_sizeType = ImageSizeType.FitXY;
    }

    @Override 
    public void Draw(Canvas canvas) {
        int save = canvas.save();
        canvas.rotate((int) getRotate(), (int) GetCenterX(), (int) getCenterY());
        this.heart_zipper_paint.setAlpha((int) getAlpha());
        canvas.skew((int) this.heart_zipper_skewX, (int) this.heart_zipper_skewY);
        if (this.heart_zipper_image.isRecycled()) {
            Log.i("image recycled", "image recycled");
            return;
        }
        int i = C00331.$SwitchMap$UgameLib$scripts$UimageSizeType[this.heart_zipper_sizeType.ordinal()];
        if (i == 1) {
            Bitmap bitmap = this.heart_zipper_image;
            canvas.drawBitmap(bitmap, Glszl_Resources.GetRectOfImage(bitmap), GetRect(), this.heart_zipper_paint);
        } else if (i == 2) {
            Rect GetRectOfImage = Glszl_Resources.GetRectOfImage(this.heart_zipper_image);
            new Rect();
            Rect GetRect = GetRect();
            canvas.drawBitmap(this.heart_zipper_image, new Rect(0, 0, GetRectOfImage.width(), GetRectOfImage.height() * (GetRect.height() / GetRect.width())), GetRect(), this.heart_zipper_paint);
        }
        canvas.restoreToCount(save);
        drawChildrens(canvas);
    }

    static  class C00331 {
        static final int[] $SwitchMap$UgameLib$scripts$UimageSizeType;

        static {
            int[] iArr = new int[ImageSizeType.values().length];
            $SwitchMap$UgameLib$scripts$UimageSizeType = iArr;
            try {
                iArr[ImageSizeType.FitXY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$UgameLib$scripts$UimageSizeType[ImageSizeType.FitX.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

}
