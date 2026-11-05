package com.example.netops;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlertsFragment extends BaseFragment {
    private int f = 0;

    /** title, sub, time, kind (0 critical, 1 info, 2 ok), route, arg */
    private static final String[][] ALERTS = {
            {"High CPU. API crashed", "C# Web API \u00b7 Server 1", "2m", "0", "alertdetail", "api"},
            {"Container restarted", "PostgreSQL \u00b7 auto-healing", "14m", "1", "healing", null},
            {"SQL Server latency > 500ms", "SQL Server \u00b7 Server 2", "31m", "0", "alertdetail", "sql"},
            {"Disk usage back to normal", "Server 2 \u00b7 71%", "1h", "2", "", null},
            {"Memory spike resolved", "C# Web API \u00b7 Server 1", "3h", "2", "", null}};

    @Override protected int layout() { return R.layout.fragment_alerts; }
    @Override protected void init() { render(); }

    private void render() {
        String[] subs = {"3 active incidents \u00b7 live", "2 critical incidents \u00b7 live", "3 resolved in the last 24h"};
        ((TextView) v(R.id.hdr_sub)).setText(subs[f]);
        UI.chips((LinearLayout) v(R.id.chips_al), new String[]{"All events", "Critical", "Resolved"}, f, false, i -> { f = i; render(); });
        LinearLayout list = v(R.id.al_list);
        list.removeAllViews();
        for (final String[] a : ALERTS) {
            int kind = Integer.parseInt(a[3]);
            if (f == 1 && kind != 0) continue;
            if (f == 2 && kind == 0) continue;
            int col = kind == 0 ? C.RED : kind == 1 ? C.AMBER : C.GREEN;
            String icon = kind == 0 ? "warn" : kind == 1 ? "zap" : "check";
            View it = UI.inflate(list, R.layout.item_alert);
            it.setBackground(UI.rect(C.CARD, kind == 0 ? UI.alpha(C.RED, 0.5f) : C.STROKE, 1, 20));
            UI.tint(it.findViewById(R.id.al_tile), col, 14);
            UI.icon((ImageView) it.findViewById(R.id.al_icon), icon, col);
            ((TextView) it.findViewById(R.id.al_title)).setText(a[0]);
            ((TextView) it.findViewById(R.id.al_sub)).setText(a[1]);
            ((TextView) it.findViewById(R.id.al_time)).setText(a[2]);
            if (!a[4].isEmpty()) it.setOnClickListener(x -> nav().go(a[4], a[5]));
            list.addView(it);
        }
    }
}
