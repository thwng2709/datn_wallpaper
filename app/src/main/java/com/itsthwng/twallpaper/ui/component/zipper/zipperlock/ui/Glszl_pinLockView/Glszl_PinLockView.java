package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_pinLockView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

import com.itsthwng.twallpaper.R;

public class Glszl_PinLockView extends RecyclerView {
    private static final int[] heart_zipper_DEFAULT_KEY_SET = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
    private Glszl_PinLockAdapter heart_zipper_mAdapter;
    private Drawable heart_zipper_mButtonBackgroundDrawable;
    private int heart_zipper_mButtonSize;
    private int[] heart_zipper_mCustomKeySet;
    private Glszl_CustomizationOptionsBundle heart_zipper_mCustomizationOptionsBundle;
    private Drawable heart_zipper_mDeleteButtonDrawable;
    private int heart_zipper_mDeleteButtonPressedColor;
    private int heart_zipper_mDeleteButtonSize;
    private int heart_zipper_mHorizontalSpacing;
    private Glszl_IndicatorDots heart_zipper_mIndicatorDots;
    private Glszl_PinLockAdapter.OnDeleteClickListener heart_zipper_mOnDeleteClickListener = new Glszl_PinLockAdapter.OnDeleteClickListener() {

        @Override 
        public void onDeleteClicked() {
            if (Glszl_PinLockView.this.mPin.length() > 0) {
                Glszl_PinLockView pinLockView2 = Glszl_PinLockView.this;
                pinLockView2.mPin = pinLockView2.mPin.substring(0, Glszl_PinLockView.this.mPin.length() - 1);
                if (Glszl_PinLockView.this.isIndicatorDotsAttached()) {
                    Glszl_PinLockView.this.heart_zipper_mIndicatorDots.updateDot(Glszl_PinLockView.this.mPin.length());
                }
                if (Glszl_PinLockView.this.mPin.length() == 0) {
                    Glszl_PinLockView.this.heart_zipper_mAdapter.setPinLength(Glszl_PinLockView.this.mPin.length());
                    Glszl_PinLockView.this.heart_zipper_mAdapter.notifyItemChanged(Glszl_PinLockView.this.heart_zipper_mAdapter.getItemCount() - 1);
                }
                if (Glszl_PinLockView.this.mPinLockListener == null) {
                    return;
                }
                if (Glszl_PinLockView.this.mPin.length() == 0) {
                    Glszl_PinLockView.this.mPinLockListener.onEmpty();
                    Glszl_PinLockView.this.clearInternalPin();
                    return;
                }
                Glszl_PinLockView.this.mPinLockListener.onPinChange(Glszl_PinLockView.this.mPin.length(), Glszl_PinLockView.this.mPin);
            } else if (Glszl_PinLockView.this.mPinLockListener != null) {
                Glszl_PinLockView.this.mPinLockListener.onEmpty();
            }
        }

        @Override 
        public void onDeleteLongClicked() {
            Glszl_PinLockView.this.resetPinLockView();
            if (Glszl_PinLockView.this.mPinLockListener != null) {
                Glszl_PinLockView.this.mPinLockListener.onEmpty();
            }
        }
    };
    private Glszl_PinLockAdapter.OnNumberClickListener mOnNumberClickListener = new Glszl_PinLockAdapter.OnNumberClickListener() {

        @Override 
        public void onNumberClicked(int i) {
            if (Glszl_PinLockView.this.mPin.length() < Glszl_PinLockView.this.getPinLength()) {
                Glszl_PinLockView pinLockView2 = Glszl_PinLockView.this;
                pinLockView2.mPin = pinLockView2.mPin.concat(String.valueOf(i));
                if (Glszl_PinLockView.this.isIndicatorDotsAttached()) {
                    Glszl_PinLockView.this.heart_zipper_mIndicatorDots.updateDot(Glszl_PinLockView.this.mPin.length());
                }
                if (Glszl_PinLockView.this.mPin.length() == 1) {
                    Glszl_PinLockView.this.heart_zipper_mAdapter.setPinLength(Glszl_PinLockView.this.mPin.length());
                    Glszl_PinLockView.this.heart_zipper_mAdapter.notifyItemChanged(Glszl_PinLockView.this.heart_zipper_mAdapter.getItemCount() - 1);
                }
                if (Glszl_PinLockView.this.mPinLockListener == null) {
                    return;
                }
                if (Glszl_PinLockView.this.mPin.length() == Glszl_PinLockView.this.mPinLength) {
                    Glszl_PinLockView.this.mPinLockListener.onComplete(Glszl_PinLockView.this.mPin);
                } else {
                    Glszl_PinLockView.this.mPinLockListener.onPinChange(Glszl_PinLockView.this.mPin.length(), Glszl_PinLockView.this.mPin);
                }
            } else if (!Glszl_PinLockView.this.isShowDeleteButton()) {
                Glszl_PinLockView.this.resetPinLockView();
                Glszl_PinLockView pinLockView3 = Glszl_PinLockView.this;
                pinLockView3.mPin = pinLockView3.mPin.concat(String.valueOf(i));
                if (Glszl_PinLockView.this.isIndicatorDotsAttached()) {
                    Glszl_PinLockView.this.heart_zipper_mIndicatorDots.updateDot(Glszl_PinLockView.this.mPin.length());
                }
                if (Glszl_PinLockView.this.mPinLockListener != null) {
                    Glszl_PinLockView.this.mPinLockListener.onPinChange(Glszl_PinLockView.this.mPin.length(), Glszl_PinLockView.this.mPin);
                }
            } else if (Glszl_PinLockView.this.mPinLockListener != null) {
                Glszl_PinLockView.this.mPinLockListener.onComplete(Glszl_PinLockView.this.mPin);
            }
        }
    };
    private String mPin = "";
    private int mPinLength;
    private PinLockListener mPinLockListener;
    private boolean mShowDeleteButton;
    private int mTextColor;
    private int mTextSize;
    private int mVerticalSpacing;

