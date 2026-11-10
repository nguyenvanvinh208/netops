package com.example.netops;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/** Nen app: 2 vet sang tron (cyan goc tren phai, tim goc duoi trai) nhu AppBg trong ban Compose. */
public class GlowBackground extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    public GlowBackground(Context c) { super(c); }
    public GlowBackground(Context c, AttributeSet a) { super(c, a); }
    public GlowBackground(Context c, AttributeSet a, int s) { super(c, a, s); }

    private void glow(Canvas cv, float cx, float cy, float rad, int color, float a) {
        float d = UI.density();
        p.setShader(new RadialGradient(cx * d, cy * d, rad * d,
                new int[]{UI.alpha(color, a * 0.8f), UI.alpha(color, a * 0.5f), UI.alpha(color, 0f)},
                new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
        cv.drawCircle(cx * d, cy * d, rad * d, p);
    }

    @Override protected void onDraw(Canvas cv) {
        cv.drawColor(C.BG);
        glow(cv, 370f, 20f, 300f, C.CYAN, 0.20f);
        glow(cv, 20f, 680f, 290f, C.VIOLET, 0.14f);
    }
}
