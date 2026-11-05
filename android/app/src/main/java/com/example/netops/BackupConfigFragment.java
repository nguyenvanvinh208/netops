package com.example.netops;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BackupConfigFragment extends DbBaseFragment {
    private boolean compress = true;
    private int storage = 1, sched = 0;
    private StateButton instant;

    private static final String[][] OPTS = {
            {"Local disk", "/var/backups \u00b7 fastest restore", "hdd"},
            {"Azure Blob", "netopsprod \u00b7 geo-redundant", "cloud"},
            {"Cloud Cold Storage", "Archive tier \u00b7 90-day retention", "snow"}};
    private static final int[] OPT_COL = {C.CYAN, C.BLUE, C.VIOLET};

    @Override protected int layout() { return R.layout.fragment_backupcfg; }
    @Override protected int tabIndex() { return 2; }
    @Override protected String subtitle(Data.Db db) { return db.name + " \u00b7 schedule & storage"; }

    @Override protected void init() {
        NetToggle t = v(R.id.bc_toggle);
        t.setChecked(compress);
        t.setOnChange(c -> { compress = c; refresh(); });
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            View o = v(new int[]{R.id.bc_o0, R.id.bc_o1, R.id.bc_o2}[i]);
            o.setOnClickListener(x -> { storage = idx; refresh(); });
        }
        super.init();
    }

    @Override protected void bind(final Data.Db db) {
        ((TextView) v(R.id.bc_sub)).setText(compress ? db.compressSave : "No compression");

        int[] rows = {R.id.bc_o0, R.id.bc_o1, R.id.bc_o2}, tiles = {R.id.bc_o0_tile, R.id.bc_o1_tile, R.id.bc_o2_tile};
        int[] icons = {R.id.bc_o0_icon, R.id.bc_o1_icon, R.id.bc_o2_icon}, ts = {R.id.bc_o0_t, R.id.bc_o1_t, R.id.bc_o2_t};
        int[] ss = {R.id.bc_o0_s, R.id.bc_o1_s, R.id.bc_o2_s}, chk = {R.id.bc_o0_chk, R.id.bc_o1_chk, R.id.bc_o2_chk};
        int[] chkIcon = {R.id.bc_o0_chkicon, R.id.bc_o1_chkicon, R.id.bc_o2_chkicon};
        for (int i = 0; i < 3; i++) {
            boolean sel = storage == i;
            View row = v(rows[i]);
            row.setBackground(UI.rect(sel ? C.FIELD : C.CARD, sel ? C.CYAN : C.STROKE, sel ? 1.5f : 1, 18));
            if (sel) UI.glow(row, UI.alpha(C.CYAN, 0.35f), 10, 18); else UI.noGlow(row);
            UI.tint(v(tiles[i]), OPT_COL[i], 12);
            UI.icon((ImageView) v(icons[i]), OPTS[i][2], OPT_COL[i]);
            ((TextView) v(ts[i])).setText(OPTS[i][0]);
            ((TextView) v(ss[i])).setText(OPTS[i][1]);
            v(chk[i]).setBackground(sel ? UI.grad(0, true, true) : UI.oval(0, C.STROKE, 2));
            v(chkIcon[i]).setVisibility(sel ? View.VISIBLE : View.GONE);
        }

        UI.chips((LinearLayout) v(R.id.bc_chips), new String[]{"Daily", "Weekly", "Monthly"}, sched, false, i -> { sched = i; refresh(); });
        ((TextView) v(R.id.bc_runs)).setText(sched == 0 ? "Runs every day at" : sched == 1 ? "Runs every Sunday at" : "Runs on the 1st at");
        ((TextView) v(R.id.bc_time)).setText(db.time);

        instant = new StateButton(v(R.id.bc_btn), 16)
                .idle("database", C.ON_CYAN, "Trigger Instant DB Backup", C.ON_CYAN, 16, UI.grad(16, false, false))
                .glow(UI.alpha(C.CYAN, 0.5f), 12)
                .done("Backup started \u00b7 0 / " + db.snaps[0].size, 15)
                .onFire(() -> Store.say("Instant backup started for " + db.name));
    }
}
