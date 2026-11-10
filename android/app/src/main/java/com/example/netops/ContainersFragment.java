package com.example.netops;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContainersFragment extends BaseFragment {
    private String query = "";

    @Override protected int layout() { return R.layout.fragment_containers; }

    @Override protected void init() {
        ((TextView) v(R.id.hdr_title)).setText("Containers");
        tap(R.id.hdr_add, () -> nav().go("newdeploy"));
        EditText s = v(R.id.ct_search);
        s.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence a, int b, int c, int d) {}
            @Override public void onTextChanged(CharSequence a, int b, int c, int d) {}
            @Override public void afterTextChanged(Editable e) { query = e.toString(); renderList(); }
        });
        render();
    }

    private void render() {
        UI.segmented((LinearLayout) v(R.id.seg_cont), new String[]{"Containers", "Images"}, 0, i -> { if (i == 1) nav().replace("images"); });
        UI.chips((LinearLayout) v(R.id.chips_cont), new String[]{"All servers", "Server 1", "Server 2"}, Store.contServer, false,
                i -> { Store.contServer = i; render(); });
        renderList();
    }

    private void renderList() {
        ((TextView) v(R.id.hdr_sub)).setText(Store.ENV_NAMES[Store.env] + " \u00b7 " + Store.runningCount() + " running");
        LinearLayout list = v(R.id.ct_list);
        list.removeAllViews();
        int n = 0;
        for (final Data.CInfo c : Data.CONTAINERS) {
            boolean serverOk = Store.contServer == 0 || c.server == Store.contServer;
            boolean textOk = query.trim().isEmpty() || c.name.toLowerCase().contains(query.toLowerCase()) || c.id.toLowerCase().contains(query.toLowerCase());
            if (!serverOk || !textOk) continue;
            n++;
            list.addView(card(list, c));
        }
        ((TextView) v(R.id.ct_count)).setText(n + " of 3");
        v(R.id.ct_empty).setVisibility(n == 0 ? View.VISIBLE : View.GONE);
    }

    private View card(LinearLayout parent, final Data.CInfo c) {
        Object[] st = ContainerUi.statusOf(c.id);
        int col = (Integer) st[1];
        boolean up = (Boolean) st[2];
        boolean sql = c.id.equals("sql-server") && up;
        View it = UI.inflate(parent, R.layout.item_container);
        UI.tint(it.findViewById(R.id.ct_tile), c.tile, 14);
        UI.icon((ImageView) it.findViewById(R.id.ct_icon), c.icon, c.tile);
        ((TextView) it.findViewById(R.id.ct_name)).setText(c.name);
        ((TextView) it.findViewById(R.id.ct_sub)).setText(c.sub);
        UI.pill((TextView) it.findViewById(R.id.ct_status), (String) st[0], col);

        int cpu = up ? c.cpu : 0, ram = up ? c.ram : 0;
        ((TextView) it.findViewById(R.id.ct_cpu_pct)).setText(cpu + "%");
        ((TextView) it.findViewById(R.id.ct_ram_pct)).setText(ram + "%");
        View cf = it.findViewById(R.id.ct_cpu_fill), rf = it.findViewById(R.id.ct_ram_fill);
        cf.setBackground(UI.rect(sql ? C.AMBER : C.CYAN, 0, 0, 3));
        rf.setBackground(UI.rect(sql ? C.RED : C.CYAN, 0, 0, 3));
        UI.fill(cf, cpu / 100f);
        UI.fill(rf, ram / 100f);

        ContainerUi.tintButton(it.findViewById(R.id.ct_start), C.GREEN, !up, () -> { Store.start(c.id); Store.say(c.id + " started"); renderList(); });
        ContainerUi.tintButton(it.findViewById(R.id.ct_stop), C.RED, up, () -> ask(c.id, "stop"));
        ContainerUi.tintButton(it.findViewById(R.id.ct_restart), C.AMBER, up, () -> ask(c.id, "restart"));
        it.setOnClickListener(x -> nav().go("container", c.id));
        return it;
    }

    private void ask(String id, String action) { ConfirmDialog.show(act(), id, action, this::renderList); }
}
