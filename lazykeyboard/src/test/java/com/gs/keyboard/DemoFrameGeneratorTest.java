package com.gs.keyboard;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

/**
 * 生成 README 演示 GIF 的帧序列（Robolectric NATIVE 图形模式下渲染真实控件）。
 * 仅在设置环境变量 DEMO_FRAMES_DIR 时执行，CI 与常规测试自动跳过：
 *   DEMO_FRAMES_DIR=/tmp/frames ./gradlew :lazykeyboard:testDebugUnitTest --tests '*DemoFrameGenerator*'
 * 帧序列：字母键盘输入 → 长按删除连发 → 数字键盘（乱序）→ 符号键盘 → 完成。
 */
@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = 34, qualifiers = "w411dp-h860dp-420dpi")
public class DemoFrameGeneratorTest {

    private static final float LETTER_KEY = 0.08425f;
    private static final float SYMBOL_KEY = 0.094f;
    private static final float NUMBER_KEY = 0.305344f;
    private static final float GAP_DP = 4f;

    /** 各键盘每行的（首键前导 gap dp，行内键宽比例表），与 res/xml 布局文件一一对应。 */
    private static final float[][] LETTER_ROWS = {
            {GAP_DP, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY},
            {GAP_DP + 4.999995f, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY},
            {GAP_DP, 0.13099998f, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, LETTER_KEY, 0.13099998f},
            {GAP_DP, 0.75299998f, 0.18000004f},
    };
    private static final float[][] NUMBER_ROWS = {
            {GAP_DP, NUMBER_KEY, NUMBER_KEY, NUMBER_KEY},
            {GAP_DP, NUMBER_KEY, NUMBER_KEY, NUMBER_KEY},
            {GAP_DP, NUMBER_KEY, NUMBER_KEY, NUMBER_KEY},
            {GAP_DP, NUMBER_KEY, NUMBER_KEY, NUMBER_KEY},
    };
    private static final float[][] SYMBOL_ROWS = {
            {GAP_DP, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY},
            {GAP_DP, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY},
            {GAP_DP, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, 0.20209998f},
            {GAP_DP, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, SYMBOL_KEY, 0.18000004f},
    };

    private int frameIndex = 0;
    private Activity activity;
    private KeyboardDialog dialog;
    private SecurityEditText editText;
    private SecurityKeyboardView keyboardView;
    private String outputDir;

    @Test
    public void generateDemoFrames() {
        outputDir = System.getenv("DEMO_FRAMES_DIR");
        Assume.assumeNotNull(outputDir);
        //noinspection ResultOfMethodCallIgnored
        new File(outputDir).mkdirs();

        init();

        // 字母键盘输入 lazy（'a' 行有 4.999995%p 的额外前导缩进）
        capture();
        tap(LETTER_ROWS[1], 4f + 4.999995f * contentWidth() / density(), 8);
        tap(LETTER_ROWS[1], 4f + 4.999995f * contentWidth() / density(), 0);
        tap(LETTER_ROWS[2], 4f, 1);
        tap(LETTER_ROWS[0], 4f, 5);

        // 长按删除：按住期间连发
        int deleteX = keyCenter(LETTER_ROWS[2], 4f, 8);
        int deleteY = rowCenterY(2);
        dispatch(MotionEvent.ACTION_DOWN, deleteX, deleteY);
        SystemClock.sleep(470); // 按下立即触发一次 + 400ms 起始 + 若干 50ms 连发
        capture();
        dispatch(MotionEvent.ACTION_UP, deleteX, deleteY);

        // 数字键盘（每次切换乱序）
        keyboardSwitch(R.id.tv_number);
        capture();
        tap(NUMBER_ROWS[0], 4f, 0);
        tap(NUMBER_ROWS[1], 4f, 1);

        // 符号键盘
        keyboardSwitch(R.id.tv_symbol);
        capture();
        tap(SYMBOL_ROWS[1], 4f, 3);

        // 回字母并完成
        keyboardSwitch(R.id.tv_letter);
        capture();
        tap(LETTER_ROWS[3], 4f, 1); // 完成
        capture();
    }

    private void init() {
        activity = Robolectric.setupActivity(Activity.class);
        editText = new SecurityEditText(activity);
        editText.setHint("密码");
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        // 禁止自动聚焦：避免 SecurityEditText 自行弹出第二个键盘对话框
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);

