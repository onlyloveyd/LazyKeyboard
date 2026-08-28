package com.gs.keyboard.compose

import com.gs.keyboard.OnSecurityKeyListener
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Compose 状态镜像逻辑测试：按键序列如何映射到可观察的 text 状态。
 */
class SecurityInputStateTest {

    @Test
    fun printableKeys_appendCharacters() {
        val state = SecurityInputState()
        "lazy".forEach { state.dispatchKey(it.code) }
        assertEquals("lazy", state.text)
    }

    @Test
    fun deleteKey_removesLastCharacter() {
        val state = SecurityInputState("ab")
        state.dispatchKey(OnSecurityKeyListener.KEYCODE_DELETE)
        assertEquals("a", state.text)
    }

    @Test
    fun deleteKey_onEmptyState_isNoOp() {
        val state = SecurityInputState()
        state.dispatchKey(OnSecurityKeyListener.KEYCODE_DELETE)
        assertEquals("", state.text)
    }

    @Test
    fun functionalKeys_doNotChangeText() {
        val state = SecurityInputState("x")
        state.dispatchKey(OnSecurityKeyListener.KEYCODE_SHIFT)
        state.dispatchKey(OnSecurityKeyListener.KEYCODE_CANCEL)
        state.dispatchKey(OnSecurityKeyListener.KEYCODE_DONE)
        assertEquals("x", state.text)
    }

    @Test
    fun clear_emptiesText() {
        val state = SecurityInputState("secret")
        state.clear()
        assertEquals("", state.text)
    }

    @Test
    fun saver_restoresText_acrossConfigurationChange() {
        // 旋转屏幕后 rememberSaveable 通过 Saver.restore 重建状态，内容不应丢失
        val restored = SecurityInputState.SAVER.restore("lzy521")
        assertEquals("lzy521", restored?.text)
        // 后续按键继续在恢复后的内容上追加
        restored?.dispatchKey('0'.code)
        assertEquals("lzy5210", restored?.text)
    }
}
