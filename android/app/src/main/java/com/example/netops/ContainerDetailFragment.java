package com.example.netops;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContainerDetailFragment extends BaseFragment {
    private Data.CInfo c;

    @Override protected int layout() { return R.layout.fragment_container; }

    @Override protected void init() {
        c = Data.cinfo(arg());
        ((TextView) v(R.id.hdr_title)).setText(c.name);
        ((TextView) v(R.id.hdr_sub)).setText(c.id + " \u00b7 Server " + c.server);
        tap(R.id.cd_copy, () -> { UI.copy(requireContext(), c.digest); Store.say("Digest copied"); });
        tap(R.id.cd_ssh, () -> nav().go("terminal", c.id));
        render();
    }

    private void render() {
        Object[] st = ContainerUi.statusOf(c.id);
        int col = (Integer) st[1];
        boolean up = (Boolean) st[2];
        TextView chip = v(R.id.cd_status);
        chip.setText((String) st[0]);
        chip.setTextColor(col);
        chip.setBackground(UI.rect(UI.alpha(col, 0.14f), 0, 0, 99));

        int cpu = up ? c.cpu : 0, ram = up ? c.ram : 0;
        ((RingView) v(R.id.cd_cpu_ring)).setProgress(cpu / 100f);
        ((RingView) v(R.id.cd_ram_ring)).setProgress(ram / 100f);
        ((TextView) v(R.id.cd_cpu_pct)).setText(cpu + "%");
        ((TextView) v(R.id.cd_ram_pct)).setText(ram + "%");
        ((TextView) v(R.id.cd_cpu_sub)).setText(up ? c.vcpu : "\u2014");
        ((TextView) v(R.id.cd_ram_sub)).setText(up ? c.ramGb : "\u2014");

        ((TextView) v(R.id.cd_up)).setText(up ? c.up : "\u2014");
        ((TextView) v(R.id.cd_image)).setText(c.image);
        ((TextView) v(R.id.cd_digest)).setText(c.digest);
        ((TextView) v(R.id.cd_policy)).setText(c.policy);

        LinearLayout ports = v(R.id.cd_ports);
        ports.removeAllViews();
        for (String p : c.ports) {
            TextView t = new TextView(requireContext());
            t.setText(p);
            t.setTextSize(11);
            t.setTextColor(C.CYAN);
            t.setTypeface(android.graphics.Typeface.MONOSPACE);
            t.setIncludeFontPadding(false);
            t.setPadding(UI.dp(8), UI.dp(3), UI.dp(8), UI.dp(3));
            t.setBackground(UI.rect(UI.alpha(C.CYAN, 0.12f), 0, 0, 8));
            ports.addView(t);
        }

        View restart = v(R.id.cd_restart), stop = v(R.id.cd_stop), start = v(R.id.cd_start);
        restart.setVisibility(up ? View.VISIBLE : View.GONE);
        stop.setVisibility(up ? View.VISIBLE : View.GONE);
        start.setVisibility(up ? View.GONE : View.VISIBLE);
        ContainerUi.tintButton(restart, C.AMBER, true, () -> ConfirmDialog.show(act(), c.id, "restart", this::render));
        ContainerUi.tintButton(stop, C.RED, true, () -> ConfirmDialog.show(act(), c.id, "stop", this::render));
        ContainerUi.tintButton(start, C.GREEN, true, () -> { Store.start(c.id); Store.say(c.id + " started"); render(); });
    }
}
