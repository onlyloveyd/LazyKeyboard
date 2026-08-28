package com.gs.keyboard;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 自绘键盘布局解析与几何计算测试。
 * 覆盖：按键数量、百分比/固定尺寸解析、行堆叠、命中测试、
 * 可连击/修饰键标记、label 与 codes 的运行时可变性（随机数字、大小写切换依赖）。
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class KeyboardLayoutTest {

    private static final int BASE_WIDTH = 1080;
    private final Context context = ApplicationProvider.getApplicationContext();

    private KeyboardLayout english;
    private KeyboardLayout number;
    private final float density = context.getResources().getDisplayMetrics().density;

    @Before
    public void setUp() {
        english = KeyboardLayout.parse(context, R.xml.gs_keyboard_english);
        number = KeyboardLayout.parse(context, R.xml.gs_keyboard_number);
        english.layout(BASE_WIDTH);
        number.layout(BASE_WIDTH);
    }

    @Test
    public void englishKeyboard_containsAllKeys() {
        // 10 + 9 + 9 + (shift+7+delete) + (space+done)
        assertEquals(30, english.getKeys().size());
    }

    @Test
    public void keyWidth_resolvesAsFractionOfBaseWidth() {
        KeyboardLayout.Key firstKey = english.getKeys().get(0);
        // app:keyWidth="8.42500%p"
        assertEquals(Math.round(0.08425f * BASE_WIDTH), firstKey.getWidth());
    }

    @Test
    public void firstKey_isOffsetByHorizontalGap() {
        KeyboardLayout.Key firstKey = english.getKeys().get(0);
        assertEquals(Math.round(4 * density), firstKey.getX());
    }

    @Test
    public void rows_stackWithVerticalGap() {
        int rowHeight = Math.round(48 * density);
        int verticalGap = Math.round(4 * density);
        // 4 行：4 * rowHeight + 3 * verticalGap
        assertEquals(4 * rowHeight + 3 * verticalGap, english.getTotalHeight());
    }

    @Test
    public void deleteKey_isRepeatable_shiftKeyIsModifier() {
        KeyboardLayout.Key delete = findKeyByCode(english, KeyboardLayout.KEYCODE_DELETE);
        assertNotNull(delete);
        assertTrue(delete.repeatable);

        KeyboardLayout.Key shift = findKeyByCode(english, KeyboardLayout.KEYCODE_SHIFT);
        assertNotNull(shift);
        assertTrue(shift.modifier);
        assertTrue(shift.sticky);
    }

    @Test
    public void spaceKey_hasEmptyLabel_doneKeyHasLabel() {
        KeyboardLayout.Key space = findKeyByCode(english, ' ');
        assertNotNull(space);
        assertEquals("", space.label.toString());

        KeyboardLayout.Key done = findKeyByCode(english, KeyboardLayout.KEYCODE_CANCEL);
        assertNotNull(done);
        assertEquals("完成", done.label.toString());
    }

    @Test
    public void hitTest_findsKeyInsideBounds() {
        KeyboardLayout.Key firstKey = english.getKeys().get(0);
        int centerX = firstKey.getX() + firstKey.getWidth() / 2;
        int centerY = firstKey.getY() + firstKey.getHeight() / 2;
        assertEquals(0, english.getKeyIndexForPosition(centerX, centerY));
        assertEquals(-1, english.getKeyIndexForPosition(-100, -100));
    }

    @Test
    public void keyLabelAndCodes_areMutableAtRuntime() {
        KeyboardLayout.Key key = number.getKeys().get(0);
        int originalX = key.getX();
        int originalWidth = key.getWidth();
        // 模拟随机数字：只改 label/codes，几何不受影响
        key.label = "9";
        key.codes[0] = 57;
        assertEquals("9", key.label.toString());
        assertEquals(57, key.getPrimaryCode());
        assertEquals(originalX, key.getX());
        assertEquals(originalWidth, key.getWidth());
    }

    @Test
    public void numberKeyboard_hasTwelveKeys() {
        assertEquals(12, number.getKeys().size());
    }

    private static KeyboardLayout.Key findKeyByCode(KeyboardLayout layout, int code) {
        List<KeyboardLayout.Key> keys = layout.getKeys();
        for (KeyboardLayout.Key key : keys) {
            if (key.getPrimaryCode() == code) {
                return key;
            }
        }
        return null;
    }
}
