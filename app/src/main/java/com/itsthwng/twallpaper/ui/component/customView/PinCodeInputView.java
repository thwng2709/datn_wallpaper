package com.itsthwng.twallpaper.ui.component.customView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.itsthwng.twallpaper.R;

import java.util.Arrays;

public class PinCodeInputView extends FrameLayout {

    private static final int DEFAULT_PIN_COUNT = 4;
    private static final long DEFAULT_MASK_DELAY_MS = 350L;

    private final EditText input;
    private final IndicatorView indicator;

    private int pinCount = DEFAULT_PIN_COUNT;
    private long maskDelayMs = DEFAULT_MASK_DELAY_MS;

    // Paint & dimensions
    private float dotRadiusPx;
    private float dotSpacingPx;
    private int colorDotEmpty;
    private int colorDotFilled;
    private int colorDigit;
    private float digitTextSizePx;

    // State per slot
    private char[] chars;
    private State[] states;
    private long[] showUntil; // time millis to keep showing digit before masking

    private enum State {EMPTY, SHOW_DIGIT, FILLED}

    public PinCodeInputView(Context context) {
        this(context, null);
    }

    public PinCodeInputView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setImeAction(int imeAction) {
        input.setImeOptions(imeAction);
    }

    public PinCodeInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Defaults
        float dp = getResources().getDisplayMetrics().density;
        dotRadiusPx = 8f * dp;
        dotSpacingPx = 20f * dp;
        digitTextSizePx = sp(20);
        colorDotEmpty = 0x55FFFFFF;  // trắng mờ
        colorDotFilled = 0xFFFFFFFF; // trắng sáng
        colorDigit = 0xFFFFFFFF;     // số màu trắng

