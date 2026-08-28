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
}
