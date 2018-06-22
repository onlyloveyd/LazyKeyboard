package com.gs.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gs.utils.DisplayUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 安全键盘主类
 *
 * @author yidong (onlyloveyd@gmaol.com)
 * @date 2018/6/22 07:45
 */
public class SecurityKeyboard extends PopupWindow {

    private KeyboardView keyboardView;

    /**
     * 字母键盘
     */
    private Keyboard mKeyboardEnglish;

    /**
     * 符号键盘
     */
    private Keyboard mKeyboardNumber, mKeyboardSymbol;

    /**
     * 是否为数字键盘
     */
    private boolean isNumber = false;

    /**
     * 是否大写
     */
    private boolean isUpper = false;

    private TextView tvSymbol, tvLetter, tvNumber;
    private View mMainView;
    private ArrayList<String> nums_ = new ArrayList<>();

    private SecurityEditText curEditText;

    RelativeLayout keyboard_view_ly;

    private ViewGroup mParentLayout;
    private Context mContext;

    @SuppressLint("ClickableViewAccessibility")
    public SecurityKeyboard(final ViewGroup parentLayout) {
        super(parentLayout.getContext());
        mParentLayout = parentLayout;

        mContext = parentLayout.getContext();

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMainView = inflater.inflate(R.layout.gs_keyboard, null);
        this.setContentView(mMainView);
        this.setWidth(DisplayUtils.getScreenWidth(mContext));
        this.setHeight(LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(Color.parseColor("#00000000"));
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);

        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setPopupWindowTouchModal(this, false);

        this.setAnimationStyle(R.style.PopupKeybroad);
        if (DisplayUtils.dp2px(mContext, 236) > (int) (DisplayUtils
                .getScreenHeight(mContext) * 3.0f / 5.0f)) {
            mKeyboardEnglish = new Keyboard(mContext,
                    R.xml.gs_keyboard_english_land);
            mKeyboardNumber = new Keyboard(mContext, R.xml.gs_keyboard_number_land);
            mKeyboardSymbol = new Keyboard(mContext,
                    R.xml.gs_keyboard_symbols_shift_land);
        } else {
            mKeyboardEnglish = new Keyboard(mContext, R.xml.gs_keyboard_english);
            mKeyboardNumber = new Keyboard(mContext, R.xml.gs_keyboard_number);
            mKeyboardSymbol = new Keyboard(mContext,
                    R.xml.gs_keyboard_symbols_shift);
        }

        keyboardView = mMainView
                .findViewById(R.id.keyboard_view);
        keyboard_view_ly = mMainView.findViewById(R.id.keyboard_view_ly);

        tvSymbol = mMainView.findViewById(R.id.tv_symbol);
        tvLetter = mMainView.findViewById(R.id.tv_letter);
        tvNumber = mMainView.findViewById(R.id.tv_number);

        initNumbers();
        randomNumbers();
        keyboardView.setKeyboard(mKeyboardEnglish);
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(listener);
        tvNumber.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                randomNumbers();
                keyboardView.setKeyboard(mKeyboardNumber);
            }
        });
        tvLetter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                keyboardView.setKeyboard(mKeyboardEnglish);
            }
        });
        tvSymbol.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                keyboardView.setKeyboard(mKeyboardSymbol);
            }
        });

        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View view = parentLayout.getChildAt(i);
            if (view instanceof SecurityEditText) {
                SecurityEditText securityEditText = (SecurityEditText) view;
                securityEditText.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            curEditText = (SecurityEditText) v;
                            curEditText.requestFocus();
                            curEditText.setInputType(InputType.TYPE_NULL);
                            //将光标移到文本最后
                            Editable editable = curEditText.getText();
                            Selection.setSelection(editable, editable.length());
                            hideSystemKeyboard(v);
                            showKeyboard(mParentLayout);
                        }
                        return true;
                    }
                });
            }
        }
    }

    /**
     * @param popupWindow popupWindow 的touch事件传递
     * @param touchModal  true代表拦截，事件不向下一层传递，false表示不拦截，事件向下一层传递
     */
    @SuppressLint("PrivateApi")
    private void setPopupWindowTouchModal(PopupWindow popupWindow,
                                          boolean touchModal) {
        Method method;
        try {
            method = PopupWindow.class.getDeclaredMethod("setTouchModal",
                    boolean.class);
            method.setAccessible(true);
            method.invoke(popupWindow, touchModal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideSystemKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initNumbers() {
        nums_.clear();
        nums_.add("48#0");
        nums_.add("49#1");
        nums_.add("50#2");
        nums_.add("51#3");
        nums_.add("52#4");
        nums_.add("53#5");
        nums_.add("54#6");
        nums_.add("55#7");
        nums_.add("56#8");
        nums_.add("57#9");
    }

    private OnKeyboardActionListener listener = new OnKeyboardActionListener() {
        @Override
        public void swipeUp() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void onText(CharSequence text) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onPress(int primaryCode) {
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            Editable editable = curEditText.getText();
            int start = curEditText.getSelectionStart();
            if (primaryCode == Keyboard.KEYCODE_CANCEL) {
                // 完成按钮所做的动作
                hideKeyboard();
            } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                // 删除按钮所做的动作
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
            } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
                // 大小写切换
                changeKey();
                keyboardView.setKeyboard(mKeyboardEnglish);

            } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {
                // 数字键盘切换,默认是英文键盘
                if (isNumber) {
                    isNumber = false;
                    keyboardView.setKeyboard(mKeyboardEnglish);
                } else {
                    isNumber = true;
                    keyboardView.setKeyboard(mKeyboardNumber);
                }
            } else if (primaryCode == 57419) {
                //左移
                if (start > 0) {
                    curEditText.setSelection(start - 1);
                }
            } else if (primaryCode == 57421) {
                //右移
                if (start < curEditText.length()) {
                    curEditText.setSelection(start + 1);
                }
            } else {
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }
    };

    /**
     * 键盘大小写切换
     */
    private void changeKey() {
        List<Key> keylist = mKeyboardEnglish.getKeys();
        if (isUpper) {
            isUpper = false;
            for (Key key : keylist) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
                if (key.codes[0] == -1) {
                    key.icon = mContext.getResources().getDrawable(
                            R.drawable.keyboard_shift);
                }
            }
        } else {// 小写切换大写
            isUpper = true;
            for (Key key : keylist) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
                if (key.codes[0] == -1) {
                    key.icon = mContext.getResources().getDrawable(
                            R.drawable.keyboard_shift_c);
                }
            }
        }
    }

    /**
     * 键盘数字随机切换
     */
    private void randomNumbers() {
        List<Key> keylist = mKeyboardNumber.getKeys();
        ArrayList<String> temNum = new ArrayList<>(nums_);

        for (Key key : keylist) {
            if (key.label != null && isNumber(key.label.toString())) {
                int number = new Random().nextInt(temNum.size());
                String[] text = temNum.get(number).split("#");
                key.label = text[1];
                key.codes[0] = Integer.valueOf(text[0], 10);
                temNum.remove(number);
            }
        }
    }

    /**
     * 弹出键盘
     *
     * @param view
     */
    private void showKeyboard(View view) {
        int realHeight = 0;
        int yOff;

        yOff = realHeight - DisplayUtils.dp2px(mContext, 231);

        if (DisplayUtils.dp2px(mContext, 236) > (int) (DisplayUtils
                .getScreenHeight(mContext) * 3.0f / 5.0f)) {
            yOff = DisplayUtils.getScreenHeight(mContext)
                    - DisplayUtils.dp2px(mContext, 199);
        }
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in);
        showAtLocation(view, Gravity.BOTTOM | Gravity.LEFT, 0, yOff);
        getContentView().setAnimation(animation);
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        this.dismiss();
    }

    private boolean isLetter(String str) {
        String letterStr = mContext.getString(R.string.aToz);
        return letterStr.contains(str.toLowerCase());
    }

    private boolean isNumber(String str) {
        String numStr = mContext.getString(R.string.zeroTonine);
        return numStr.contains(str.toLowerCase());
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