        // Read XML attributes (optional)
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PinCodeInputView);
            pinCount = a.getInt(R.styleable.PinCodeInputView_pinCount, DEFAULT_PIN_COUNT);
            maskDelayMs = a.getInt(R.styleable.PinCodeInputView_maskDelayMs, (int) DEFAULT_MASK_DELAY_MS);
            dotRadiusPx = a.getDimension(R.styleable.PinCodeInputView_dotRadius, dotRadiusPx);
            dotSpacingPx = a.getDimension(R.styleable.PinCodeInputView_dotSpacing2, dotSpacingPx);
            digitTextSizePx = a.getDimension(R.styleable.PinCodeInputView_digitTextSize, digitTextSizePx);
            colorDotEmpty = a.getColor(R.styleable.PinCodeInputView_colorDotEmpty, colorDotEmpty);
            colorDotFilled = a.getColor(R.styleable.PinCodeInputView_colorDotFilled, colorDotFilled);
            colorDigit = a.getColor(R.styleable.PinCodeInputView_colorDigit, colorDigit);
            a.recycle();
        }

        chars = new char[pinCount];
        states = new State[pinCount];
        showUntil = new long[pinCount];
        Arrays.fill(chars, '\0');
        Arrays.fill(states, State.EMPTY);
        Arrays.fill(showUntil, 0L);

        // Child 1: EditText (ẩn văn bản)
        input = new EditText(context);
        input.setBackground(null);
        input.setCursorVisible(false);
        input.setGravity(Gravity.CENTER);
        input.setTextColor(0x00000000);  // textColor trong suốt để không thấy
        input.setHintTextColor(0x00000000);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(pinCount)});
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            input.setImportantForAutofill(IMPORTANT_FOR_AUTOFILL_NO);
            input.setAutofillHints((String[]) null);
        }
        input.setPadding(0, 0, 0, 0);
        input.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(input);

        // Child 2: Indicator overlay (vẽ 4 dấu chấm / số)
        indicator = new IndicatorView(context);
        indicator.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(indicator);

        // Lắng nghe thay đổi text
        input.addTextChangedListener(new TextWatcher() {
            private String old = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                old = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String now = s.toString();
                // Giới hạn chỉ cho số
                if (!now.matches("\\d*")) {
                    // lọc ký tự không phải số
                    String filtered = now.replaceAll("\\D+", "");
                    input.removeTextChangedListener(this);
                    input.setText(filtered);
                    input.setSelection(filtered.length());
                    input.addTextChangedListener(this);
                    now = filtered;
                }

                // Cập nhật state
                updateStatesFromText(old, now);
            }
        });

        // Khi chạm vào vùng hiển thị chấm, focus vào EditText & show keyboard
        setClickable(true);
        setFocusable(true);
        setOnClickListener(v -> focusInput());
    }

    /**
     * Trả về Editable giống EditText.getText()
     */
    public Editable getText() {
        return input.getText();
    }

    /**
     * Đặt sẵn giá trị PIN (giống setText)
     */
    public void setText(String text) {
        if (text == null) text = "";
        // lọc ký tự không phải số
        String filtered = text.replaceAll("\\D+", "");
        if (filtered.length() > pinCount) {
            filtered = filtered.substring(0, pinCount);
        }
        input.setText(filtered);
        input.setSelection(filtered.length());
        updateStatesFromText("", filtered);
    }

    /**
     * Alias cho setText
     */
    public void setPin(String text) {
        setText(text);
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }

    private void focusInput() {
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
    }

    private void updateStatesFromText(String oldText, String newText) {
        int oldLen = oldText.length();
        int newLen = newText.length();

        long now = System.currentTimeMillis();

        // Reset all to empty
        for (int i = 0; i < pinCount; i++) {
            chars[i] = '\0';
            states[i] = State.EMPTY;
            showUntil[i] = 0L;
        }

        // Fill known chars; by default mark FILLED
        for (int i = 0; i < newLen && i < pinCount; i++) {
            chars[i] = newText.charAt(i);
            states[i] = State.FILLED;
        }

        // Detect if last action is an insert; show last digit briefly
        if (newLen > 0 && newLen > oldLen) {
            int idx = newLen - 1;
            if (idx >= 0 && idx < pinCount) {
                states[idx] = State.SHOW_DIGIT;
                showUntil[idx] = now + maskDelayMs;

                // Schedule masking
                postDelayed(() -> {
                    // If still time passed and state is SHOW_DIGIT, mask it
                    if (states[idx] == State.SHOW_DIGIT && System.currentTimeMillis() >= showUntil[idx]) {
                        states[idx] = State.FILLED;
                        indicator.invalidate();
                    }
                }, maskDelayMs);
            }
        }

        indicator.invalidate();
    }

    /**
     * Lấy PIN hiện tại (chuỗi số)
     */
    public String getPin() {
        return input.getText() == null ? "" : input.getText().toString();
    }

    /**
     * Xoá PIN
     */
    public void clearPin() {
        input.setText("");
        Arrays.fill(chars, '\0');
        Arrays.fill(states, State.EMPTY);
        Arrays.fill(showUntil, 0L);
        indicator.invalidate();
        focusInput();
    }

    /**
     * Đặt độ trễ mask (ms) sau khi hiển thị số
     */
    public void setMaskDelayMs(long delayMs) {
        this.maskDelayMs = Math.max(0, delayMs);
    }

    /**
     * Đặt số ô PIN (mặc định 4). Gọi sớm trước khi view đo/hiển thị.
     */
    public void setPinCount(int count) {
        if (count < 1) count = 1;
        this.pinCount = count;
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(pinCount)});
        chars = new char[pinCount];
        states = new State[pinCount];
        showUntil = new long[pinCount];
        Arrays.fill(chars, '\0');
        Arrays.fill(states, State.EMPTY);
        Arrays.fill(showUntil, 0L);
        indicator.invalidate();
    }

    /**
     * Cho phép đặt màu/size tùy biến nếu cần (tuỳ chọn)
     */
    public void setDotSizes(float radiusPx, float spacingPx) {
        this.dotRadiusPx = radiusPx;
        this.dotSpacingPx = spacingPx;
        indicator.invalidate();
    }

    public void setColors(int emptyDot, int filledDot, int digitColor) {
        this.colorDotEmpty = emptyDot;
        this.colorDotFilled = filledDot;
        this.colorDigit = digitColor;
        indicator.invalidate();
    }

    public void setDigitTextSizePx(float sizePx) {
        this.digitTextSizePx = sizePx;
        indicator.invalidate();
    }

    /**
     * Vẽ lớp chấm / số
     */
    private class IndicatorView extends View {
        private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint digitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Rect textBounds = new Rect();
        private boolean cursorVisible = true;

        public IndicatorView(Context context) {
            super(context);
            setWillNotDraw(false);
            dotPaint.setStyle(Paint.Style.FILL);

            digitPaint.setStyle(Paint.Style.FILL);
            digitPaint.setTextAlign(Paint.Align.CENTER);
            digitPaint.setTextSize(digitTextSizePx);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();

            // Tính vị trí 4 chấm nằm giữa view
            float totalWidth = pinCount * (dotRadiusPx * 2) + (pinCount - 1) * dotSpacingPx;
            float startX = (w - totalWidth) / 2f + dotRadiusPx; // tâm chấm đầu tiên
            float centerY = h / 2f;

            long now = System.currentTimeMillis();

            for (int i = 0; i < pinCount; i++) {
                float cx = startX + i * ((dotRadiusPx * 2) + dotSpacingPx);

                State st = states[i];
                if (st == State.EMPTY) {
                    dotPaint.setColor(colorDotEmpty);
                    canvas.drawCircle(cx, centerY, dotRadiusPx, dotPaint);
                } else if (st == State.FILLED) {
                    dotPaint.setColor(colorDotFilled);
                    canvas.drawCircle(cx, centerY, dotRadiusPx, dotPaint);
                } else { // SHOW_DIGIT
                    if (now < showUntil[i]) {
                        // Vẽ ký tự
                        digitPaint.setColor(colorDigit);
                        digitPaint.setTextSize(digitTextSizePx);

                        String s = String.valueOf(chars[i]);
                        digitPaint.getTextBounds(s, 0, 1, textBounds);
                        float textHeight = textBounds.height();
                        float textBaseline = centerY + textHeight / 2f;
                        canvas.drawText(s, cx, textBaseline, digitPaint);
                    } else {
                        // Hết thời gian show -> vẽ chấm đậm
                        dotPaint.setColor(colorDotFilled);
                        canvas.drawCircle(cx, centerY, dotRadiusPx, dotPaint);
                    }
                }
            }

            // Vẽ con trỏ nhấp nháy nếu có focus
            if (cursorVisible && input.hasFocus()) {
                int cursorPos = input.getSelectionStart();
                if (cursorPos < 0) {
                    cursorPos = 0;
                }
                if (cursorPos >= pinCount) {
                    cursorPos = pinCount - 1;
                }

                float cursorX = startX + cursorPos * ((dotRadiusPx * 2) + dotSpacingPx);
                float cursorTop = centerY - dotRadiusPx * 2f;   // cao hơn chấm
                float cursorBottom = centerY + dotRadiusPx * 2f; // thấp hơn chấm

                Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                cursorPaint.setColor(colorDigit);
                cursorPaint.setStrokeWidth(3f);
                canvas.drawLine(cursorX, cursorTop, cursorX, cursorBottom, cursorPaint);
            }

        }

        // Nhấp nháy con trỏ
        private final Runnable blink = new Runnable() {
            @Override
            public void run() {
                cursorVisible = !cursorVisible;
                invalidate();
                postDelayed(this, 300); // nhấp nháy mỗi 0.3 giây
            }
        };

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            removeCallbacks(blink);
            post(blink);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            removeCallbacks(blink);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                focusInput();
            }
            return super.onTouchEvent(event);
        }
    }
}