        LinearLayout host = new LinearLayout(activity);
        host.setOrientation(LinearLayout.VERTICAL);
        host.setPadding(dp(24), dp(72), dp(24), 0);
        host.addView(editText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        FrameLayout root = new FrameLayout(activity);
        root.addView(host, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        activity.setContentView(root);

        measureActivityDecor();
        dialog = new KeyboardDialog(activity, editText);
        dialog.show();
        // 触发一次 decor 测量，拿到键盘视图的真实锚点
        measureDialogDecor();
        keyboardView = dialog.findViewById(R.id.keyboard_view);
    }

    private void tap(float[] rowSpec, float leadingGapDp, int index) {
        int x = keyCenter(rowSpec, leadingGapDp, index);
        int y = rowCenterY(rowIndex(rowSpec));
        dispatch(MotionEvent.ACTION_DOWN, x, y);
        dispatch(MotionEvent.ACTION_UP, x, y);
        capture();
    }

    private int rowIndex(float[] rowSpec) {
        for (int r = 0; r < LETTER_ROWS.length; r++) {
            if (rowSpec == LETTER_ROWS[r]) return r;
        }
        for (int r = 0; r < NUMBER_ROWS.length; r++) {
            if (rowSpec == NUMBER_ROWS[r]) return r;
        }
        for (int r = 0; r < SYMBOL_ROWS.length; r++) {
            if (rowSpec == SYMBOL_ROWS[r]) return r;
        }
        throw new IllegalArgumentException("unknown row");
    }

    private void keyboardSwitch(int textViewId) {
        dialog.findViewById(textViewId).performClick();
        measureDialogDecor(); // 切换键盘后重新测量锚点
    }

    private void dispatch(int action, float x, float y) {
        MotionEvent event = MotionEvent.obtain(0, 0, action, x, y, 0);
        keyboardView.dispatchTouchEvent(event);
        event.recycle();
    }

    /** 与 KeyboardLayout#layout 相同的几何算法：按行宽表与居中平移计算按键中心。 */
    private int keyCenter(float[] rowSpec, float leadingGapDp, int index) {
        int gap = dp(4);
        int[] widths = new int[rowSpec.length - 1];
        int advance = 0;
        for (int i = 0; i < widths.length; i++) {
            widths[i] = Math.round(rowSpec[i + 1] * contentWidth());
            advance += gap + widths[i];
        }
        int leadingGap = Math.round(leadingGapDp * density());
        int x = leadingGap;
        for (int i = 0; i < index; i++) {
            x += gap + widths[i];
        }
        int slack = contentWidth() - advance - leadingGap;
        int shift = Math.max(0, slack / 2);
        return anchorX() + shift + x + widths[index] / 2;
    }

    private int rowCenterY(int row) {
        int keyHeight = dp(48);
        int gap = dp(4);
        return anchorY() + gap + row * (keyHeight + gap) + keyHeight / 2;
    }

    private void measureActivityDecor() {
        int screenW = activity.getResources().getDisplayMetrics().widthPixels;
        int screenH = activity.getResources().getDisplayMetrics().heightPixels;
        View decor = activity.getWindow().getDecorView();
        decor.measure(View.MeasureSpec.makeMeasureSpec(screenW, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(screenH, View.MeasureSpec.EXACTLY));
        decor.layout(0, 0, screenW, screenH);
    }

    private void measureDialogDecor() {
        int screenW = activity.getResources().getDisplayMetrics().widthPixels;
        View decor = dialog.getWindow().getDecorView();
        decor.measure(View.MeasureSpec.makeMeasureSpec(screenW, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        decor.layout(0, 0, screenW, decor.getMeasuredHeight());
    }

    private int anchorX() {
        return keyboardView.getLeft() + keyboardView.getPaddingLeft();
    }

    private int anchorY() {
        return keyboardView.getTop() + keyboardView.getPaddingTop();
    }

    private int contentWidth() {
        return keyboardView.getWidth() - keyboardView.getPaddingLeft() - keyboardView.getPaddingRight();
    }

    private void capture() {
        int screenW = activity.getResources().getDisplayMetrics().widthPixels;
        int screenH = activity.getResources().getDisplayMetrics().heightPixels;
        Bitmap bitmap = Bitmap.createBitmap(screenW, screenH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xFF1B1F27); // 示例页背景色

        activity.getWindow().getDecorView().draw(canvas);

        if (dialog.isShowing()) {
            View decor = dialog.getWindow().getDecorView();
            canvas.save();
            canvas.translate(0, screenH - decor.getMeasuredHeight());
            decor.draw(canvas);
            canvas.restore();

        }

        File out = new File(outputDir, String.format(Locale.US, "frame_%02d.png", frameIndex++));
        try (FileOutputStream fos = new FileOutputStream(out)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            throw new IllegalStateException("failed to write frame " + out, e);
        }
    }

    private float density() {
        return activity.getResources().getDisplayMetrics().density;
    }

    private int dp(int value) {
        return Math.round(value * density());
    }
}
