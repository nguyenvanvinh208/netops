package com.example.netops;

import static android.util.Log.v;

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

/** Overview (Fleet): toàn bộ số liệu lấy từ GET api/Home/overview, tự làm mới mỗi 10 giây. */
public class DashboardFragment extends BaseFragment {
    private static final long REFRESH_MS = 10_000;

    @Override protected int layout() { return R.layout.fragment_dashboard; }

    @Override protected void init() {
        Data.Env e = Data.ENVS[Store.env];
        ((TextView) v(R.id.hdr_sub)).setText(Store.ENV_NAMES[Store.env] + " · loading…");
        ((TextView) v(R.id.env_pill_t)).setText(e.tag);
        ((TextView) v(R.id.env_pill_t)).setTextColor(e.tagColor);
        UI.iconTint((ImageView) v(R.id.env_pill_i), e.tagColor);
        v(R.id.env_pill).setBackground(UI.rect(UI.alpha(e.tagColor, 0.14f), 0, 0, 99));
        tap(R.id.env_pill, () -> nav().go("env"));

        kpi(R.id.kpi_srv_v, R.id.kpi_srv_dot, R.id.kpi_srv_s, "–", C.DIM, "loading", C.MUTED);
        kpi(R.id.kpi_cont_v, R.id.kpi_cont_dot, R.id.kpi_cont_s, "–", C.DIM, "loading", C.MUTED);
        kpi(R.id.kpi_al_v, R.id.kpi_al_dot, R.id.kpi_al_s, "–", C.DIM, "loading", C.MUTED);
        tap(R.id.kpi_srv, () -> nav().go("server", "1"));
        tap(R.id.kpi_cont, () -> nav().tab(1));
        tap(R.id.kpi_al, () -> nav().tab(3));
        tap(R.id.attn_all, () -> nav().tab(3));
        tap(R.id.top_all, () -> nav().tab(1));

        if (Session.overview != null) applyOverview(Session.overview); // hiện ngay bản gần nhất, rồi tải lại
        loadOverview();
    }

