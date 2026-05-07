package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Media.Glszl_C0013Media;


public class Glszl_ULabel extends Glszl_Urect {
    String heart_zipper_Text;

    public String getText() {
        return this.heart_zipper_Text;
    }

    public void setText(String str) {
        this.heart_zipper_Text = str;
    }

    public Glszl_ULabel(double d, double d2, double d3, double d4, String str) {
        super(d, d2, d3, d4);
        this.heart_zipper_Text = str;
        setFont(Glszl_C0013Media.heart_zipper_font1);
        this.heart_zipper_paint.setTextAlign(Paint.Align.CENTER);
    }

    public void setTextAlign(Paint.Align align) {
        this.heart_zipper_paint.setTextAlign(align);
    }

    public void SetTextSize(double d) {
        this.heart_zipper_paint.setTextSize((float) d);
    }

    public double getTextSize() {
        return this.heart_zipper_paint.getTextSize();
    }

    public void setFont(Typeface typeface) {
        if (typeface != null) {
            this.heart_zipper_paint.setTypeface(typeface);
        }
    }

    public Typeface getFont() {
        return this.heart_zipper_paint.getTypeface();
    }

    @Override 
    public void Draw(Canvas canvas) {
        int save = canvas.save();
        canvas.skew((int) this.heart_zipper_skewX, (int) this.heart_zipper_skewY);
        canvas.rotate((int) getRotate(), (int) GetCenterX(), (int) getCenterY());
        this.heart_zipper_paint.setAlpha((int) getAlpha());
        canvas.drawText(getText(), (float) GetCenterX(), (float) (getCenterY() + (Height() / 5.0d)), this.heart_zipper_paint);
        canvas.restoreToCount(save);
        drawChildrens(canvas);
    }
}
