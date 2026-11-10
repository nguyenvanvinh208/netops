package com.example.netops;

import android.widget.TextView;

public class DatabaseFragment extends DbBaseFragment {
    private StateButton backup;

    @Override protected int layout() { return R.layout.fragment_database; }
    @Override protected int tabIndex() { return 0; }
    @Override protected String subtitle(Data.Db db) { return db.engine + " \u00b7 Server 2"; }

    @Override protected void bind(final Data.Db db) {
        TextView chip = v(R.id.db_health);
        chip.setText("\u25cf " + db.health);
        chip.setTextColor(db.healthColor);
        chip.setBackground(UI.rect(UI.alpha(db.healthColor, 0.14f), 0, 0, 99));

        ring(0, R.id.rc0_ring, R.id.rc0_pct, R.id.rc0_lbl, R.id.rc0_sub, "Storage", db.storage, db.storageTxt, C.CYAN);
        ring(1, R.id.rc1_ring, R.id.rc1_pct, R.id.rc1_lbl, R.id.rc1_sub, "Connections", db.conn, db.connTxt, C.VIOLET);
        ring(2, R.id.rc2_ring, R.id.rc2_pct, R.id.rc2_lbl, R.id.rc2_sub, "Read/Write", db.rw, db.rwTxt, C.GREEN);
        ((TextView) v(R.id.dbt_engine)).setText(db.engine);
        ((TextView) v(R.id.dbt_last)).setText(db.lastBackup);
        ((TextView) v(R.id.dbt_rep)).setText(db.replicas);

        backup = new StateButton(v(R.id.dbt_btn), 16)
                .idle("database", C.ON_CYAN, "Trigger Backup", C.ON_CYAN, 16, UI.grad(16, false, false))
                .glow(UI.alpha(C.CYAN, 0.5f), 12)
                .done("Backup started \u00b7 snapshot queued", 15)
                .onFire(() -> Store.say("Backup queued for " + db.name));
    }

    private void ring(int i, int ringId, int pctId, int lblId, int subId, String label, int pct, String sub, int color) {
        RingView r = v(ringId);
        r.setColor(color);
        r.setProgress(pct / 100f);
        ((TextView) v(pctId)).setText(pct + "%");
        ((TextView) v(lblId)).setText(label);
        ((TextView) v(subId)).setText(sub);
    }
}
