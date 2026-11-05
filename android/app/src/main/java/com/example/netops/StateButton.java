package com.example.netops;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Nut 2 trang thai: idle -> done (xanh) roi tu quay lai sau 2.6s. Box = LinearLayout [ImageView, TextView]. */
public class StateButton {
    private final View box;
    private final ImageView icon;
    private final TextView label;
    private final float r;
    private String idleIcon, idleText, doneText;
    private int idleIconColor, idleTextColor;
    private float idleSp, doneSp = 15;
    private Drawable idleBg;
    private int glowColor;
    private float glowElev;
    private boolean done;
    private Runnable fire;

    public StateButton(View box, float rDp) {
        this.box = box;
        this.r = rDp;
        LinearLayout l = (LinearLayout) box;
        icon = (ImageView) l.getChildAt(0);
        label = (TextView) l.getChildAt(1);
        box.setOnClickListener(v -> {
            if (done) return;
            done = true;
            applyDone();
            if (fire != null) fire.run();
            box.postDelayed(() -> { done = false; applyIdle(); }, 2600);
        });
    }

    public StateButton idle(String iconName, int iconColor, String text, int textColor, float sp, Drawable bg) {
        idleIcon = iconName; idleIconColor = iconColor; idleText = text; idleTextColor = textColor; idleSp = sp; idleBg = bg;
        applyIdle();
        return this;
    }

    public StateButton glow(int color, float elev) { glowColor = color; glowElev = elev; applyIdle(); return this; }
    public StateButton done(String text, float sp) { doneText = text; doneSp = sp; return this; }
    public StateButton onFire(Runnable run) { fire = run; return this; }

    /** Cap nhat chu khi du lieu doi (vd doi DB). */
    public void setIdleText(String t) { idleText = t; if (!done) label.setText(t); }
    public void setDoneText(String t) { doneText = t; if (done) label.setText(t); }

    private void applyIdle() {
        UI.icon(icon, idleIcon, idleIconColor);
        label.setText(idleText);
        label.setTextColor(idleTextColor);
        label.setTextSize(idleSp);
        box.setBackground(idleBg);
        if (glowElev > 0) UI.glow(box, glowColor, glowElev, r); else UI.noGlow(box);
    }

    private void applyDone() {
        UI.icon(icon, "check", C.GREEN);
        label.setText(doneText);
        label.setTextColor(C.GREEN);
        label.setTextSize(doneSp);
        box.setBackground(UI.rect(UI.alpha(C.GREEN, 0.14f), UI.alpha(C.GREEN, 0.5f), 1, r));
        UI.noGlow(box);
    }
}
