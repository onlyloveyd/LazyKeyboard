package com.gs.keyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 自绘安全键盘视图，替代已废弃的 android.inputmethodservice.KeyboardView。
 * <p>
 * 与框架实现的关键差异（也是 #13 的根因修复）：每个按键持有
 * <b>独立的</b>按键背景 drawable 实例，按键状态与动画不会在按键间串扰，
 * 因此 keyBackground 使用带动画的 selector 也不会导致渲染错乱。
 * <p>
 * 触摸行为对齐框架 KeyboardView：长按可连击按键（如删除键）按下立即触发一次，
 * 400ms 后以 50ms 间隔连发；滑动离开按键不触发；多指触摸取消当前按键。
 */
public class SecurityKeyboardView extends View {

    /** 按键动作回调，由 KeyboardDialog 实现。 */
    public interface OnKeyActionListener {
        void onPress(int primaryCode);

        void onKey(int primaryCode, @Nullable CharSequence label);

        void onRelease(int primaryCode);
    }

    private static final long REPEAT_START_DELAY_MS = 400L;
    private static final long REPEAT_INTERVAL_MS = 50L;
    private static final int[] KEY_STATE_NORMAL = new int[0];
    private static final int[] KEY_STATE_PRESSED = {android.R.attr.state_pressed};

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Drawable> mKeyBackgrounds = new ArrayList<>();
    private final Rect mKeyBackgroundPadding = new Rect();

    private KeyboardLayout mKeyboard;
    private Drawable mKeyBackgroundTemplate;
    private int mKeyTextSize;
    private int mLabelTextSize;
    @ColorInt
    private int mKeyTextColor;
    private int mPreviewHeight;
    private boolean mPreviewEnabled = true;
    private OnKeyActionListener mListener;

    private int mCurrentKeyIndex = -1;
    private int mRepeatKeyIndex = -1;
    private int mPressedCode = 0;
    private boolean mAbortKey = false;
    private final int mTouchSlop;

    private PopupWindow mPreviewPopup;
    private TextView mPreviewText;
    private final int[] mWindowLocation = new int[2];

