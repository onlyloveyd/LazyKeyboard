package com.gs.keyboard;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;

class KeyboardAttribute {
    public ColorStateList chooserSelectedColor;
    public ColorStateList chooserUnselectedColor;
    public Drawable chooserBackground;
    public Drawable keyboardBackground;
    public boolean isKeyPreview;

    public KeyboardAttribute(ColorStateList chooserSelectedColor, ColorStateList chooserUnselectedColor, Drawable chooserBackground, Drawable keyboardBackground, boolean isKeyPreview) {
        this.chooserSelectedColor = chooserSelectedColor;
        this.chooserUnselectedColor = chooserUnselectedColor;
        this.chooserBackground = chooserBackground;
        this.keyboardBackground = keyboardBackground;
        this.isKeyPreview = isKeyPreview;
    }
}
