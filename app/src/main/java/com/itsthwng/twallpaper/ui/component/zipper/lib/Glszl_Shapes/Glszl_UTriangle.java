package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Glszl_UTriangle extends Glszl_Urect {

    public Glszl_UTriangle(double d, double d2, double d3, double d4, int i) {
        super(d, d2, d3, d4, i);
        this.heart_zipper_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.heart_zipper_paint.setStrokeWidth(4.0f);
    }

    @Override 
    public void Draw(Canvas canvas) {
        setRotate(getRotate() + 45.0d);
        super.Draw(canvas);
        setRotate(getRotate() - 45.0d);
    }
}
