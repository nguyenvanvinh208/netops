package com.example.netops;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Ham tien ich dung chung: kich thuoc, shape, glow, icon, chip, segmented... */
public final class UI {
    private UI() {}

    public interface IntCb { void on(int i); }

    public static float density() { return Resources.getSystem().getDisplayMetrics().density; }
    public static int dp(float v) { return Math.round(v * density()); }
    public static int alpha(int color, float a) { return (color & 0x00FFFFFF) | (Math.round(a * 255f) << 24); }

    // ---------- shapes
    public static GradientDrawable rect(int fill, int stroke, float swDp, float rDp) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setCornerRadius(dp(rDp));
        g.setColor(fill);
        if (stroke != 0 && swDp > 0) g.setStroke(Math.max(1, dp(swDp)), stroke);
        return g;
    }

    public static GradientDrawable oval(int fill, int stroke, float swDp) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(fill);
        if (stroke != 0 && swDp > 0) g.setStroke(Math.max(1, dp(swDp)), stroke);
        return g;
    }

    /** Gradient cyan -> xanh duong (ngang hoac cheo). */
    public static GradientDrawable grad(float rDp, boolean diag, boolean isOval) {
        GradientDrawable g = new GradientDrawable(
                diag ? GradientDrawable.Orientation.TL_BR : GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{C.CYAN, C.BLUE});
        g.setShape(isOval ? GradientDrawable.OVAL : GradientDrawable.RECTANGLE);
        if (!isOval) g.setCornerRadius(dp(rDp));
        return g;
    }

    /** Nen mau nhat (alpha 14%) bo goc. */
    public static void tint(View v, int color, float rDp) { v.setBackground(rect(alpha(color, 0.14f), 0, 0, rDp)); }

    public static void pill(TextView t, String text, int color) {
        t.setText(text);
        t.setTextColor(color);
        t.setBackground(rect(alpha(color, 0.14f), 0, 0, 99));
    }

    // ---------- glow (shadow mau) giong Modifier.shadow(ambientColor, spotColor)
    public static void glow(View v, int color, float elevDp, final float rDp) {
        v.setElevation(dp(elevDp));
        if (Build.VERSION.SDK_INT >= 28) {
            v.setOutlineAmbientShadowColor(color);
            v.setOutlineSpotShadowColor(color);
        }
        final float r = dp(rDp);
        v.setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View view, Outline o) {
                o.setRoundRect(0, 0, view.getWidth(), view.getHeight(), r);
                o.setAlpha(1f);
            }
        });
    }

    public static void noGlow(View v) {
        v.setElevation(0);
        if (Build.VERSION.SDK_INT >= 28) {
            v.setOutlineAmbientShadowColor(Color.BLACK);
            v.setOutlineSpotShadowColor(Color.BLACK);
        }
        v.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
    }

    // ---------- icon
    public static int iconRes(Context c, String name) {
        return c.getResources().getIdentifier("ic_" + name, "drawable", c.getPackageName());
    }

    public static void icon(ImageView iv, String name, int color) {
        iv.setImageResource(iconRes(iv.getContext(), name));
        iv.setImageTintList(ColorStateList.valueOf(color));
    }

    public static void iconTint(ImageView iv, int color) { iv.setImageTintList(ColorStateList.valueOf(color)); }

    // ---------- thanh tien do: parent la LinearLayout ngang co weightSum = 1
    public static void fill(View fill, float frac) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) fill.getLayoutParams();
        lp.width = 0;
        lp.weight = Math.max(0f, Math.min(1f, frac));
        fill.setLayoutParams(lp);
    }

    public static View inflate(ViewGroup parent, int layout) {
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
    }

    public static void copy(Context c, String text) {
        ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("netops", text));
    }

    // ---------- segmented + filter chips (item nam trong XML: item_seg_tab / item_chip)
    public static void segmented(LinearLayout row, String[] tabs, int selected, final IntCb cb) {
        row.removeAllViews();
        for (int i = 0; i < tabs.length; i++) {
            TextView t = (TextView) inflate(row, R.layout.item_seg_tab);
            boolean sel = i == selected;
            t.setText(tabs[i]);
            t.setTextColor(sel ? C.ON_CYAN : C.MUTED);
            t.setBackground(sel ? grad(12, false, false) : null);
            final int idx = i;
            t.setOnClickListener(x -> cb.on(idx));
            row.addView(t);
        }
    }

    public static void chips(LinearLayout row, String[] opts, int selected, boolean fillWidth, final IntCb cb) {
        chips(row, opts, selected, fillWidth, 99, cb);
    }

    public static void chips(LinearLayout row, String[] opts, int selected, boolean fillWidth, float radiusDp, final IntCb cb) {
        row.removeAllViews();
        for (int i = 0; i < opts.length; i++) {
            TextView t = (TextView) inflate(row, R.layout.item_chip);
            boolean sel = i == selected;
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) t.getLayoutParams();
            if (fillWidth) { lp.width = 0; lp.weight = 1; }
            t.setLayoutParams(lp);
            t.setText(opts[i]);
            t.setTextColor(sel ? C.CYAN : C.MUTED);
            t.setBackground(rect(sel ? alpha(C.CYAN, 0.16f) : C.CARD, sel ? alpha(C.CYAN, 0.5f) : C.STROKE, 1, radiusDp));
            final int idx = i;
            t.setOnClickListener(x -> cb.on(idx));
            row.addView(t);
        }
    }

    // ---------- o nhap lieu: vien sang + glow khi focus
    public static void bindFocus(final View box, EditText et, final ImageView icon, final boolean force) {
        et.setOnFocusChangeListener((v, f) -> styleInput(box, icon, f || force));
        styleInput(box, icon, force);
    }

    private static void styleInput(View box, ImageView icon, boolean active) {
        if (active) {
            box.setBackground(rect(C.CARD, alpha(C.CYAN, 0.7f), 1.5f, 16));
            glow(box, alpha(C.CYAN, 0.5f), 10, 16);
            iconTint(icon, C.CYAN);
        } else {
            box.setBackground(rect(C.CARD, C.STROKE, 1, 16));
            noGlow(box);
            iconTint(icon, C.MUTED);
        }
    }

    /** Nut con mat hien/an mat khau. */
    public static void passwordToggle(final EditText et, final ImageView eye) {
        final boolean[] show = {false};
        applyPw(et, eye, false);
        eye.setOnClickListener(v -> { show[0] = !show[0]; applyPw(et, eye, show[0]); });
    }

    private static void applyPw(EditText et, ImageView eye, boolean show) {
        int sel = et.getSelectionEnd();
        et.setTransformationMethod(show ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
        et.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)); // inputType password mac dinh la monospace
        et.setLetterSpacing(show ? 0f : 2f / 15f);
        iconTint(eye, show ? C.CYAN : C.MUTED);
        if (sel >= 0) et.setSelection(Math.min(sel, et.length()));
    }

    // ---------- avatar (view_avatar / item_avatar_opt)
    private static final int[][] AV = {
            {C.CYAN, C.BLUE}, {0xFFA673FF, 0xFFFA4F6E}, {0xFFFABF24, 0xFFFA734D}, {0xFF33D499, C.CYAN}};
    private static final String[] AV_ICON = {null, "shield", "server", "zap"};

    /** circle: FrameLayout 64dp gom [TextView chu M, ImageView icon]. */
    public static void avatar(ViewGroup circle, int style, boolean selected) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, AV[style]);
        g.setShape(GradientDrawable.OVAL);
        if (selected) g.setStroke(dp(3), C.CYAN);
        circle.setBackground(g);
        TextView letter = (TextView) circle.getChildAt(0);
        ImageView ic = (ImageView) circle.getChildAt(1);
        if (AV_ICON[style] == null) { letter.setVisibility(View.VISIBLE); ic.setVisibility(View.GONE); }
        else { letter.setVisibility(View.GONE); ic.setVisibility(View.VISIBLE); icon(ic, AV_ICON[style], C.INK); }
    }
}