    private final Runnable mRepeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRepeatKeyIndex >= 0 && mRepeatKeyIndex == mCurrentKeyIndex) {
                fireKey(mRepeatKeyIndex);
                postDelayed(this, REPEAT_INTERVAL_MS);
            }
        }
    };

    public SecurityKeyboardView(Context context) {
        this(context, null);
    }

    public SecurityKeyboardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SecurityKeyboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);

        mKeyTextSize = sp(22);        mLabelTextSize = sp(14);
        mKeyTextColor = Color.BLACK;
        mPreviewHeight = dp(52);
        mKeyBackgroundTemplate = defaultKeyBackground();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SecurityKeyboardView,
                defStyleAttr, 0);
        Drawable background = a.getDrawable(R.styleable.SecurityKeyboardView_keyBackground);
        if (background != null) {
            mKeyBackgroundTemplate = background;
        }
        mKeyTextSize = a.getDimensionPixelSize(R.styleable.SecurityKeyboardView_keyTextSize,
                mKeyTextSize);
        mLabelTextSize = a.getDimensionPixelSize(R.styleable.SecurityKeyboardView_labelTextSize,
                mLabelTextSize);
        mKeyTextColor = a.getColor(R.styleable.SecurityKeyboardView_keyTextColor, mKeyTextColor);
        mPreviewHeight = a.getDimensionPixelSize(R.styleable.SecurityKeyboardView_keyPreviewHeight,
                mPreviewHeight);
        a.recycle();
    }

    /** 与库内 keyboard_key.xml 外观一致的默认按键背景。 */
    private Drawable defaultKeyBackground() {
        GradientDrawable normal = new GradientDrawable();
        normal.setColor(Color.WHITE);
        normal.setStroke(Math.max(1, dp(1)), Color.parseColor("#888888"));
        normal.setCornerRadius(dp(4));

        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(Color.WHITE);
        pressed.setStroke(Math.max(1, dp(1)), Color.WHITE);
        pressed.setCornerRadius(dp(4));

        StateListDrawable selector = new StateListDrawable();
        selector.addState(KEY_STATE_PRESSED, pressed);
        selector.addState(KEY_STATE_NORMAL, normal);
        return selector;
    }

    public void setKeyboard(@Nullable KeyboardLayout keyboard) {
        mKeyboard = keyboard;
        mKeyBackgrounds.clear();
        if (keyboard != null) {
            for (int i = 0; i < keyboard.getKeys().size(); i++) {
                mKeyBackgrounds.add(newKeyBackgroundInstance());
            }
        }
        mCurrentKeyIndex = -1;
        mRepeatKeyIndex = -1;
        requestLayout();
        invalidate();
    }

    private Drawable newKeyBackgroundInstance() {
        Drawable.ConstantState state = mKeyBackgroundTemplate.getConstantState();
        Drawable instance = state != null
                ? state.newDrawable(getResources())
                : mKeyBackgroundTemplate;
        return instance.mutate();
    }

    public void setPreviewEnabled(boolean enabled) {
        mPreviewEnabled = enabled;
        if (!enabled) {
            hidePreview();
        }
    }

    public void setOnKeyActionListener(@Nullable OnKeyActionListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mKeyboard == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int baseWidth = Math.max(0, specWidth - getPaddingLeft() - getPaddingRight());
        mKeyboard.layout(baseWidth);
        int width = resolveSize(mKeyboard.getTotalWidth()
                + getPaddingLeft() + getPaddingRight(), widthMeasureSpec);
        int height = mKeyboard.getTotalHeight() + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mKeyboard == null) {
            return;
        }
        final float paddingLeft = getPaddingLeft();
        final float paddingTop = getPaddingTop();
        List<KeyboardLayout.Key> keys = mKeyboard.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            KeyboardLayout.Key key = keys.get(i);
            Drawable keyBackground = mKeyBackgrounds.get(i);
            keyBackground.setState(i == mCurrentKeyIndex ? KEY_STATE_PRESSED : KEY_STATE_NORMAL);
            keyBackground.setBounds(0, 0, key.width, key.height);
            if (!keyBackground.getPadding(mKeyBackgroundPadding)) {
                mKeyBackgroundPadding.setEmpty();
            }
            canvas.save();
            canvas.translate(key.x + paddingLeft, key.y + paddingTop);
            keyBackground.draw(canvas);
            drawKeyContent(canvas, key);
            canvas.restore();
        }
    }

    private void drawKeyContent(Canvas canvas, KeyboardLayout.Key key) {
        CharSequence label = key.label;
        Rect padding = mKeyBackgroundPadding;
        if (label != null && label.length() > 0) {
            boolean isLabel = label.length() > 1 && key.codes.length < 2;
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setColor(mKeyTextColor);
            if (isLabel) {
                mPaint.setTextSize(mLabelTextSize);
                mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                mPaint.setTextSize(mKeyTextSize);
                mPaint.setTypeface(Typeface.DEFAULT);
            }
            float centerX = (key.width - padding.left - padding.right) / 2f + padding.left;
            float centerY = (key.height - padding.top - padding.bottom) / 2f + padding.top
                    + (mPaint.getTextSize() - mPaint.descent()) / 2f;
            canvas.drawText(label, 0, label.length(), centerX, centerY, mPaint);
        } else if (key.icon != null) {
            int iconWidth = key.icon.getIntrinsicWidth();
            int iconHeight = key.icon.getIntrinsicHeight();
            int drawableX = (key.width - padding.left - padding.right - iconWidth) / 2 + padding.left;
            int drawableY = (key.height - padding.top - padding.bottom - iconHeight) / 2 + padding.top;
            canvas.translate(drawableX, drawableY);
            key.icon.setBounds(0, 0, iconWidth, iconHeight);
            key.icon.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        // 第二根手指按下：取消当前按键，避免两指连点误触
        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            cancelKey();
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mAbortKey = false;
                mCurrentKeyIndex = keyIndexAt(event);
                if (mCurrentKeyIndex >= 0) {
                    pressKey(mCurrentKeyIndex);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                int index = keyIndexAt(event);
                if (index != mCurrentKeyIndex) {
                    // 滑出当前按键：取消按压与连发，跟随手指高亮新按键
                    if (mCurrentKeyIndex >= 0) {
                        cancelRepeat();
                    }
                    mCurrentKeyIndex = index;
                    if (index >= 0) {
                        showPreview(index);
                    } else {
                        hidePreview();
                    }
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                int upIndex = mAbortKey ? -1 : keyIndexAt(event);
                boolean handledByRepeat = mRepeatKeyIndex >= 0;
                cancelRepeat();
                if (upIndex >= 0 && upIndex == mCurrentKeyIndex && !handledByRepeat) {
                    fireKey(upIndex);
                }
                if (mCurrentKeyIndex >= 0) {
                    notifyRelease();
                }
                resetTouchState();
                return true;
            case MotionEvent.ACTION_CANCEL:
            default:
                cancelKey();
                return true;
        }
    }

    private int keyIndexAt(MotionEvent event) {
        if (mKeyboard == null) {
            return -1;
        }
        float x = event.getX() - getPaddingLeft();
        float y = event.getY() - getPaddingTop();
        int index = mKeyboard.getKeyIndexForPosition(x, y);
        if (index >= 0) {
            return index;
        }
        // 命中判定向四周放宽半个 touchSlop，容错贴边触摸
        float slop = mTouchSlop / 2f;
        if (index < 0) {
            index = mKeyboard.getKeyIndexForPosition(x + slop, y);
        }
        if (index < 0) {
            index = mKeyboard.getKeyIndexForPosition(x - slop, y);
        }
        if (index < 0) {
            index = mKeyboard.getKeyIndexForPosition(x, y + slop);
        }
        if (index < 0) {
            index = mKeyboard.getKeyIndexForPosition(x, y - slop);
        }
        return index;
    }

    private void pressKey(int index) {
        KeyboardLayout.Key key = mKeyboard.getKeys().get(index);
        mPressedCode = key.getPrimaryCode();
        if (mListener != null) {
            mListener.onPress(mPressedCode);
        }
        showPreview(index);
        invalidate();
        if (key.repeatable) {
            // 可连击按键按下立即触发一次，随后按间隔连发
            mRepeatKeyIndex = index;
            fireKey(index);
            postDelayed(mRepeatRunnable, REPEAT_START_DELAY_MS);
        }
    }

    private void fireKey(int index) {
        KeyboardLayout.Key key = mKeyboard.getKeys().get(index);
        if (mListener != null) {
            mListener.onKey(key.getPrimaryCode(), key.label);
        }
    }

    private void notifyRelease() {
        if (mListener != null && mPressedCode != 0) {
            mListener.onRelease(mPressedCode);
        }
    }

    private void cancelRepeat() {
        removeCallbacks(mRepeatRunnable);
        mRepeatKeyIndex = -1;
    }

    private void resetTouchState() {
        mCurrentKeyIndex = -1;
        mPressedCode = 0;
        hidePreview();
        invalidate();
    }

    private void cancelKey() {
        cancelRepeat();
        mAbortKey = true;
        mCurrentKeyIndex = -1;
        hidePreview();
        invalidate();
    }

    // ------------------------------------------------------------------
    // 按键预览气泡
    // ------------------------------------------------------------------

    private void ensurePreviewPopup() {
        if (mPreviewPopup != null) {
            return;
        }
        mPreviewText = new TextView(getContext());
        mPreviewText.setGravity(Gravity.CENTER);
        mPreviewText.setTextColor(Color.WHITE);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#CC2B2B2B"));
        background.setCornerRadius(dp(6));
        mPreviewText.setBackground(background);

        mPreviewPopup = new PopupWindow(mPreviewText, ViewGroup.LayoutParams.WRAP_CONTENT,
                mPreviewHeight, false);
        mPreviewPopup.setTouchable(false);
        mPreviewPopup.setOutsideTouchable(false);
        mPreviewPopup.setClippingEnabled(true);
    }

    private void showPreview(int index) {
        if (!mPreviewEnabled || mKeyboard == null) {
            return;
        }
        KeyboardLayout.Key key = mKeyboard.getKeys().get(index);
        boolean hasText = key.label != null && key.label.length() > 0;
        if (!hasText && key.icon == null) {
            hidePreview();
            return;
        }
        ensurePreviewPopup();
        if (hasText) {
            mPreviewText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            mPreviewText.setText(key.label);
            if (key.label.length() > 1 && key.codes.length < 2) {
                mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLabelTextSize);
                mPreviewText.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mKeyTextSize);
                mPreviewText.setTypeface(Typeface.DEFAULT);
            }
        } else {
            mPreviewText.setText(null);
            mPreviewText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, key.icon);
        }
        mPreviewText.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(mPreviewHeight, MeasureSpec.EXACTLY));
        int popupWidth = Math.max(mPreviewText.getMeasuredWidth(),
                key.width + mPreviewText.getPaddingLeft() + mPreviewText.getPaddingRight());
        int popupHeight = mPreviewHeight;

        // PopupWindow.showAtLocation 的坐标是宿主窗口相对坐标，必须用
        // getLocationInWindow（getLocationOnScreen 是屏幕坐标，会导致气泡被
        // WindowManager 钳到屏幕底端）
        getLocationInWindow(mWindowLocation);
        int keyCenterX = mWindowLocation[0] + getPaddingLeft() + key.x + key.width / 2;
        int popupX = keyCenterX - popupWidth / 2;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        popupX = Math.max(0, Math.min(popupX, screenWidth - popupWidth));

        int keyTopInWindow = mWindowLocation[1] + getPaddingTop() + key.y;
        int popupY = keyTopInWindow - popupHeight - dp(8);
        if (popupY < 0) {
            // 上方空间不足（首行按键）时改为贴在按键下方
            popupY = keyTopInWindow + key.height + dp(8);
        }

        if (mPreviewPopup.isShowing()) {
            mPreviewPopup.update(popupX, popupY, popupWidth, popupHeight);
        } else {
            mPreviewPopup.setWidth(popupWidth);
            mPreviewPopup.setHeight(popupHeight);
            mPreviewPopup.showAtLocation(this, Gravity.NO_GRAVITY, popupX, popupY);
        }
    }

    private void hidePreview() {
        if (mPreviewPopup != null && mPreviewPopup.isShowing()) {
            mPreviewPopup.dismiss();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelRepeat();
        hidePreview();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private int sp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value,
                getResources().getDisplayMetrics());
    }
}
