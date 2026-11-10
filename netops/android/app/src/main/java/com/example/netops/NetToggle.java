package com.example.netops;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/** Cong tat 48x28: nen gradient khi bat, vien xam khi tat, nut trang truot 160ms. */
public class NetToggle extends View {
    public interface OnChange { void on(boolean checked); }

    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumb = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Shader grad;
    private boolean on;
    private float knob;
    private OnChange listener;

    public NetToggle(Context c) { this(c, null); }
    public NetToggle(Context c, AttributeSet a) { this(c, a, 0); }
    public NetToggle(Context c, AttributeSet a, int s) {
        super(c, a, s);
        if (a != null) {
            TypedArray t = c.obtainStyledAttributes(a, R.styleable.NetToggle);
            on = t.getBoolean(R.styleable.NetToggle_toggleOn, false);
            t.recycle();
        }
        knob = on ? 1f : 0f;
        thumb.setColor(0xFFFFFFFF);
        setClickable(true);
        super.setOnClickListener(v -> {
            on = !on;
            animateTo(on ? 1f : 0f);
            if (listener != null) listener.on(on);
        });
    }

    public void setOnChange(OnChange l) { listener = l; }
    public boolean isChecked() { return on; }
    public void setChecked(boolean b) { on = b; knob = b ? 1f : 0f; invalidate(); }

    private void animateTo(float to) {
        ValueAnimator va = ValueAnimator.ofFloat(knob, to);
        va.setDuration(160);
        va.addUpdateListener(an -> { knob = (float) an.getAnimatedValue(); invalidate(); });
        va.start();
    }

    @Override protected void onMeasure(int w, int h) { setMeasuredDimension(UI.dp(48), UI.dp(28)); }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        grad = new LinearGradient(0, 0, w, 0, C.CYAN, C.BLUE, Shader.TileMode.CLAMP);
    }

    @Override protected void onDraw(Canvas cv) {
        float w = getWidth(), h = getHeight();
        if (on) { track.setShader(grad); track.setColor(0xFFFFFFFF); }
        else { track.setShader(null); track.setColor(C.STROKE); }
        cv.drawRoundRect(0, 0, w, h, h / 2f, h / 2f, track);
        float x = UI.dp(3) + knob * UI.dp(20);
        cv.drawCircle(x + UI.dp(11), UI.dp(3) + UI.dp(11), UI.dp(11), thumb);
    }
}
