package com.example.netops;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.netops.net.ApiClient;
import com.example.netops.net.Models;
import com.example.netops.net.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Overview: số liệu thật đo từ 2 server Azure (qua api/Home/overview), tự làm mới mỗi 10 giây. */
public class DashboardFragment extends BaseFragment {
    private static final long REFRESH_MS = 10_000;

    private Models.Overview api;

    @Override protected int layout() { return R.layout.fragment_dashboard; }

    @Override protected void init() {
        Data.Env e = Data.ENVS[Store.env];
        ((TextView) v(R.id.hdr_sub)).setText("Đang tải...");
        ((TextView) v(R.id.env_pill_t)).setText(e.tag);
        ((TextView) v(R.id.env_pill_t)).setTextColor(e.tagColor);
        UI.iconTint((ImageView) v(R.id.env_pill_i), e.tagColor);
        v(R.id.env_pill).setBackground(UI.rect(UI.alpha(e.tagColor, 0.14f), 0, 0, 99));
        tap(R.id.env_pill, () -> nav().go("env"));

        kpi(R.id.kpi_srv_v, R.id.kpi_srv_dot, R.id.kpi_srv_s, "–", C.CYAN, "loading", C.CYAN);
        kpi(R.id.kpi_cont_v, R.id.kpi_cont_dot, R.id.kpi_cont_s, "–", C.CYAN, "loading", C.CYAN);
        kpi(R.id.kpi_al_v, R.id.kpi_al_dot, R.id.kpi_al_s, "–", C.CYAN, "loading", C.CYAN);
        ((LinearLayout) v(R.id.attn_list)).removeAllViews();
        ((LinearLayout) v(R.id.srv_list)).removeAllViews();
        ((LinearLayout) v(R.id.top_list)).removeAllViews();

        tap(R.id.kpi_srv, () -> nav().go("server", "1"));
        tap(R.id.kpi_cont, () -> nav().tab(1));
        tap(R.id.kpi_al, () -> nav().tab(3));
        tap(R.id.attn_all, () -> nav().tab(3));
        tap(R.id.top_all, () -> nav().tab(1));

        loadOverview();
    }

