package com.gs.keyboard;

import android.graphics.Color;

/**
 * 安全键盘相关配置
 *
 * @author yidong (onlyloveyd@gmaol.com)
 * @date 2018/6/22 07:45
 */
public class SecurityConfigure {

    /**
     * 键盘类型选中颜色
     */
    private int selectedColor = 0xff66aeff;

    /**
     * 键盘类型未选中颜色
     */
    private int unselectedColor = Color.BLACK;

    /**
     * 数字键盘使能
     */
    private boolean isNumberEnabled = true;

    /**
     * 字母键盘使能
     */
    private boolean isLetterEnabled = true;

    /**
     * 符号键盘使能
     */
    private boolean isSymbolEnabled = true;

    /**
     * 默认选中键盘
     */
    private KeyboardType defaultKeyboardType = KeyboardType.LETTER;

    public SecurityConfigure() {
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public SecurityConfigure setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
        return this;
    }

    public int getUnselectedColor() {
        return unselectedColor;
    }

    public SecurityConfigure setUnselectedColor(int unselectedColor) {
        this.unselectedColor = unselectedColor;
        return this;
    }

    public boolean isNumberEnabled() {
        return isNumberEnabled;
    }

    public SecurityConfigure setNumberEnabled(boolean numberEnabled) {
        this.isNumberEnabled = numberEnabled;
        return this;
    }

    public boolean isLetterEnabled() {
        return isLetterEnabled;
    }

    public SecurityConfigure setLetterEnabled(boolean letterEnabled) {
        this.isLetterEnabled = letterEnabled;
        return this;
    }

    public boolean isSymbolEnabled() {
        return isSymbolEnabled;
    }

    public SecurityConfigure setSymbolEnabled(boolean symbolEnabled) {
        this.isSymbolEnabled = symbolEnabled;
        return this;
    }

    public KeyboardType getDefaultKeyboardType() {
        return defaultKeyboardType;
    }

    public SecurityConfigure setDefaultKeyboardType(KeyboardType defaultKeyboardType) {
        this.defaultKeyboardType = defaultKeyboardType;
        return this;
    }
}
