package com.gs.keyboard;

import androidx.annotation.Nullable;

/**
 * 安全键盘输入回调：在按键作用到输入框文本<b>之前</b>触发，
 * 接入方可以借此拿到完整按键序列，用于自行维护加密内容或做输入审计。
 * <pre>
 * SecurityEditText editText = findViewById(R.id.password);
 * editText.setOnSecurityKeyListener((primaryCode, label) -&gt; {
 *     if (primaryCode == OnSecurityKeyListener.KEYCODE_DELETE) {
 *         // 删除一位密文
 *     } else if (primaryCode &gt;= 0) {
 *         // 追加一个字符（label 为按键文案，可为 null）
 *     }
 * });
 * </pre>
 */
public interface OnSecurityKeyListener {

    int KEYCODE_SHIFT = KeyboardLayout.KEYCODE_SHIFT;
    int KEYCODE_MODE_CHANGE = KeyboardLayout.KEYCODE_MODE_CHANGE;
    int KEYCODE_CANCEL = KeyboardLayout.KEYCODE_CANCEL;
    int KEYCODE_DONE = KeyboardLayout.KEYCODE_DONE;
    int KEYCODE_DELETE = KeyboardLayout.KEYCODE_DELETE;

    /**
     * @param primaryCode 按键主码：&gt;= 0 为字符编码；负数为功能键，见本接口常量
     * @param label       按键显示文案，图标按键（如 shift、删除）为 null
     */
    void onKey(int primaryCode, @Nullable CharSequence label);
}
