package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_pinLockView;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;

public class Glszl_LTRGridLayoutManager extends GridLayoutManager {
    @Override 
    public boolean isLayoutRTL() {
        return false;
    }

    public Glszl_LTRGridLayoutManager(Context context, int i) {
        super(context, i);
    }

}
