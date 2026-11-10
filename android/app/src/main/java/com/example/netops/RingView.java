package com.example.netops;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** Vong tron tien do (track + cung mau, dau bo tron, bat dau tu 12h). */
public class RingView extends View {
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arc = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private float progress;

    public RingView(Context c) { this(c, null); }
    public RingView(Context c, AttributeSet a) { this(c, a, 0); }
    public RingView(Context c, AttributeSet a, int s) {
        super(c, a, s);
        int color = C.CYAN, trackColor = C.STROKE;
        float stroke = UI.dp(6);
        if (a != null) {
            TypedArray t = c.obtainStyledAttributes(a, R.styleable.RingView);
            color = t.getColor(R.styleable.RingView_ringColor, color);
            trackColor = t.getColor(R.styleable.RingView_ringTrack, trackColor);
            stroke = t.getDimension(R.styleable.RingView_ringStroke, stroke);
            progress = t.getFloat(R.styleable.RingView_ringProgress, 0f);
            t.recycle();
        }
        track.setStyle(Paint.Style.STROKE); arc.setStyle(Paint.Style.STROKE);
        arc.setStrokeCap(Paint.Cap.ROUND);
        track.setColor(trackColor); arc.setColor(color);
        track.setStrokeWidth(stroke); arc.setStrokeWidth(stroke);
    }

    public void setProgress(float p) { progress = Math.max(0f, Math.min(1f, p)); invalidate(); }
    public void setColor(int c) { arc.setColor(c); invalidate(); }

    @Override protected void onDraw(Canvas cv) {
        float in = arc.getStrokeWidth() / 2f;
        rect.set(in, in, getWidth() - in, getHeight() - in);
        cv.drawOval(rect, track);
        if (progress > 0f) cv.drawArc(rect, -90f, 360f * progress, false, arc);
    }
}