    private void loadOverview() {
        ApiClient.get().overview(Session.bearer()).enqueue(new Callback<Models.Overview>() {
            @Override public void onResponse(Call<Models.Overview> call, Response<Models.Overview> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null) {
                    api = r.body();
                    render();
                    later(REFRESH_MS, DashboardFragment.this::loadOverview);
                } else if (r.code() == 401) {
                    Store.say("Phiên đăng nhập hết hạn");
                    nav().replace("login");
                } else {
                    Store.say("Không tải được Overview (" + r.code() + ")");
                    later(REFRESH_MS, DashboardFragment.this::loadOverview);
                }
            }
            @Override public void onFailure(Call<Models.Overview> call, Throwable t) {
                if (!isAdded()) return;
                Store.say("Không kết nối được máy chủ");
                later(REFRESH_MS, DashboardFragment.this::loadOverview);
            }
        });
    }

    private List<Models.ServerDto> servers() { return api.servers != null ? api.servers : new ArrayList<>(); }
    private List<Models.ContainerDto> containers() { return api.containers != null ? api.containers : new ArrayList<>(); }
    private List<Models.AttentionDto> attention() { return api.attention != null ? api.attention : new ArrayList<>(); }

    private void render() {
        int online = 0;
        for (Models.ServerDto s : servers()) if ("Running".equalsIgnoreCase(s.status)) online++;
        int total = api.totalServers;
        boolean allUp = online == total;

        ((TextView) v(R.id.hdr_sub)).setText(Store.ENV_NAMES[Store.env] + " · " + online + " servers online");
        kpi(R.id.kpi_srv_v, R.id.kpi_srv_dot, R.id.kpi_srv_s, online + "/" + total,
                allUp ? C.GREEN : C.AMBER, allUp ? "all online" : (total - online) + " offline", allUp ? C.GREEN : C.AMBER);
        ((TextView) v(R.id.kpi_srv_v)).setTextColor(allUp ? C.GREEN : C.AMBER);

        int stopped = 0;
        for (Models.ContainerDto c : containers()) if (!"Running".equalsIgnoreCase(c.status)) stopped++;
        kpi(R.id.kpi_cont_v, R.id.kpi_cont_dot, R.id.kpi_cont_s, String.valueOf(api.totalContainers),
                stopped > 0 ? C.AMBER : C.GREEN, stopped > 0 ? stopped + " stopped" : "all healthy", stopped > 0 ? C.AMBER : C.GREEN);

        boolean hasAl = api.alerts > 0;
        kpi(R.id.kpi_al_v, R.id.kpi_al_dot, R.id.kpi_al_s, String.valueOf(api.alerts),
                hasAl ? C.RED : C.GREEN, hasAl ? "need attention" : "none", hasAl ? C.RED : C.GREEN);
        ((TextView) v(R.id.kpi_al_v)).setTextColor(hasAl ? C.RED : C.GREEN);

        ((TextView) v(R.id.srv_online)).setText(online + " online");
        renderAttention();
        renderServers();
        renderTop();
    }

    private void kpi(int vId, int dotId, int sId, String value, int dot, String sub, int subColor) {
        ((TextView) v(vId)).setText(value);
        v(dotId).setBackground(UI.oval(dot, 0, 0));
        TextView s = v(sId);
        s.setText(sub);
        s.setTextColor(subColor);
    }

    private void note(LinearLayout list, String text) {
        TextView t = new TextView(requireContext());
        t.setText(text);
        t.setTextSize(13f);
        t.setTextColor(C.GREEN);
        t.setPadding(UI.dp(4), UI.dp(8), UI.dp(4), UI.dp(8));
        list.addView(t);
    }

    private void renderAttention() {
        LinearLayout list = v(R.id.attn_list);
        list.removeAllViews();
        if (attention().isEmpty()) { note(list, "Mọi thứ hoạt động bình thường"); return; }
        for (Models.AttentionDto a : attention()) {
            int col = "critical".equals(a.severity) ? C.RED : C.AMBER;
            View it = UI.inflate(list, R.layout.item_attn);
            it.setBackground(UI.rect(C.CARD, UI.alpha(col, 0.5f), 1, 16));
            UI.tint(it.findViewById(R.id.attn_tile), col, 10);
            UI.icon((ImageView) it.findViewById(R.id.attn_icon), "warn", col);
            ((TextView) it.findViewById(R.id.attn_t)).setText(a.title);
            ((TextView) it.findViewById(R.id.attn_s)).setText(a.sub);
            it.setOnClickListener(x -> nav().tab(3));
            list.addView(it);
        }
    }

    private void renderServers() {
        LinearLayout list = v(R.id.srv_list);
        list.removeAllViews();
        int[] bars = {C.CYAN, C.VIOLET, C.BLUE};
        for (final Models.ServerDto sd : servers()) {
            int count = 0;
            for (Models.ContainerDto c : containers()) if (c.serverId == sd.id) count++;
            boolean up = "Running".equalsIgnoreCase(sd.status);
            boolean warn = up && (sd.cpu >= 85 || sd.ram >= 90 || sd.disk >= 90);
            int[] vals = {clamp(sd.cpu), clamp(sd.ram), clamp(sd.disk)};

            View it = UI.inflate(list, R.layout.item_server_card);
            UI.tint(it.findViewById(R.id.sv_tile), C.BLUE, 11);
            ((TextView) it.findViewById(R.id.sv_name)).setText(sd.name);
            ((TextView) it.findViewById(R.id.sv_sub)).setText(sd.ipAddress + " · " + count + (count == 1 ? " container" : " containers"));
            UI.pill((TextView) it.findViewById(R.id.sv_chip),
                    !up ? "● Offline" : warn ? "● Warning" : "● Healthy", !up ? C.RED : warn ? C.AMBER : C.GREEN);
            int[] vi = {R.id.sv_0_v, R.id.sv_1_v, R.id.sv_2_v}, fi = {R.id.sv_0_f, R.id.sv_1_f, R.id.sv_2_f};
            for (int k = 0; k < 3; k++) {
                ((TextView) it.findViewById(vi[k])).setText(up ? vals[k] + "%" : "--");
                View f = it.findViewById(fi[k]);
                f.setBackground(UI.rect(bars[k], 0, 0, 2));
                UI.fill(f, up ? vals[k] / 100f : 0f);
            }
            it.setOnClickListener(x -> nav().go("server", String.valueOf(Math.min(Math.max(sd.id, 1), 2))));
            list.addView(it);
        }
    }

    private void renderTop() {
        LinearLayout list = v(R.id.top_list);
        list.removeAllViews();
        List<Models.ContainerDto> run = new ArrayList<>();
        for (Models.ContainerDto c : containers()) if ("Running".equalsIgnoreCase(c.status)) run.add(c);
        if (run.isEmpty()) { note(list, "Chưa có container nào đang chạy"); return; }
        Collections.sort(run, (a, b) -> Double.compare(b.cpu, a.cpu));
        int shown = 0;
        for (final Models.ContainerDto c : run) {
            if (shown == 2) break;
            if (shown > 0) {
                View div = new View(requireContext());
                div.setBackgroundColor(C.STROKE);
                list.addView(div, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UI.dp(1)));
            }
            View it = UI.inflate(list, R.layout.item_top);
            UI.tint(it.findViewById(R.id.tp_tile), C.VIOLET, 8);
            UI.icon((ImageView) it.findViewById(R.id.tp_icon), "box", C.VIOLET);
            ((TextView) it.findViewById(R.id.tp_name)).setText(c.name);
            ((TextView) it.findViewById(R.id.tp_sub)).setText(serverName(c.serverId));
            ((TextView) it.findViewById(R.id.tp_cpu)).setText(Math.round(c.cpu) + "%");
            it.setOnClickListener(x -> nav().tab(1));
            list.addView(it);
            shown++;
        }
    }

    private String serverName(int id) {
        for (Models.ServerDto s : servers()) if (s.id == id) return s.name;
        return "Server " + id;
    }

    private static int clamp(double v) { return (int) Math.max(0, Math.min(100, Math.round(v))); }
}
