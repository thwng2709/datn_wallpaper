package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_pinLockView;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class Glszl_ItemSpaceDecoration extends RecyclerView.ItemDecoration {
    private final int heart_zipper_mHorizontalSpaceWidth;
    private final boolean heart_zipper_mIncludeEdge;
    private final int heart_zipper_mSpanCount;
    private final int heart_zipper_mVerticalSpaceHeight;

    public Glszl_ItemSpaceDecoration(int i, int i2, int i3, boolean z) {
        this.heart_zipper_mHorizontalSpaceWidth = i;
        this.heart_zipper_mVerticalSpaceHeight = i2;
        this.heart_zipper_mSpanCount = i3;
        this.heart_zipper_mIncludeEdge = z;
    }

    @Override 
    public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, RecyclerView.State state) {
        int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
        int i = this.heart_zipper_mSpanCount;
        int i2 = childAdapterPosition % i;
        if (this.heart_zipper_mIncludeEdge) {
            int i3 = this.heart_zipper_mHorizontalSpaceWidth;
            rect.left = i3 - ((i2 * i3) / i);
            rect.right = ((i2 + 1) * this.heart_zipper_mHorizontalSpaceWidth) / this.heart_zipper_mSpanCount;
            if (childAdapterPosition < this.heart_zipper_mSpanCount) {
                rect.top = this.heart_zipper_mVerticalSpaceHeight;
            }
            rect.bottom = this.heart_zipper_mVerticalSpaceHeight;
            return;
        }
        rect.left = (this.heart_zipper_mHorizontalSpaceWidth * i2) / i;
        int i4 = this.heart_zipper_mHorizontalSpaceWidth;
        rect.right = i4 - (((i2 + 1) * i4) / this.heart_zipper_mSpanCount);
        if (childAdapterPosition >= this.heart_zipper_mSpanCount) {
            rect.top = this.heart_zipper_mVerticalSpaceHeight;
        }
    }
}
