package com.gs.keyboard.compose

import android.text.InputType
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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
        },
    )
}