    private void loadOverview() {
        ApiClient.get().overview(Session.bearer()).enqueue(new Callback<Models.Overview>() {
            @Override public void onResponse(Call<Models.Overview> call, Response<Models.Overview> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null) {
                    Session.overview = r.body();
                    applyOverview(r.body());
                } else if (r.code() == 401) {
                    Store.say("Phiên đăng nhập hết hạn");
                    nav().replace("login");
                    return;
                } else {
                    Store.say("Không tải được Overview (" + r.code() + ")");
                }
                later(REFRESH_MS, DashboardFragment.this::loadOverview);
            }
            @Override public void onFailure(Call<Models.Overview> call, Throwable t) {
                if (!isAdded()) return;
                Store.say("Không kết nối được máy chủ");
                later(REFRESH_MS, DashboardFragment.this::loadOverview);
            }
        });
    }

    private void applyOverview(Models.Overview api) {
        List<Models.ServerDto> servers = api.servers != null ? api.servers : new ArrayList<>();
        List<Models.ContainerDto> containers = api.containers != null ? api.containers : new ArrayList<>();
        List<Models.AttentionDto> attn = api.attention != null ? api.attention : new ArrayList<>();

        int online = 0;
        for (Models.ServerDto s : servers) if (isUp(s)) online++;
        boolean allUp = online == api.totalServers;
        kpi(R.id.kpi_srv_v, R.id.kpi_srv_dot, R.id.kpi_srv_s, online + "/" + api.totalServers,
                allUp ? C.GREEN : C.AMBER, allUp ? "all online" : (api.totalServers - online) + " offline", allUp ? C.GREEN : C.AMBER);
        ((TextView) v(R.id.kpi_srv_v)).setTextColor(allUp ? C.GREEN : C.AMBER);

        int stopped = 0;
        for (Models.ContainerDto c : containers) if (!"Running".equalsIgnoreCase(c.status)) stopped++;
        kpi(R.id.kpi_cont_v, R.id.kpi_cont_dot, R.id.kpi_cont_s, String.valueOf(api.totalContainers),
                stopped > 0 ? C.AMBER : C.GREEN, stopped > 0 ? stopped + " stopped" : "all running", stopped > 0 ? C.AMBER : C.GREEN);
        ((TextView) v(R.id.kpi_cont_v)).setTextColor(C.TEXT);

        int critical = 0;
        for (Models.AttentionDto a : attn) if ("critical".equalsIgnoreCase(a.severity)) critical++;
        int col = api.alerts == 0 ? C.GREEN : critical > 0 ? C.RED : C.AMBER;
        kpi(R.id.kpi_al_v, R.id.kpi_al_dot, R.id.kpi_al_s, String.valueOf(api.alerts), col,
                api.alerts == 0 ? "none" : critical > 0 ? "critical" : "warning", col);
        ((TextView) v(R.id.kpi_al_v)).setTextColor(col);

        ((TextView) v(R.id.hdr_sub)).setText(Store.ENV_NAMES[Store.env] + " · " + online + " servers online");
        ((TextView) v(R.id.srv_online)).setText(online + " online");
        ((TextView) v(R.id.srv_online)).setTextColor(allUp ? C.GREEN : C.AMBER);

        attention(attn);
        servers(servers, containers);
        topContainers(containers);
    }

    static boolean isUp(Models.ServerDto s) { return "Running".equalsIgnoreCase(s.status); }

    private void kpi(int vId, int dotId, int sId, String value, int dot, String sub, int subColor) {
        TextView t = v(vId);
        t.setText(value);
        t.setTextColor(C.TEXT);
        v(dotId).setBackground(UI.oval(dot, 0, 0));
        TextView s = v(sId);
        s.setText(sub);
        s.setTextColor(subColor);
    }

    private void attention(List<Models.AttentionDto> attn) {
        LinearLayout list = v(R.id.attn_list);
        list.removeAllViews();
        if (attn.isEmpty()) {
            View it = UI.inflate(list, R.layout.item_attn);
            it.setBackground(UI.rect(C.CARD, UI.alpha(C.GREEN, 0.4f), 1, 16));
            UI.tint(it.findViewById(R.id.attn_tile), C.GREEN, 10);
            UI.icon((ImageView) it.findViewById(R.id.attn_icon), "check", C.GREEN);
            ((TextView) it.findViewById(R.id.attn_t)).setText("All systems normal");
            ((TextView) it.findViewById(R.id.attn_s)).setText("No issues detected on any server");
            it.findViewById(R.id.attn_root).setClickable(false);
            list.addView(it);
            return;
        }
        for (Models.AttentionDto a : attn) {
            int col = "critical".equalsIgnoreCase(a.severity) ? C.RED : C.AMBER;
            View it = UI.inflate(list, R.layout.item_attn);
            it.setBackground(UI.rect(C.CARD, UI.alpha(col, 0.5f), 1, 16));
            UI.tint(it.findViewById(R.id.attn_tile), col, 10);
            UI.icon((ImageView) it.findViewById(R.id.attn_icon), "warn", col);
            ((TextView) it.findViewById(R.id.attn_t)).setText(a.title);
            ((TextView) it.findViewById(R.id.attn_s)).setText(a.sub);
            final int sid = a.serverId;
            it.setOnClickListener(x -> nav().go("server", String.valueOf(sid)));
            list.addView(it);
        }
    }

    private void servers(List<Models.ServerDto> servers, List<Models.ContainerDto> containers) {
        LinearLayout list = v(R.id.srv_list);
        list.removeAllViews();
        int[] bars = {C.CYAN, C.VIOLET, C.BLUE};
        for (Models.ServerDto sd : servers) {
            int count = 0, stopped = 0;
            for (Models.ContainerDto c : containers) {
                if (c.serverId != sd.id) continue;
                count++;
                if (!"Running".equalsIgnoreCase(c.status)) stopped++;
            }
            boolean up = isUp(sd);
            boolean warn = up && (sd.cpu >= 85 || sd.ram >= 90 || sd.disk >= 90 || stopped > 0);
            int[] vals = {(int) Math.round(sd.cpu), (int) Math.round(sd.ram), (int) Math.round(sd.disk)};
            View it = UI.inflate(list, R.layout.item_server_card);
            UI.tint(it.findViewById(R.id.sv_tile), C.BLUE, 11);
            ((TextView) it.findViewById(R.id.sv_name)).setText(sd.name);
            String sub = sd.ipAddress + " · " + count + (count == 1 ? " container" : " containers");
            if (stopped > 0) sub += " · " + stopped + " stopped";
            ((TextView) it.findViewById(R.id.sv_sub)).setText(sub);
            UI.pill((TextView) it.findViewById(R.id.sv_chip), !up ? "● Offline" : warn ? "● Warning" : "● Healthy",
                    !up ? C.RED : warn ? C.AMBER : C.GREEN);
            int[] vi = {R.id.sv_0_v, R.id.sv_1_v, R.id.sv_2_v}, fi = {R.id.sv_0_f, R.id.sv_1_f, R.id.sv_2_f};
            for (int k = 0; k < 3; k++) {
                ((TextView) it.findViewById(vi[k])).setText(up ? vals[k] + "%" : "—");
                View f = it.findViewById(fi[k]);
                f.setBackground(UI.rect(bars[k], 0, 0, 2));
                UI.fill(f, up ? vals[k] / 100f : 0f);
            }
            final int sid = sd.id;
            it.setOnClickListener(x -> nav().go("server", String.valueOf(sid)));
            list.addView(it);
        }
    }

    private void topContainers(List<Models.ContainerDto> containers) {
        LinearLayout list = v(R.id.top_list);
        list.removeAllViews();
        List<Models.ContainerDto> run = new ArrayList<>();
        for (Models.ContainerDto c : containers) if ("Running".equalsIgnoreCase(c.status)) run.add(c);
        Collections.sort(run, (a, b) -> Double.compare(b.cpu, a.cpu));
        list.setVisibility(run.isEmpty() ? View.GONE : View.VISIBLE);
        int shown = 0;
        for (Models.ContainerDto c : run) {
            if (shown == 2) break;
            if (shown > 0) {
                View div = new View(requireContext());
                div.setBackgroundColor(C.STROKE);
                list.addView(div, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UI.dp(1)));
            }
            View it = UI.inflate(list, R.layout.item_top);
            bindContainerRow(it, c, serverName(c.serverId));
            it.setOnClickListener(x -> nav().tab(1));
            list.addView(it);
            shown++;
        }
    }

    private String serverName(int id) {
        Models.Overview o = Session.overview;
        if (o != null && o.servers != null) for (Models.ServerDto s : o.servers) if (s.id == id) return s.name;
        return "Server " + id;
    }

    /** Màu và icon theo tên container (postgres, sql, còn lại là box). */
    static void bindContainerRow(View it, Models.ContainerDto c, String sub) {
        String n = c.name == null ? "" : c.name.toLowerCase();
        int tile = n.contains("sql") ? C.AMBER : n.contains("postgres") ? C.BLUE : C.VIOLET;
        String icon = n.contains("sql") || n.contains("postgres") ? "db" : "box";
        UI.tint(it.findViewById(R.id.tp_tile), tile, 8);
        UI.icon((ImageView) it.findViewById(R.id.tp_icon), icon, tile);
        ((TextView) it.findViewById(R.id.tp_name)).setText(c.name);
        ((TextView) it.findViewById(R.id.tp_sub)).setText(sub);
        boolean running = "Running".equalsIgnoreCase(c.status);
        ((TextView) it.findViewById(R.id.tp_cpu)).setText(running ? Math.round(c.cpu) + "%" : "—");
    }
}
