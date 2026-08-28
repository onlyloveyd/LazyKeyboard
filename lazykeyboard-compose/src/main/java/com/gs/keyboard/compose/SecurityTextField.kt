package com.gs.keyboard.compose

import android.text.InputType
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.gs.keyboard.SecurityEditText

/**
 * Compose 版安全输入框。
 *
 * 基于 `AndroidView` 包装核心库的 [SecurityEditText]：点击后弹出 LazyKeyboard
 * 安全键盘，系统输入法全程不参与。内容通过 [SecurityInputState] 暴露为
 * Compose 状态，按键回调 [onKey] 在每次按键作用到文本之前触发。
 *
 * @param state      由 [rememberSecurityInputState] 创建的可观察输入状态
 * @param hint       占位提示文案
 * @param isPassword true 时按密码形态输入（默认）；false 为普通明文输入
 * @param onKey      按键回调，功能键（大小写/完成/删除）为负数编码，
 *                   常量见 [com.gs.keyboard.OnSecurityKeyListener]
 */
@Composable
fun SecurityTextField(
    state: SecurityInputState,
    modifier: Modifier = Modifier,
    hint: String = "",
    isPassword: Boolean = true,
    onKey: (primaryCode: Int, label: CharSequence?) -> Unit = { _, _ -> },
) {
    // 跨配置变更记住输入框是否持有焦点：旋转后恢复焦点，
    // SecurityEditText 的聚焦逻辑会随之重新弹出键盘（与系统输入法行为一致）
    // 显式 Boolean Saver:rememberSaveable 的 autoSaver 对 MutableState 在当前
    // Compose 版本上保存后不恢复(真机验证),焦点标记会静默回到 false
    val focusSaver = Saver<Boolean, Boolean>(save = { it }, restore = { it })
    var focused by rememberSaveable(stateSaver = focusSaver) { mutableStateOf(false) }

    AndroidView<SecurityEditText>(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            SecurityEditText(context).apply {
                inputType = if (isPassword) {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT
                }
            }
        },
        update = { editText: SecurityEditText ->
            editText.hint = hint
            editText.setOnSecurityKeyListener { primaryCode: Int, label: CharSequence? ->
                state.dispatchKey(primaryCode)
                onKey(primaryCode, label)
            }
            editText.setOnFocusChangeListener { _, hasFocus -> focused = hasFocus }
            if (focused && !editText.hasFocus()) {
                // 重建的输入框：post 到视图附着后再恢复焦点，键盘随之弹出
                editText.post { editText.requestFocus() }
            }
            // 配置变更后 factory 重建了空的输入框、或 clear() 只改了状态时，
            // 把状态同步回输入框；正常键盘输入两者一致，不会走到这里
            if (editText.text.toString() != state.text) {
                editText.setText(state.text)
                editText.setSelection(state.text.length)
            }
        },
    )
}
