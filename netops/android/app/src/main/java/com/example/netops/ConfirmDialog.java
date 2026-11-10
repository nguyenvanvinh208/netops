package com.example.netops;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/** Hop thoai xac nhan Stop / Restart (lop phu toan man hinh). */
public final class ConfirmDialog {
    private ConfirmDialog() {}

    public static void show(final MainActivity act, String id, String action, final Runnable after) {
        final Data.CInfo c = Data.cinfo(id);
        final boolean restart = "restart".equals(action);
        final String srv = "Server " + c.server;
        final FrameLayout host = act.overlayHost();
        final View root = LayoutInflater.from(act).inflate(R.layout.view_confirm, host, false);

        ((TextView) root.findViewById(R.id.cf_title)).setText(restart ? "Restart " + c.name + "?" : "Stop " + c.name + "?");
        ((TextView) root.findViewById(R.id.cf_body)).setText(restart
                ? "This will stop and start the container on " + srv + ". Active connections will drop for about 8 seconds."
                : "The container on " + srv + " will stop. It stays unavailable until you start it again.");
        ((TextView) root.findViewById(R.id.cf_session)).setText(c.sessions);
        UI.icon((ImageView) root.findViewById(R.id.cf_icon), restart ? "restart" : "stop", C.ON_RED);

        View card = root.findViewById(R.id.cf_card);
        ViewGroup.LayoutParams lp = card.getLayoutParams();
        lp.width = Math.min(UI.dp(318), act.getResources().getDisplayMetrics().widthPixels - UI.dp(72));
        card.setLayoutParams(lp);

        final Runnable close = () -> host.removeView(root);
        root.setOnClickListener(v -> close.run());
        root.findViewById(R.id.cf_cancel).setOnClickListener(v -> close.run());
        root.findViewById(R.id.cf_confirm).setOnClickListener(v -> {
            if (restart) Store.say(c.id + " restarted");
            else { Store.stop(c.id); Store.say(c.id + " stopped"); }
            close.run();
            if (after != null) after.run();
        });
        host.addView(root);
    }
}
