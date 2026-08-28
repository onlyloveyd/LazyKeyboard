package com.gs.keyboard;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * 输入回调（OnSecurityKeyListener）测试。
 * 核心断言：回调在按键作用到输入框文本<b>之前</b>触发，
 * 接入方可以在回调里拿到变更前的完整状态自行维护加密序列。
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class OnSecurityKeyListenerTest {

    private Activity activity;
    private SecurityEditText editText;
    private KeyboardDialog dialog;
    private final List<String> events = new ArrayList<>();

    @Before
    public void setUp() {
        activity = Robolectric.setupActivity(Activity.class);
        editText = new SecurityEditText(activity);
        editText.setText("12");
        editText.setSelection(editText.length());
        editText.setOnSecurityKeyListener((primaryCode, label) -> {
            // 记录回调触发瞬间输入框的内容，用于断言时序
            events.add(primaryCode + ":" + editText.getText().length());
        });
        dialog = new KeyboardDialog(activity, editText);
        events.clear();
    }

    @Test
    public void callback_firesBeforeTextMutation() {
        dialog.onKey('3', "3");

        assertEquals(1, events.size());
        // 回调触发时文本仍是 2 位，说明先于插入
        assertEquals("51:2", events.get(0));
        assertEquals("123", editText.getText().toString());
    }

    @Test
    public void deleteKey_notifiedBeforeDeletion() {
        dialog.onKey(OnSecurityKeyListener.KEYCODE_DELETE, null);

        assertEquals(1, events.size());
        assertEquals("-5:2", events.get(0));
        assertEquals("1", editText.getText().toString());
    }

    @Test
    public void keycodeConstants_matchFrameworkValues() {
        assertEquals(-1, OnSecurityKeyListener.KEYCODE_SHIFT);
        assertEquals(-3, OnSecurityKeyListener.KEYCODE_CANCEL);
        assertEquals(-5, OnSecurityKeyListener.KEYCODE_DELETE);
    }
}
