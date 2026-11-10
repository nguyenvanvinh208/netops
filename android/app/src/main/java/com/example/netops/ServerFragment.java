package com.example.netops;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.netops.net.Models;
import com.example.netops.net.Session;

/** Server detail: tai nguyen hien tai, bieu do CPU vs RAM theo khoang thoi gian, container tren server. */
public class ServerFragment extends BaseFragment {
    private int srv = 1;

    @Override protected int layout() { return R.layout.fragment_server; }

    @Override protected void init() {
        srv = "2".equals(arg()) ? 2 : 1;
        Models.ServerDto sd = null;
        if (Session.overview != null && Session.overview.servers != null)
            for (Models.ServerDto x : Session.overview.servers) if (x.id == srv) sd = x;

        ((TextView) v(R.id.hdr_title)).setText(sd != null ? sd.name : "Server " + srv);
        ((TextView) v(R.id.hdr_sub)).setText(sd != null ? sd.ipAddress : "srv" + srv + ".prod");
        boolean up = sd == null || DashboardFragment.isUp(sd);
        UI.pill((TextView) v(R.id.sr_chip), up ? "Online" : "Offline", up ? C.GREEN : C.RED);

        // CPU/RAM/Disk hiện tại: số thật từ API (nếu chưa có thì dùng số mẫu)
        Data.Srv d = Data.SRV[srv];
        int[] vals = sd != null
                ? new int[]{(int) Math.round(sd.cpu), (int) Math.round(sd.ram), (int) Math.round(sd.disk)}
                : new int[]{d.cpu, d.ram, Data.SRV_DISK[srv]};
        int[] cols = {C.CYAN, C.VIOLET, C.BLUE};
        int[] vi = {R.id.rs_0_v, R.id.rs_1_v, R.id.rs_2_v}, fi = {R.id.rs_0_f, R.id.rs_1_f, R.id.rs_2_f};
        for (int k = 0; k < 3; k++) {
            ((TextView) v(vi[k])).setText(up ? vals[k] + "%" : "\u2014");
            View f = v(fi[k]);
            f.setBackground(UI.rect(cols[k], 0, 0, 2));
            UI.fill(f, up ? vals[k] / 100f : 0f);
        }
        containers();
        range();
    }

    private void range() {
        UI.chips((LinearLayout) v(R.id.chips_perf), new String[]{"1H", "24H", "7D", "30D"}, Store.perfRange, true, 12,
                i -> { Store.perfRange = i; range(); });
        Data.Perf p = Data.PERF[Store.perfRange];
        ((PerfChartView) v(R.id.pf_chart)).setData(p.cpu, p.cpuTop, p.ram, p.ramTop);
        int[] l = {R.id.pf_l0, R.id.pf_l1, R.id.pf_l2, R.id.pf_l3, R.id.pf_l4};
        for (int i = 0; i < 5; i++) ((TextView) v(l[i])).setText(p.labels[i]);
        String[] s = Data.SRV_STATS[srv - 1][Store.perfRange];
        ((TextView) v(R.id.mt_0)).setText(s[0]);
        ((TextView) v(R.id.mt_1)).setText(s[1]);
        ((TextView) v(R.id.mt_2)).setText(s[2]);
    }

    private void containers() {
        LinearLayout list = v(R.id.sc_list);
        list.removeAllViews();
        int n = 0;
        if (Session.overview != null && Session.overview.containers != null) {
            for (Models.ContainerDto c : Session.overview.containers) {
                if (c.serverId != srv) continue;
                if (n > 0) addDivider(list);
                View it = UI.inflate(list, R.layout.item_top);
                boolean running = "Running".equalsIgnoreCase(c.status);
                DashboardFragment.bindContainerRow(it, c, running ? "Running" : "Stopped");
                it.setOnClickListener(x -> nav().tab(1));
                list.addView(it);
                n++;
            }
        } else {
            for (final Data.CInfo c : Data.CONTAINERS) {
                if (c.server != srv) continue;
                if (n > 0) addDivider(list);
                String st = Store.st(c.id);
                View it = UI.inflate(list, R.layout.item_top);
                UI.tint(it.findViewById(R.id.tp_tile), c.tile, 8);
                UI.icon((ImageView) it.findViewById(R.id.tp_icon), c.icon, c.tile);
                ((TextView) it.findViewById(R.id.tp_name)).setText(c.name);
                ((TextView) it.findViewById(R.id.tp_sub)).setText("stopped".equals(st) ? "Stopped" : "slow".equals(st) ? "Slow" : "Running");
                ((TextView) it.findViewById(R.id.tp_cpu)).setText("stopped".equals(st) ? "\u2014" : c.cpu + "%");
                it.setOnClickListener(x -> nav().go("container", c.id));
                list.addView(it);
                n++;
            }
        }
        ((TextView) v(R.id.sc_title)).setText("Containers (" + n + ")");
        tap(R.id.sc_all, () -> nav().tab(1));
    }

    private void addDivider(LinearLayout list) {
        View div = new View(requireContext());
        div.setBackgroundColor(C.STROKE);
        list.addView(div, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UI.dp(1)));
    }
}