    public void updateDots() {
        if (isIndicatorDotsAttached()) {
            this.heart_zipper_mIndicatorDots.updateDot(this.mPin.length());
        }
    }

    public void clearPin() {
        if (this.mPin.length() > 0) {
            String str = this.mPin;
            this.mPin = str.substring(0, str.length() - 1);
            if (isIndicatorDotsAttached()) {
                this.heart_zipper_mIndicatorDots.updateDot(this.mPin.length());
            }
            if (this.mPin.length() == 0) {
                this.heart_zipper_mAdapter.setPinLength(this.mPin.length());
                Glszl_PinLockAdapter pinLockAdapter = this.heart_zipper_mAdapter;
                pinLockAdapter.notifyItemChanged(pinLockAdapter.getItemCount() - 1);
            }
            if (this.mPinLockListener == null) {
                return;
            }
            if (this.mPin.length() == 0) {
                this.mPinLockListener.onEmpty();
                clearInternalPin();
                return;
            }
            this.mPinLockListener.onPinChange(this.mPin.length(), this.mPin);
            return;
        }
        PinLockListener pinLockListener = this.mPinLockListener;
        if (pinLockListener != null) {
            pinLockListener.onEmpty();
        }
    }

    public Glszl_PinLockView(Context context) {
        super(context);
        init(null, 0);
    }

