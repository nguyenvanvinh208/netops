package com.example.netops;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Overview (Fleet): KPI, Needs attention, Servers, Top containers by CPU. */
public class DashboardFragment extends BaseFragment {
    /** title, sub, route arg */
    private static final String[][] ATTN = {
            {"High CPU. API crashed", "C# Web API · Server 1 · 2m ago", "api"},
            {"SQL Server latency > 500ms", "SQL Server · Server 2 · 31m ago", "sql"}};

    @Override protected int layout() { return R.layout.fragment_dashboard; }

    @Override protected void init() {
        Data.Env e = Data.ENVS[Store.env];
        ((TextView) v(R.id.hdr_sub)).setText(Store.ENV_NAMES[Store.env] + " · 2 servers online");
        ((TextView) v(R.id.env_pill_t)).setText(e.tag);
        ((TextView) v(R.id.env_pill_t)).setTextColor(e.tagColor);
        UI.iconTint((ImageView) v(R.id.env_pill_i), e.tagColor);
        v(R.id.env_pill).setBackground(UI.rect(UI.alpha(e.tagColor, 0.14f), 0, 0, 99));
        tap(R.id.env_pill, () -> nav().go("env"));

        int slow = 0;
        for (Data.CInfo c : Data.CONTAINERS) if ("slow".equals(Store.st(c.id))) slow++;
        kpi(R.id.kpi_srv_v, R.id.kpi_srv_dot, R.id.kpi_srv_s, "2/2", C.GREEN, "all online", C.GREEN);
        kpi(R.id.kpi_cont_v, R.id.kpi_cont_dot, R.id.kpi_cont_s, String.valueOf(Data.CONTAINERS.length),
                slow > 0 ? C.AMBER : C.GREEN, slow > 0 ? slow + " slow" : "all healthy", slow > 0 ? C.AMBER : C.GREEN);
        kpi(R.id.kpi_al_v, R.id.kpi_al_dot, R.id.kpi_al_s, String.valueOf(ATTN.length), C.RED, "critical", C.RED);
        ((TextView) v(R.id.kpi_srv_v)).setTextColor(C.GREEN);
        ((TextView) v(R.id.kpi_al_v)).setTextColor(C.RED);
        tap(R.id.kpi_srv, () -> nav().go("server", "1"));
        tap(R.id.kpi_cont, () -> nav().tab(1));
        tap(R.id.kpi_al, () -> nav().tab(3));

        attention();
        servers();
        topContainers();
        tap(R.id.attn_all, () -> nav().tab(3));
        tap(R.id.top_all, () -> nav().tab(1));
    }

    private void kpi(int vId, int dotId, int sId, String value, int dot, String sub, int subColor) {
        ((TextView) v(vId)).setText(value);
        v(dotId).setBackground(UI.oval(dot, 0, 0));
        TextView s = v(sId);
        s.setText(sub);
        s.setTextColor(subColor);
    }

    private void attention() {
        LinearLayout list = v(R.id.attn_list);
        list.removeAllViews();
        for (final String[] a : ATTN) {
            View it = UI.inflate(list, R.layout.item_attn);
            it.setBackground(UI.rect(C.CARD, UI.alpha(C.RED, 0.5f), 1, 16));
            UI.tint(it.findViewById(R.id.attn_tile), C.RED, 10);
            UI.icon((ImageView) it.findViewById(R.id.attn_icon), "warn", C.RED);
            ((TextView) it.findViewById(R.id.attn_t)).setText(a[0]);
            ((TextView) it.findViewById(R.id.attn_s)).setText(a[1]);
            it.setOnClickListener(x -> nav().go("alertdetail", a[2]));
            list.addView(it);
        }
    }

    private void servers() {
        LinearLayout list = v(R.id.srv_list);
        list.removeAllViews();
        int[] bars = {C.CYAN, C.VIOLET, C.BLUE};
        for (int n = 1; n <= 2; n++) {
            final int srv = n;
            int count = 0, slow = 0, stopped = 0;
            for (Data.CInfo c : Data.CONTAINERS) {
                if (c.server != n) continue;
                count++;
                String st = Store.st(c.id);
                if ("slow".equals(st)) slow++;
                if ("stopped".equals(st)) stopped++;
            }
            Data.Srv d = Data.SRV[n];
            int[] vals = {d.cpu, d.ram, Data.SRV_DISK[n]};
            View it = UI.inflate(list, R.layout.item_server_card);
            UI.tint(it.findViewById(R.id.sv_tile), C.BLUE, 11);
            ((TextView) it.findViewById(R.id.sv_name)).setText("Server " + n);
            String sub = "srv" + n + ".prod · " + count + (count == 1 ? " container" : " containers");
            if (slow > 0) sub += " · " + slow + " slow";
            if (stopped > 0) sub += " · " + stopped + " stopped";
            ((TextView) it.findViewById(R.id.sv_sub)).setText(sub);
            boolean warn = slow > 0 || stopped > 0;
            UI.pill((TextView) it.findViewById(R.id.sv_chip), warn ? "● Warning" : "● Healthy", warn ? C.AMBER : C.GREEN);
            int[] vi = {R.id.sv_0_v, R.id.sv_1_v, R.id.sv_2_v}, fi = {R.id.sv_0_f, R.id.sv_1_f, R.id.sv_2_f};
            for (int k = 0; k < 3; k++) {
                ((TextView) it.findViewById(vi[k])).setText(vals[k] + "%");
                View f = it.findViewById(fi[k]);
                f.setBackground(UI.rect(bars[k], 0, 0, 2));
                UI.fill(f, vals[k] / 100f);
            }
            it.setOnClickListener(x -> nav().go("server", String.valueOf(srv)));
            list.addView(it);
        }
    }

    private void topContainers() {
        LinearLayout list = v(R.id.top_list);
        list.removeAllViews();
        Data.CInfo[] sorted = Data.CONTAINERS.clone();
        java.util.Arrays.sort(sorted, (a, b) -> b.cpu - a.cpu);
        int shown = 0;
        for (final Data.CInfo c : sorted) {
            if ("stopped".equals(Store.st(c.id))) continue;
            if (shown == 2) break;
            if (shown > 0) {
                View div = new View(requireContext());
                div.setBackgroundColor(C.STROKE);
                list.addView(div, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UI.dp(1)));
            }
            View it = UI.inflate(list, R.layout.item_top);
            UI.tint(it.findViewById(R.id.tp_tile), c.tile, 8);
            UI.icon((ImageView) it.findViewById(R.id.tp_icon), c.icon, c.tile);
            ((TextView) it.findViewById(R.id.tp_name)).setText(c.name);
            ((TextView) it.findViewById(R.id.tp_sub)).setText("Server " + c.server);
            ((TextView) it.findViewById(R.id.tp_cpu)).setText(c.cpu + "%");
            it.setOnClickListener(x -> nav().go("container", c.id));
            list.addView(it);
            shown++;
        }
    }
}
