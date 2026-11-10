package com.example.netops;

import android.widget.TextView;

public class AlertDetailFragment extends BaseFragment {
    @Override protected int layout() { return R.layout.fragment_alertdetail; }

    @Override protected void init() {
        final boolean sql = "sql".equals(arg());
        ((TextView) v(R.id.ad_title)).setText(sql ? "SQL Server latency > 500ms" : "High CPU. API crashed");
        ((TextView) v(R.id.ad_where)).setText(sql ? "SQL Server \u00b7 Server 2 \u00b7 13:31 UTC" : "C# Web API \u00b7 Server 1 \u00b7 14:02 UTC");

        String[] labels = {sql ? "Latency" : "CPU", "Restarts", "Downtime"};
        String[] vals = {sql ? "84%" : "97%", sql ? "1" : "3", sql ? "00:41" : "02:14"};
        int[] cols = {C.RED, C.AMBER, C.TEXT};
        int[] lid = {R.id.ad_l0, R.id.ad_l1, R.id.ad_l2}, vid = {R.id.ad_v0, R.id.ad_v1, R.id.ad_v2};
        for (int i = 0; i < 3; i++) {
            ((TextView) v(lid[i])).setText(labels[i]);
            TextView t = v(vid[i]);
            t.setText(vals[i]);
            t.setTextColor(cols[i]);
        }

        String[] lines = sql ? new String[]{
                "[13:29:58] WARN  query p95=640ms limit=500ms", "[13:30:21] WARN  lock wait on tempdb 4.2s", "   at Sql.Exec.Plan.Run()",
                "[13:30:40] ERROR deadlock victim spid=57", "[13:31:02] HEALTH latency probe 512ms"}
                : new String[]{
                "[14:02:11] WARN  cpu=97% threshold=85%", "[14:02:14] ERROR System.OutOfMemoryException", "   at WebApi.Jobs.Sync.Run()",
                "[14:02:15] FATAL process exited (code 137)", "[14:02:16] HEALTH probe failed x3"};
        int[] lc = {C.AMBER, sql ? C.AMBER : C.RED, C.MUTED, C.RED, C.AMBER};
        if (sql) lc = new int[]{C.AMBER, C.AMBER, C.MUTED, C.RED, C.AMBER};
        int[] ids = {R.id.ad_log0, R.id.ad_log1, R.id.ad_log2, R.id.ad_log3, R.id.ad_log4};
        for (int i = 0; i < 5; i++) {
            TextView t = v(ids[i]);
            t.setText(lines[i].replace("  ", " \u00a0"));
            t.setTextColor(lc[i]);
        }
        tap(R.id.ad_fix, () -> nav().go("terminal", sql ? "sql-server" : "web-api"));
        tap(R.id.ad_ignore, () -> nav().back());
    }
}
