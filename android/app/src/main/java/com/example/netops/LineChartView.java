package com.example.netops;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/** Bieu do duong + vung gradient (4 duong luoi, net 2.5dp). */
public class LineChartView extends View {
    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillP = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lineP = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float[] values = new float[0];
    private int color = C.CYAN;
    private float lo = 0f, hi = 100f;

    public LineChartView(Context c) { this(c, null); }
    public LineChartView(Context c, AttributeSet a) { this(c, a, 0); }
    public LineChartView(Context c, AttributeSet a, int s) {
        super(c, a, s);
        float d = UI.density();
        grid.setColor(C.STROKE); grid.setStrokeWidth(d); grid.setStyle(Paint.Style.STROKE);
        fillP.setStyle(Paint.Style.FILL);
        lineP.setStyle(Paint.Style.STROKE); lineP.setStrokeWidth(2.5f * d); lineP.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setData(float[] values, int color, float lo, float hi) {
        this.values = values; this.color = color; this.lo = lo; this.hi = hi;
        invalidate();
    }

    @Override protected void onDraw(Canvas cv) {
        float d = UI.density(), w = getWidth(), h = getHeight(), pad = 6f * d;
        for (int i = 0; i < 4; i++) {
            float y = 4f * d + i * (h - 8f * d) / 3f;
            cv.drawLine(0, y, w, y, grid);
        }
        int n = values.length;
        if (n < 2) return;
        PointF[] p = new PointF[n];
        float top = Float.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            float y = pad + (h - 2 * pad) * (1f - (values[i] - lo) / (hi - lo));
            p[i] = new PointF(i * w / (n - 1), y);
            top = Math.min(top, y);
        }
        Path line = new Path();
        line.moveTo(p[0].x, p[0].y);
        for (int i = 0; i < n - 1; i++) {
            PointF p0 = p[Math.max(0, i - 1)], p1 = p[i], p2 = p[i + 1], p3 = p[Math.min(n - 1, i + 2)];
            line.cubicTo(p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f,
                    p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f, p2.x, p2.y);
        }
        Path area = new Path(line);
        area.lineTo(w, h); area.lineTo(0, h); area.close();
        fillP.setShader(new LinearGradient(0, top, 0, h, UI.alpha(color, 0.32f), UI.alpha(color, 0f), Shader.TileMode.CLAMP));
        cv.drawPath(area, fillP);
        lineP.setColor(color);
        cv.drawPath(line, lineP);
    }
}
