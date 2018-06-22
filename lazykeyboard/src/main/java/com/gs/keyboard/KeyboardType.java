package com.gs.keyboard;

/**
 * KeyboardType 键盘类型
 *
 * @author yidong (onlyloveyd@gmail.com)
 * @date 2018/6/22 08:45
 */
public enum KeyboardType {

    /**
     * 字母键盘
     */
    LETTER(0, "字母"),

    /**
     * 数字键盘
     */
    NUMBER(1, "数字"),

    /**
     * 符号键盘
     */
    SYMBOL(2, "符号");

    private int code;
    private String name;

    KeyboardType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
