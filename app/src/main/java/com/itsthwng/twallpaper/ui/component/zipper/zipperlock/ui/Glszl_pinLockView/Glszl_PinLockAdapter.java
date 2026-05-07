package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_pinLockView;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.itsthwng.twallpaper.R;

public class Glszl_PinLockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context mContext;
    private Glszl_CustomizationOptionsBundle heart_zipper_mCustomizationOptionsBundle;
    private int[] mKeyValues = getAdjustKeyValues(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
    private OnDeleteClickListener heart_zipper_mOnDeleteClickListener;
    private OnNumberClickListener heart_zipper_mOnNumberClickListener;
    private int mPinLength;

    public interface OnDeleteClickListener {
        void onDeleteClicked();

        void onDeleteLongClicked();
    }

    public interface OnNumberClickListener {
        void onNumberClicked(int i);
    }

    @Override 
    public int getItemCount() {
        return 12;
    }

    public Glszl_PinLockAdapter(Context context) {
        this.mContext = context;
    }

    @Override 
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater from = LayoutInflater.from(viewGroup.getContext());
        if (i == 0) {
            return new NumberViewHolder(from.inflate(R.layout.layout_number_item, viewGroup, false));
        }
        return new DeleteViewHolder(from.inflate(R.layout.layout_delete_item, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder.getItemViewType() == 0) {
            configureNumberButtonHolder((NumberViewHolder) viewHolder, i);
        } else if (viewHolder.getItemViewType() == 1) {
            configureDeleteButtonHolder((DeleteViewHolder) viewHolder);
        }
    }

    private void configureNumberButtonHolder(NumberViewHolder numberViewHolder, int i) {
        if (numberViewHolder != null) {
            if (i == 9) {
                numberViewHolder.mNumberButton.setVisibility(View.GONE);
            } else {
                numberViewHolder.mNumberButton.setText(String.valueOf(this.mKeyValues[i]));
                numberViewHolder.mNumberButton.setVisibility(View.VISIBLE);
                numberViewHolder.mNumberButton.setTag(Integer.valueOf(this.mKeyValues[i]));
            }
            if (this.heart_zipper_mCustomizationOptionsBundle != null) {
                numberViewHolder.mNumberButton.setTextColor(this.heart_zipper_mCustomizationOptionsBundle.getTextColor());
                if (this.heart_zipper_mCustomizationOptionsBundle.getButtonBackgroundDrawable() != null) {
                    if (Build.VERSION.SDK_INT < 16) {
                        numberViewHolder.mNumberButton.setBackgroundDrawable(this.heart_zipper_mCustomizationOptionsBundle.getButtonBackgroundDrawable());
                    } else {
                        numberViewHolder.mNumberButton.setBackground(this.heart_zipper_mCustomizationOptionsBundle.getButtonBackgroundDrawable());
                    }
                }
                numberViewHolder.mNumberButton.setTextSize(0, (float) this.heart_zipper_mCustomizationOptionsBundle.getTextSize());
                numberViewHolder.mNumberButton.setLayoutParams(new LinearLayout.LayoutParams(this.heart_zipper_mCustomizationOptionsBundle.getButtonSize(), this.heart_zipper_mCustomizationOptionsBundle.getButtonSize()));
            }
        }
    }

    private void configureDeleteButtonHolder(DeleteViewHolder deleteViewHolder) {
        if (deleteViewHolder == null) {
            return;
        }
        if (!this.heart_zipper_mCustomizationOptionsBundle.isShowDeleteButton() || this.mPinLength <= 0) {
            deleteViewHolder.mButtonImage.setVisibility(View.GONE);
            return;
        }
        deleteViewHolder.mButtonImage.setVisibility(View.VISIBLE);
        if (this.heart_zipper_mCustomizationOptionsBundle.getDeleteButtonDrawable() != null) {
            deleteViewHolder.mButtonImage.setImageDrawable(this.heart_zipper_mCustomizationOptionsBundle.getDeleteButtonDrawable());
        }
        deleteViewHolder.mButtonImage.setColorFilter(this.heart_zipper_mCustomizationOptionsBundle.getTextColor(), PorterDuff.Mode.SRC_ATOP);
        deleteViewHolder.mButtonImage.setLayoutParams(new LinearLayout.LayoutParams(this.heart_zipper_mCustomizationOptionsBundle.getDeleteButtonSize(), this.heart_zipper_mCustomizationOptionsBundle.getDeleteButtonSize()));
    }

    @Override 
    public int getItemViewType(int i) {
        return i == getItemCount() - 1 ? 1 : 0;
    }

    public void setPinLength(int i) {
        this.mPinLength = i;
    }

    public void setKeyValues(int[] iArr) {
        this.mKeyValues = getAdjustKeyValues(iArr);
        notifyDataSetChanged();
    }

    private int[] getAdjustKeyValues(int[] iArr) {
        int[] iArr2 = new int[(iArr.length + 1)];
        for (int i = 0; i < iArr.length; i++) {
            if (i < 9) {
                iArr2[i] = iArr[i];
            } else {
                iArr2[i] = -1;
                iArr2[i + 1] = iArr[i];
            }
        }
        return iArr2;
    }

    public OnNumberClickListener getOnItemClickListener() {
        return this.heart_zipper_mOnNumberClickListener;
    }

    public void setOnItemClickListener(OnNumberClickListener onNumberClickListener) {
        this.heart_zipper_mOnNumberClickListener = onNumberClickListener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
        this.heart_zipper_mOnDeleteClickListener = onDeleteClickListener;
    }

    public void setCustomizationOptions(Glszl_CustomizationOptionsBundle customizationOptionsBundle) {
        this.heart_zipper_mCustomizationOptionsBundle = customizationOptionsBundle;
    }

    public class NumberViewHolder extends RecyclerView.ViewHolder {
        Button mNumberButton;

        public NumberViewHolder(View view) {
            super(view);
            Button button = (Button) view.findViewById(R.id.button);
            this.mNumberButton = button;
            button.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    if (Glszl_PinLockAdapter.this.heart_zipper_mOnNumberClickListener != null) {
                        Glszl_PinLockAdapter.this.heart_zipper_mOnNumberClickListener.onNumberClicked(((Integer) view.getTag()).intValue());
                    }
                }
            });
        }
    }

    public class DeleteViewHolder extends RecyclerView.ViewHolder {
        ImageView mButtonImage;
        LinearLayout mDeleteButton;

        public DeleteViewHolder(View view) {
            super(view);
            this.mDeleteButton = (LinearLayout) view.findViewById(R.id.button);
            this.mButtonImage = (ImageView) view.findViewById(R.id.buttonImage);
            if (Glszl_PinLockAdapter.this.heart_zipper_mCustomizationOptionsBundle.isShowDeleteButton() && Glszl_PinLockAdapter.this.mPinLength > 0) {
                this.mDeleteButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View view) {
                        if (Glszl_PinLockAdapter.this.heart_zipper_mOnDeleteClickListener != null) {
                            Glszl_PinLockAdapter.this.heart_zipper_mOnDeleteClickListener.onDeleteClicked();
                        }
                    }
                });
                this.mDeleteButton.setOnLongClickListener(new View.OnLongClickListener() {

                    public boolean onLongClick(View view) {
                        if (Glszl_PinLockAdapter.this.heart_zipper_mOnDeleteClickListener == null) {
                            return true;
                        }
                        Glszl_PinLockAdapter.this.heart_zipper_mOnDeleteClickListener.onDeleteLongClicked();
                        return true;
                    }
                });
                this.mDeleteButton.setOnTouchListener(new View.OnTouchListener() {
                    private Rect rect;

                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == 0) {
                            DeleteViewHolder.this.mButtonImage.setColorFilter(Glszl_PinLockAdapter.this.heart_zipper_mCustomizationOptionsBundle.getDeleteButtonPressesColor());
                            this.rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                        }
                        if (motionEvent.getAction() == 1) {
                            DeleteViewHolder.this.mButtonImage.clearColorFilter();
                        }
                        if (motionEvent.getAction() != 2 || this.rect.contains(view.getLeft() + ((int) motionEvent.getX()), view.getTop() + ((int) motionEvent.getY()))) {
                            return false;
                        }
                        DeleteViewHolder.this.mButtonImage.clearColorFilter();
                        return false;
                    }
                });
            }
        }
    }
}
