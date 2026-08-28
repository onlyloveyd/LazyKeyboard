package com.gs.keyboard;

import android.content.Context;
import android.view.MotionEvent;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * 自绘键盘视图的触摸路由测试。
 * 覆盖：普通按键按下不触发、抬起触发一次；可连击按键（删除）按下立即触发、
 * 抬起不重复触发；滑动离开按键后抬起不触发；按键回调先于文本变化（见 SecurityKeyListenerTest）。
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SecurityKeyboardViewTest {

    private static final int BASE_WIDTH = 1080;

    private final Context context = ApplicationProvider.getApplicationContext();
    private SecurityKeyboardView view;
    private KeyboardLayout keyboard;
    private final List<int[]> firedKeys = new ArrayList<>();

    @Before
    public void setUp() {
        view = new SecurityKeyboardView(context);
        view.setPreviewEnabled(false);
        view.setOnKeyActionListener(new SecurityKeyboardView.OnKeyActionListener() {
            @Override
            public void onPress(int primaryCode) {
            }

            @Override
            public void onKey(int primaryCode, CharSequence label) {
                firedKeys.add(new int[]{primaryCode});
            }

            @Override
            public void onRelease(int primaryCode) {
            }
        });
        keyboard = KeyboardLayout.parse(context, R.xml.gs_keyboard_english);
        view.setKeyboard(keyboard);
        view.measure(
                android.view.View.MeasureSpec.makeMeasureSpec(BASE_WIDTH,
                        android.view.View.MeasureSpec.EXACTLY),
                android.view.View.MeasureSpec.makeMeasureSpec(0,
                        android.view.View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        firedKeys.clear();
    }

    @Test
    public void normalKey_firesOnceOnUp() {
        KeyboardLayout.Key key = centerOf('q');

        dispatch(MotionEvent.ACTION_DOWN, key);
        assertEquals(0, firedKeys.size());

        dispatch(MotionEvent.ACTION_UP, key);
        assertEquals(1, firedKeys.size());
        assertEquals('q', firedKeys.get(0)[0]);
    }

    @Test
    public void repeatableKey_firesOnDown_notAgainOnUp() {
        KeyboardLayout.Key delete = centerOf(KeyboardLayout.KEYCODE_DELETE);

        dispatch(MotionEvent.ACTION_DOWN, delete);
        assertEquals("可连击按键按下立即触发一次", 1, firedKeys.size());
        assertEquals(KeyboardLayout.KEYCODE_DELETE, firedKeys.get(0)[0]);

        dispatch(MotionEvent.ACTION_UP, delete);
        assertEquals("抬起不重复触发", 1, firedKeys.size());
    }

    @Test
    public void slideOffKey_doesNotFire() {
        KeyboardLayout.Key key = centerOf('q');
        KeyboardLayout.Key other = centerOf('m');

        dispatch(MotionEvent.ACTION_DOWN, key);
        dispatch(MotionEvent.ACTION_MOVE, other);
        dispatch(MotionEvent.ACTION_UP, other);
        assertEquals("滑动换键后抬起触发的是当前键", 1, firedKeys.size());
        assertEquals('m', firedKeys.get(0)[0]);
    }

    @Test
    public void releaseOutsideKeyboard_doesNotFire() {
        KeyboardLayout.Key key = centerOf('q');

        dispatch(MotionEvent.ACTION_DOWN, key);
        MotionEvent up = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP,
                -100f, -100f, 0);
        view.dispatchTouchEvent(up);
        up.recycle();
        assertEquals(0, firedKeys.size());
    }

    @Test
    public void measuredHeight_coversAllRows() {
        assertEquals(keyboard.getTotalHeight(), view.getMeasuredHeight());
    }

    private KeyboardLayout.Key centerOf(int code) {
        for (KeyboardLayout.Key key : keyboard.getKeys()) {
            if (key.getPrimaryCode() == code) {
                return key;
            }
        }
        throw new AssertionError("key not found: " + code);
    }

    private void dispatch(int action, KeyboardLayout.Key key) {
        float x = key.getX() + key.getWidth() / 2f;
        float y = key.getY() + key.getHeight() / 2f;
        MotionEvent event = MotionEvent.obtain(0, 0, action, x, y, 0);
        view.dispatchTouchEvent(event);
        event.recycle();
    }
}
