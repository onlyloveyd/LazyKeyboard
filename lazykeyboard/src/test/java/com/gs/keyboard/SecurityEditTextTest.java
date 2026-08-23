package com.gs.keyboard;

import android.view.inputmethod.EditorInfo;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * 安全输入框的输入法阻断回归测试。
 * 对应 issue #12：任何路径（聚焦、长按选择、业务代码主动 showSoftInput）
 * 都不允许系统键盘绑定到 SecurityEditText。
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SecurityEditTextTest {

    @Test
    public void onCreateInputConnection_returnsNull_noImeCanBind() {
        SecurityEditText editText = new SecurityEditText(ApplicationProvider.getApplicationContext());

        assertNull(editText.onCreateInputConnection(new EditorInfo()));
    }

    @Test
    public void keyboardDialog_canBeCreatedWithContext() {
        SecurityEditText editText = new SecurityEditText(ApplicationProvider.getApplicationContext());

        assertNotNull(editText.getKeyboardAttribute());
    }
}