    public Glszl_PinLockView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(attributeSet, 0);
    }

    public Glszl_PinLockView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(attributeSet, i);
    }

    private void init(AttributeSet attributeSet, int i) {
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, new int[]{R.attr.dotDiameter, R.attr.dotEmptyBackground, R.attr.dotFilledBackground, R.attr.dotSpacing, R.attr.indicatorType, R.attr.keypadButtonBackgroundDrawable, R.attr.keypadButtonSize, R.attr.keypadDeleteButtonDrawable, R.attr.keypadDeleteButtonPressedColor, R.attr.keypadDeleteButtonSize, R.attr.keypadHorizontalSpacing, R.attr.keypadShowDeleteButton, R.attr.keypadTextColor, R.attr.keypadTextSize, R.attr.keypadVerticalSpacing, R.attr.pinLength});
        try {
            this.mPinLength = obtainStyledAttributes.getInt(15, 4);
            this.heart_zipper_mHorizontalSpacing = (int) obtainStyledAttributes.getDimension(10, ResourceUtils.getDimensionInPx(getContext(), R.dimen.common_32dp));
            this.mVerticalSpacing = (int) obtainStyledAttributes.getDimension(14, ResourceUtils.getDimensionInPx(getContext(), R.dimen.common_0dp));
            this.mTextColor = obtainStyledAttributes.getColor(12, ResourceUtils.getColor(getContext(), R.color.white));
            this.mTextSize = (int) obtainStyledAttributes.getDimension(13, ResourceUtils.getDimensionInPx(getContext(), R.dimen.text_size_16sp));
            this.heart_zipper_mButtonSize = (int) obtainStyledAttributes.getDimension(6, ResourceUtils.getDimensionInPx(getContext(), R.dimen.common_60dp));
            this.heart_zipper_mDeleteButtonSize = (int) obtainStyledAttributes.getDimension(9, ResourceUtils.getDimensionInPx(getContext(), R.dimen.common_16dp));
            this.heart_zipper_mButtonBackgroundDrawable = obtainStyledAttributes.getDrawable(5);
            this.heart_zipper_mDeleteButtonDrawable = obtainStyledAttributes.getDrawable(7);
            this.mShowDeleteButton = obtainStyledAttributes.getBoolean(11, true);
            this.heart_zipper_mDeleteButtonPressedColor = obtainStyledAttributes.getColor(8, ResourceUtils.getColor(getContext(), R.color.greyish));
            obtainStyledAttributes.recycle();
            Glszl_CustomizationOptionsBundle customizationOptionsBundle = new Glszl_CustomizationOptionsBundle();
            this.heart_zipper_mCustomizationOptionsBundle = customizationOptionsBundle;
            customizationOptionsBundle.setTextColor(this.mTextColor);
            this.heart_zipper_mCustomizationOptionsBundle.setTextSize(this.mTextSize);
            this.heart_zipper_mCustomizationOptionsBundle.setButtonSize(this.heart_zipper_mButtonSize);
            this.heart_zipper_mCustomizationOptionsBundle.setButtonBackgroundDrawable(this.heart_zipper_mButtonBackgroundDrawable);
            this.heart_zipper_mCustomizationOptionsBundle.setDeleteButtonDrawable(this.heart_zipper_mDeleteButtonDrawable);
            this.heart_zipper_mCustomizationOptionsBundle.setDeleteButtonSize(this.heart_zipper_mDeleteButtonSize);
            this.heart_zipper_mCustomizationOptionsBundle.setShowDeleteButton(this.mShowDeleteButton);
            this.heart_zipper_mCustomizationOptionsBundle.setDeleteButtonPressesColor(this.heart_zipper_mDeleteButtonPressedColor);
            initView();
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    private void initView() {
        setLayoutManager(new Glszl_LTRGridLayoutManager(getContext(), 3));
        Glszl_PinLockAdapter pinLockAdapter = new Glszl_PinLockAdapter(getContext());
        this.heart_zipper_mAdapter = pinLockAdapter;
        pinLockAdapter.setOnItemClickListener(this.mOnNumberClickListener);
        this.heart_zipper_mAdapter.setOnDeleteClickListener(this.heart_zipper_mOnDeleteClickListener);
        this.heart_zipper_mAdapter.setCustomizationOptions(this.heart_zipper_mCustomizationOptionsBundle);
        setAdapter(this.heart_zipper_mAdapter);
        addItemDecoration(new Glszl_ItemSpaceDecoration(this.heart_zipper_mHorizontalSpacing, this.mVerticalSpacing, 3, false));
        setOverScrollMode(2);
    }

    public void setPinLockListener(PinLockListener pinLockListener) {
        this.mPinLockListener = pinLockListener;
    }

    public int getPinLength() {
        return this.mPinLength;
    }

    public void setPinLength(int i) {
        this.mPinLength = i;
        if (isIndicatorDotsAttached()) {
            this.heart_zipper_mIndicatorDots.setPinLength(i);
        }
    }

    public int getTextColor() {
        return this.mTextColor;
    }

    public void setTextColor(int i) {
        this.mTextColor = i;
        this.heart_zipper_mCustomizationOptionsBundle.setTextColor(i);
        this.heart_zipper_mAdapter.notifyDataSetChanged();
    }

    public int getTextSize() {
        return this.mTextSize;
    }

    public void setTextSize(int i) {
        this.mTextSize = i;
        this.heart_zipper_mCustomizationOptionsBundle.setTextSize(i);
        this.heart_zipper_mAdapter.notifyDataSetChanged();
    }

    public int getButtonSize() {
        return this.heart_zipper_mButtonSize;
    }

    public void setButtonSize(int i) {
        this.heart_zipper_mButtonSize = i;
        this.heart_zipper_mCustomizationOptionsBundle.setButtonSize(i);
        this.heart_zipper_mAdapter.notifyDataSetChanged();
    }

    public Drawable getButtonBackgroundDrawable() {
        return this.heart_zipper_mButtonBackgroundDrawable;
    }

    public void setButtonBackgroundDrawable(Drawable drawable) {
        this.heart_zipper_mButtonBackgroundDrawable = drawable;
        this.heart_zipper_mCustomizationOptionsBundle.setButtonBackgroundDrawable(drawable);
        this.heart_zipper_mAdapter.notifyDataSetChanged();
    }

    public Drawable getDeleteButtonDrawable() {
        return this.heart_zipper_mDeleteButtonDrawable;
    }

    public void setDeleteButtonDrawable(Drawable drawable) {
        this.heart_zipper_mDeleteButtonDrawable = drawable;
        this.heart_zipper_mCustomizationOptionsBundle.setDeleteButtonDrawable(drawable);
        this.heart_zipper_mAdapter.notifyDataSetChanged();
    }

    public int getDeleteButtonSize() {
        return this.heart_zipper_mDeleteButtonSize;
    }

    public void setDeleteButtonSize(int i) {
        this.heart_zipper_mDeleteButtonSize = i;
        this.heart_zipper_mCustomizationOptionsBundle.setDeleteButtonSize(i);
        this.heart_zipper_mAdapter.notifyDataSetChanged();
    }

    public boolean isShowDeleteButton() {
        return this.mShowDeleteButton;
    }

    public void setShowDeleteButton(boolean z) {
        this.mShowDeleteButton = z;
        this.heart_zipper_mCustomizationOptionsBundle.setShowDeleteButton(z);
        this.heart_zipper_mAdapter.notifyDataSetChanged();
    }

    public int getDeleteButtonPressedColor() {
        return this.heart_zipper_mDeleteButtonPressedColor;
    }

    public void setDeleteButtonPressedColor(int i) {
        this.heart_zipper_mDeleteButtonPressedColor = i;
        this.heart_zipper_mCustomizationOptionsBundle.setDeleteButtonPressesColor(i);
        this.heart_zipper_mAdapter.notifyDataSetChanged();
    }

    public int[] getCustomKeySet() {
        return this.heart_zipper_mCustomKeySet;
    }

    public void setCustomKeySet(int[] iArr) {
        this.heart_zipper_mCustomKeySet = iArr;
        Glszl_PinLockAdapter pinLockAdapter = this.heart_zipper_mAdapter;
        if (pinLockAdapter != null) {
            pinLockAdapter.setKeyValues(iArr);
        }
    }

    public void enableLayoutShuffling() {
        int[] shuffle = Glszl_ShuffleArrayUtils.shuffle(heart_zipper_DEFAULT_KEY_SET);
        this.heart_zipper_mCustomKeySet = shuffle;
        Glszl_PinLockAdapter pinLockAdapter = this.heart_zipper_mAdapter;
        if (pinLockAdapter != null) {
            pinLockAdapter.setKeyValues(shuffle);
        }
    }

    private void clearInternalPin() {
        this.mPin = "";
    }

    public void resetPinLockView() {
        clearInternalPin();
        this.heart_zipper_mAdapter.setPinLength(this.mPin.length());
        Glszl_PinLockAdapter pinLockAdapter = this.heart_zipper_mAdapter;
        pinLockAdapter.notifyItemChanged(pinLockAdapter.getItemCount() - 1);
        Glszl_IndicatorDots indicatorDots = this.heart_zipper_mIndicatorDots;
        if (indicatorDots != null) {
            indicatorDots.updateDot(this.mPin.length());
        }
    }

    public boolean isIndicatorDotsAttached() {
        return this.heart_zipper_mIndicatorDots != null;
    }

    public void attachIndicatorDots(Glszl_IndicatorDots indicatorDots) {
        this.heart_zipper_mIndicatorDots = indicatorDots;
    }
}
