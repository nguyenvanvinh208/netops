package com.example.netops;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/** Phan dung chung cho man Containers va Container detail. */
final class ContainerUi {
    private ContainerUi() {}

    /** Nut vien mau (Start/Stop/Restart): mo di khi bi vo hieu. */
    static void tintButton(View btn, int color, boolean enabled, final Runnable onClick) {
        float a = enabled ? 1f : 0.35f;
        ViewGroup g = (ViewGroup) btn;
        float r = btn.getLayoutParams().height >= UI.dp(46) ? 14 : 12;
        btn.setBackground(UI.rect(UI.alpha(color, 0.12f * a), UI.alpha(color, 0.5f * a), 1, r));
        UI.iconTint((ImageView) g.getChildAt(0), UI.alpha(color, a));
        ((TextView) g.getChildAt(1)).setTextColor(UI.alpha(color, a));
        btn.setOnClickListener(enabled ? (View.OnClickListener) x -> onClick.run() : null);
        btn.setClickable(enabled);
    }

    /** {nhan, mau, dang chay?} */
    static Object[] statusOf(String id) {
        String s = Store.st(id);
        if ("stopped".equals(s)) return new Object[]{"Stopped", C.MUTED, false};
        if ("slow".equals(s)) return new Object[]{"Slow", C.AMBER, true};
        return new Object[]{"Running", C.GREEN, true};
    }
}
