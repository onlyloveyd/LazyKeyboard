package com.gs.keyboard;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.util.Xml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 自绘键盘的布局模型与解析器，替代已废弃的 android.inputmethodservice.Keyboard。
 * <p>
 * 解析 res/xml 下的键盘布局（{@code <Keyboard>/<Row>/<Key>}），保留对既有
 * gs_keyboard_*.xml 布局格式的兼容；与框架实现的两点差异：
 * <ul>
 * <li>百分比尺寸（如 {@code 8.42500%p}）以键盘视图实际内容宽度为基准解析，
 *     而不是屏幕宽度，因此键盘在任何宽度的容器中都能正确铺满；</li>
 * <li>几何位置不在解析时固化，而是在 {@link #layout(int)} 时按传入的基准宽度计算，
 *     度量变化（如旋转屏幕）后重新布局即可。</li>
 * </ul>
 */
public class KeyboardLayout {

    public static final int KEYCODE_SHIFT = -1;
    public static final int KEYCODE_MODE_CHANGE = -2;
    public static final int KEYCODE_CANCEL = -3;
    public static final int KEYCODE_DONE = -4;
    public static final int KEYCODE_DELETE = -5;

    private final List<Key> mKeys = new ArrayList<>();
    private final List<Row> mRows = new ArrayList<>();
    private int mTotalWidth = 0;
    private int mTotalHeight = 0;

    /**
     * 解析键盘布局资源。返回的布局尚未计算几何位置，
     * 使用前需调用 {@link #layout(int)}。
     */
    @NonNull
    public static KeyboardLayout parse(@NonNull Context context, @XmlRes int xmlLayoutResId) {
        KeyboardLayout keyboard = new KeyboardLayout();
        Resources res = context.getResources();
        try (XmlResourceParser parser = res.getXml(xmlLayoutResId)) {
            int event;
            Row currentRow = null;
            Spec defaultWidth = Spec.fraction(0.1f);
            Spec defaultHeight = Spec.dimension(dp(context, 48));
            Spec defaultHorizontalGap = Spec.dimension(0);
            Spec defaultVerticalGap = Spec.dimension(0);

            while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    String tag = parser.getName();
                    if ("Keyboard".equals(tag)) {
                        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                                R.styleable.KeyboardLayout);
                        defaultWidth = readSpec(a, R.styleable.KeyboardLayout_keyWidth, defaultWidth);
                        defaultHeight = readSpec(a, R.styleable.KeyboardLayout_keyHeight, defaultHeight);
                        defaultHorizontalGap = readSpec(a,
                                R.styleable.KeyboardLayout_horizontalGap, defaultHorizontalGap);
                        defaultVerticalGap = readSpec(a,
                                R.styleable.KeyboardLayout_verticalGap, defaultVerticalGap);
                        a.recycle();
                    } else if ("Row".equals(tag)) {
                        currentRow = new Row();
                        currentRow.defaultWidth = defaultWidth;
                        currentRow.defaultHeight = defaultHeight;
                        currentRow.defaultHorizontalGap = defaultHorizontalGap;
                        currentRow.verticalGap = defaultVerticalGap;
                        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                                R.styleable.KeyboardLayout);
                        currentRow.defaultWidth = readSpec(a,
                                R.styleable.KeyboardLayout_keyWidth, currentRow.defaultWidth);
                        currentRow.defaultHeight = readSpec(a,
                                R.styleable.KeyboardLayout_keyHeight, currentRow.defaultHeight);
                        currentRow.defaultHorizontalGap = readSpec(a,
                                R.styleable.KeyboardLayout_horizontalGap, currentRow.defaultHorizontalGap);
                        currentRow.verticalGap = readSpec(a,
                                R.styleable.KeyboardLayout_verticalGap, currentRow.verticalGap);
                        a.recycle();
                        keyboard.mRows.add(currentRow);
                    } else if ("Key".equals(tag)) {
                        if (currentRow == null) {
                            // 允许布局文件省略 <Row> 直接堆 <Key>，按单行兜底
                            currentRow = new Row();
                            currentRow.defaultWidth = defaultWidth;
                            currentRow.defaultHeight = defaultHeight;
                            currentRow.defaultHorizontalGap = defaultHorizontalGap;
                            currentRow.verticalGap = defaultVerticalGap;
                            keyboard.mRows.add(currentRow);
                        }
                        Key key = createKeyFromXml(res, currentRow, parser);
                        keyboard.mKeys.add(key);
                        currentRow.keys.add(key);
                    }
                }
            }
        } catch (XmlPullParserException | IOException e) {
            throw new IllegalArgumentException(
                    "Malformed keyboard layout resource: " + res.getResourceName(xmlLayoutResId), e);
        }
        return keyboard;
    }

    @NonNull
    private static Key createKeyFromXml(Resources res, Row parent, XmlResourceParser parser) {
        Key key = new Key();
        key.row = parent;
        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.KeyboardLayout);
        key.widthSpec = readSpec(a, R.styleable.KeyboardLayout_keyWidth, null);
        key.heightSpec = readSpec(a, R.styleable.KeyboardLayout_keyHeight, null);
        key.gapSpec = readSpec(a, R.styleable.KeyboardLayout_horizontalGap, null);

        TypedValue codesValue = new TypedValue();
        a.getValue(R.styleable.KeyboardLayout_codes, codesValue);
        if (codesValue.type == TypedValue.TYPE_INT_DEC
                || codesValue.type == TypedValue.TYPE_INT_HEX) {
            key.codes = new int[]{codesValue.data};
        } else if (codesValue.type == TypedValue.TYPE_STRING) {
            key.codes = parseCsv(codesValue.string.toString());
        }
        if (key.codes == null || key.codes.length == 0) {
            a.recycle();
            throw new IllegalArgumentException("Key must declare codes");
        }
        key.label = a.getText(R.styleable.KeyboardLayout_keyLabel);
        key.icon = a.getDrawable(R.styleable.KeyboardLayout_keyIcon);
        key.repeatable = a.getBoolean(R.styleable.KeyboardLayout_isRepeatable, false);
        key.modifier = a.getBoolean(R.styleable.KeyboardLayout_isModifier, false);
        key.sticky = a.getBoolean(R.styleable.KeyboardLayout_isSticky, false);
        a.recycle();
        return key;
    }

    private static int[] parseCsv(String value) {
        String[] tokens = value.split(",");
        int[] codes = new int[tokens.length];
        int count = 0;
        for (String token : tokens) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                codes[count++] = Integer.parseInt(trimmed);
            }
        }
        if (count == 0) {
            return null;
        }
        int[] result = new int[count];
        System.arraycopy(codes, 0, result, 0, count);
        return result;
    }

    @Nullable
    private static Spec readSpec(TypedArray a, int index, @Nullable Spec def) {
        TypedValue value = a.peekValue(index);
        if (value == null) {
            return def;
        }
        if (value.type == TypedValue.TYPE_DIMENSION) {
            return Spec.dimension(a.getDimensionPixelSize(index, 0));
        } else if (value.type == TypedValue.TYPE_FRACTION) {
            // base 与 parentBase 均取 1，直接得到比例值
            return Spec.fraction(a.getFraction(index, 1, 1, 0f));
        }
        return def;
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    /**
     * 按基准宽度（键盘视图的内容宽度）计算全部按键的几何位置。
     * 按键的 label/codes 在此之后仍可修改（随机数字、大小写切换），互不影响。
     */
    public void layout(int baseWidth) {
        int y = 0;
        mTotalWidth = 0;
        for (Row row : mRows) {
            int rowHeight = row.defaultHeight.resolve(baseWidth);
            int verticalGap = row.verticalGap.resolve(baseWidth);
            int x = 0;
            for (Key key : row.keys) {
                Spec widthSpec = key.widthSpec != null ? key.widthSpec : row.defaultWidth;
                Spec gapSpec = key.gapSpec != null ? key.gapSpec : row.defaultHorizontalGap;
                Spec heightSpec = key.heightSpec != null ? key.heightSpec : row.defaultHeight;
                int gap = gapSpec.resolve(baseWidth);
                key.x = x + gap;
                key.y = y;
                key.width = widthSpec.resolve(baseWidth);
                key.height = heightSpec.resolve(baseWidth);
                x += gap + key.width;
            }
            centerRow(row, baseWidth, x);
            int rowRightEdge = row.keys.isEmpty() ? 0
                    : row.keys.get(row.keys.size() - 1).x + row.keys.get(row.keys.size() - 1).width;
            mTotalWidth = Math.max(mTotalWidth, rowRightEdge);
            y += verticalGap + rowHeight;
        }
        if (!mRows.isEmpty()) {
            y -= mRows.get(mRows.size() - 1).verticalGap.resolve(baseWidth);
        }
        mTotalHeight = y;
    }

    /**
     * 行内居中：按键百分比之和通常小于内容宽度（例如按屏幕宽度调校的布局
     * 在扣除视图内边距后会整体缩水），把余量平分到左右两侧，
     * 保证键盘整体看起来居中。行宽超出基准时不平移。
     */
    private static void centerRow(Row row, int baseWidth, int advance) {
        if (row.keys.isEmpty()) {
            return;
        }
        int leadingGap = row.keys.get(0).x;
        int slack = baseWidth - advance - leadingGap;
        if (slack > 0) {
            int shift = slack / 2;
            for (Key key : row.keys) {
                key.x += shift;
            }
        }
    }

    /** 全部按键，按布局文件中的出现顺序排列。 */
    @NonNull
    public List<Key> getKeys() {
        return mKeys;
    }

    public int getTotalWidth() {
        return mTotalWidth;
    }

    public int getTotalHeight() {
        return mTotalHeight;
    }

    /** 命中测试：返回包含坐标的按键，未命中返回 -1。 */
    public int getKeyIndexForPosition(float x, float y) {
        for (int i = 0; i < mKeys.size(); i++) {
            Key key = mKeys.get(i);
            if (x >= key.x && x <= key.x + key.width
                    && y >= key.y && y <= key.y + key.height) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 尺寸声明：固定像素或基准宽度的比例，二者取其一。
     */
    private static final class Spec {
        final float fraction;
        final int dimension;

        private Spec(float fraction, int dimension) {
            this.fraction = fraction;
            this.dimension = dimension;
        }

        static Spec fraction(float fraction) {
            return new Spec(fraction, 0);
        }

        static Spec dimension(int px) {
            return new Spec(0f, px);
        }

        int resolve(int baseWidth) {
            return fraction > 0f ? Math.round(fraction * baseWidth) : dimension;
        }
    }

    static final class Row {
        final List<Key> keys = new ArrayList<>();
        Spec defaultWidth;
        Spec defaultHeight;
        Spec defaultHorizontalGap;
        Spec verticalGap;
    }

    /**
     * 单个按键。codes/label/icon 由键盘业务逻辑（随机数字、大小写切换）修改，
     * 几何字段由 {@link KeyboardLayout#layout(int)} 计算。
     */
    public static final class Key {
        public int[] codes;
        @Nullable
        public CharSequence label;
        @Nullable
        public Drawable icon;
        public boolean repeatable;
        public boolean modifier;
        public boolean sticky;

        int x;
        int y;
        int width;
        int height;
        Spec widthSpec;
        Spec heightSpec;
        Spec gapSpec;
        Row row;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getPrimaryCode() {
            return codes[0];
        }
    }
}
