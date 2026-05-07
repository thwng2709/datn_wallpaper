package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_pinLockView;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.view.ViewCompat;

import com.itsthwng.twallpaper.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("All")
public class Glszl_IndicatorDots extends LinearLayout {
    private static final int heart_zipper_DEFAULT_PIN_LENGTH = 4;
    private int heart_zipper_mDotDiameter;
    private int heart_zipper_mDotSpacing;
    private int heart_zipper_mEmptyDrawable;
    private int heart_zipper_mFillDrawable;
    private int heart_zipper_mIndicatorType;
    private int heart_zipper_mPinLength;
    private int heart_zipper_mPreviousLength;

    @Retention(RetentionPolicy.SOURCE)
    public @interface IndicatorType {
        public static final int FILL = 1;
        public static final int FILL_WITH_ANIMATION = 2;
        public static final int FIXED = 0;
    }

    public Glszl_IndicatorDots(Context context) {
        this(context, null);
    }

    public Glszl_IndicatorDots(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public Glszl_IndicatorDots(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, new int[]{R.attr.dotDiameter, R.attr.dotEmptyBackground, R.attr.dotFilledBackground, R.attr.dotSpacing, R.attr.indicatorType, R.attr.keypadButtonBackgroundDrawable, R.attr.keypadButtonSize, R.attr.keypadDeleteButtonDrawable, R.attr.keypadDeleteButtonPressedColor, R.attr.keypadDeleteButtonSize, R.attr.keypadHorizontalSpacing, R.attr.keypadShowDeleteButton, R.attr.keypadTextColor, R.attr.keypadTextSize, R.attr.keypadVerticalSpacing, R.attr.pinLength});
        try {
            this.heart_zipper_mDotDiameter = (int) obtainStyledAttributes.getDimension(0, ResourceUtils.getDimensionInPx(getContext(), R.dimen.common_8dp));
            this.heart_zipper_mDotSpacing = (int) obtainStyledAttributes.getDimension(3, ResourceUtils.getDimensionInPx(getContext(), R.dimen.common_8dp));
            this.heart_zipper_mFillDrawable = obtainStyledAttributes.getResourceId(2, R.drawable.dot_filled);
            this.heart_zipper_mEmptyDrawable = obtainStyledAttributes.getResourceId(1, R.drawable.dot_empty);
            this.heart_zipper_mPinLength = obtainStyledAttributes.getInt(15, 4);
            this.heart_zipper_mIndicatorType = obtainStyledAttributes.getInt(4, 0);
            obtainStyledAttributes.recycle();
            initView(context);
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    private void initView(Context context) {
        ViewCompat.setLayoutDirection(this, 0);
        int i = this.heart_zipper_mIndicatorType;
        if (i == 0) {
            for (int i2 = 0; i2 < this.heart_zipper_mPinLength; i2++) {
                View view = new View(context);
                emptyDot(view);
                int i3 = this.heart_zipper_mDotDiameter;
                LayoutParams layoutParams = new LayoutParams(i3, i3);
                int i4 = this.heart_zipper_mDotSpacing;
                layoutParams.setMargins(i4, 0, i4, 0);
                view.setLayoutParams(layoutParams);
                addView(view);
            }
        } else if (i == 2) {
            setLayoutTransition(new LayoutTransition());
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.heart_zipper_mIndicatorType != 0) {
            getLayoutParams().height = this.heart_zipper_mDotDiameter;
            requestLayout();
        }
    }

    public void updateDot(int i) {
        if (this.heart_zipper_mIndicatorType == 0) {
            if (i > 0) {
                if (i > this.heart_zipper_mPreviousLength) {
                    fillDot(getChildAt(i - 1));
                } else {
                    emptyDot(getChildAt(i));
                }
                this.heart_zipper_mPreviousLength = i;
                return;
            }
            for (int i2 = 0; i2 < getChildCount(); i2++) {
                emptyDot(getChildAt(i2));
            }
            this.heart_zipper_mPreviousLength = 0;
        } else if (i > 0) {
            if (i > this.heart_zipper_mPreviousLength) {
                View view = new View(getContext());
                fillDot(view);
                int i3 = this.heart_zipper_mDotDiameter;
                LayoutParams layoutParams = new LayoutParams(i3, i3);
                int i4 = this.heart_zipper_mDotSpacing;
                layoutParams.setMargins(i4, 0, i4, 0);
                view.setLayoutParams(layoutParams);
                addView(view, i - 1);
            } else {
                removeViewAt(i);
            }
            this.heart_zipper_mPreviousLength = i;
        } else {
            removeAllViews();
            this.heart_zipper_mPreviousLength = 0;
        }
    }

    private void emptyDot(View view) {
        view.setBackgroundResource(this.heart_zipper_mEmptyDrawable);
    }

    private void fillDot(View view) {
        view.setBackgroundResource(this.heart_zipper_mFillDrawable);
    }

    public int getPinLength() {
        return this.heart_zipper_mPinLength;
    }

    public void setPinLength(int i) {
        this.heart_zipper_mPinLength = i;
        removeAllViews();
        initView(getContext());
    }

    public int getIndicatorType() {
        return this.heart_zipper_mIndicatorType;
    }

    public void setIndicatorType(int i) {
        this.heart_zipper_mIndicatorType = i;
        removeAllViews();
        initView(getContext());
    }
}
