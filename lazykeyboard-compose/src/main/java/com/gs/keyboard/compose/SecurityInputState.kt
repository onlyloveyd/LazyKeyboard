package com.gs.keyboard.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gs.keyboard.OnSecurityKeyListener

/**
 * 安全输入的 Compose 可观察状态：镜像键盘输入内容。
 *
 * 状态由 [SecurityTextField] 的按键回调驱动（见核心库 `OnSecurityKeyListener`，
 * 回调先于输入框文本变化触发），因此 `text` 与输入框内容始终保持一致，
 * 仅通过键盘修改内容的前提下可直接用于提交与校验。
 */
class SecurityInputState(initial: String = "") {

    var text: String by mutableStateOf(initial)
        private set

    internal fun dispatchKey(primaryCode: Int) {
        text = when {
            primaryCode == OnSecurityKeyListener.KEYCODE_DELETE -> text.dropLast(1)
            primaryCode >= 0 -> text + primaryCode.toChar()
            else -> text // shift / 完成 等功能键不改变内容
        }
    }

    fun clear() {
        text = ""
    }
}

/**
 * 创建并记住一个 [SecurityInputState]。
 */
@Composable
fun rememberSecurityInputState(initial: String = ""): SecurityInputState =
    remember { SecurityInputState(initial) }
