package com.example.netops;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HealingFragment extends BaseFragment {
    /** icon, mau, tieu de, mo ta, thoi gian */
    private static final Object[][] LOGS = {
            {"restart", C.CYAN, "Container restarted", "web-api crashed (OOM) \u2192 restarted automatically", "14:02 \u00b7 recovered in 8s"},
            {"restart", C.CYAN, "Container restarted", "postgres health check failed \u2192 restarted", "13:48 \u00b7 recovered in 12s"},
            {"cpu", C.VIOLET, "Scaled memory limit", "sql-server near limit \u2192 limit raised to 4 GB", "12:15 \u00b7 recovered in 5s"},
            {"zap", C.AMBER, "Service reconnected", "redis dropped \u2192 connection re-established", "10:31 \u00b7 recovered in 3s"}};

    @Override protected int layout() { return R.layout.fragment_healing; }

    @Override protected void init() {
        ((RingView) v(R.id.hl_ring)).setProgress(1f);
        LinearLayout list = v(R.id.hl_list);
        for (Object[] l : LOGS) {
            int col = (Integer) l[1];
            View it = UI.inflate(list, R.layout.item_heal);
            UI.tint(it.findViewById(R.id.hl_tile), col, 12);
            UI.icon((ImageView) it.findViewById(R.id.hl_icon), (String) l[0], col);
            ((TextView) it.findViewById(R.id.hl_title)).setText((String) l[2]);
            ((TextView) it.findViewById(R.id.hl_desc)).setText((String) l[3]);
            ((TextView) it.findViewById(R.id.hl_time)).setText((String) l[4]);
            list.addView(it);
        }
    }
}
