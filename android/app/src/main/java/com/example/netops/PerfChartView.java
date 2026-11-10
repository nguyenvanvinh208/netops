package com.example.netops;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/** Bieu do Performance: 2 duong CPU (cyan) + RAM (tim), toa do lay nguyen tu Figma. */
public class PerfChartView extends View {
    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillP = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lineP = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float[] cpu = new float[0], ram = new float[0];
    private float cpuTop, ramTop;
    private final Paint axis = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dash = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint heal = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PerfChartView(Context c) { this(c, null); }
    public PerfChartView(Context c, AttributeSet a) { this(c, a, 0); }
    public PerfChartView(Context c, AttributeSet a, int s) {
        super(c, a, s);
        float d = UI.density();
        grid.setColor(C.STROKE); grid.setStrokeWidth(d); grid.setStyle(Paint.Style.STROKE);
        axis.setColor(C.DIM); axis.setTextSize(9f * d);
        dash.setColor(UI.alpha(C.RED, 0.7f)); dash.setStyle(Paint.Style.STROKE); dash.setStrokeWidth(d);
        dash.setPathEffect(new android.graphics.DashPathEffect(new float[]{4f * d, 3f * d}, 0));
        heal.setColor(C.RED); heal.setTextSize(9f * d); heal.setTextAlign(Paint.Align.RIGHT);
        fillP.setStyle(Paint.Style.FILL);
        lineP.setStyle(Paint.Style.STROKE); lineP.setStrokeWidth(2.5f * d); lineP.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setData(float[] cpu, float cpuTop, float[] ram, float ramTop) {
        this.cpu = cpu; this.cpuTop = cpuTop; this.ram = ram; this.ramTop = ramTop;
        invalidate();
    }

    private void series(Canvas cv, float[] ys, float top, int color, float w, float h, float d) {
        int n = ys.length;
        if (n < 2) return;
        float dx = w / (n - 1);
        float[] px = new float[n], py = new float[n];
        float minY = Float.MAX_VALUE;
        for (int i = 0; i < n; i++) { px[i] = i * dx; py[i] = (top + ys[i]) * d; minY = Math.min(minY, py[i]); }
        Path line = new Path();
        line.moveTo(px[0], py[0]);
        for (int i = 0; i < n - 1; i++) line.cubicTo(px[i] + dx / 2f, py[i], px[i + 1] - dx / 2f, py[i + 1], px[i + 1], py[i + 1]);
        Path area = new Path(line);
        area.lineTo(w, h); area.lineTo(0, h); area.close();
        fillP.setShader(new LinearGradient(0, minY, 0, h, UI.alpha(color, 0.32f), UI.alpha(color, 0f), Shader.TileMode.CLAMP));
        cv.drawPath(area, fillP);
        lineP.setColor(color);
        cv.drawPath(line, lineP);
    }

    @Override protected void onDraw(Canvas cv) {
        float d = UI.density(), w = getWidth(), h = getHeight();
        for (int i = 0; i < 4; i++) { float y = (4f + i * 60.67f) * d; cv.drawLine(0, y, w, y, grid); }
        series(cv, cpu, cpuTop, C.CYAN, w, h, d);
        series(cv, ram, ramTop, C.VIOLET, w, h, d);
        String[] yl = {"100%", "67%", "33%", "0%"};
        float[] yt = {6f, 67f, 127f, 175f};
        for (int i = 0; i < 4; i++) cv.drawText(yl[i], 0, (yt[i] + 8f) * d, axis);
        float ty = 31.3f * d;
        Path th = new Path(); th.moveTo(0, ty); th.lineTo(w, ty);
        cv.drawPath(th, dash);
        cv.drawText("auto-heal 85%", w, 28f * d, heal);
    }
}
