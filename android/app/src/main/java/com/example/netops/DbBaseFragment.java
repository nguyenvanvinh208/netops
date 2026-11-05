package com.example.netops;

import android.widget.LinearLayout;
import android.widget.TextView;

/** Khung chung 3 man Database: tieu de + segmented (Tracker / Restore / Config) + chip chon DB. */
public abstract class DbBaseFragment extends BaseFragment {
    protected abstract int tabIndex();
    protected abstract String subtitle(Data.Db db);
    protected abstract void bind(Data.Db db);

    protected Data.Db db() { return Data.DBS[Store.dbSel]; }

    @Override protected void init() { refresh(); }

    protected void refresh() {
        final String[] routes = {"database", "restore", "backupcfg"};
        UI.segmented((LinearLayout) v(R.id.seg_db), new String[]{"Tracker", "Restore Points", "Config"}, tabIndex(), i -> nav().replace(routes[i]));
        UI.chips((LinearLayout) v(R.id.chips_db), new String[]{"PostgreSQL", "SQL Server"}, Store.dbSel, true, i -> { Store.dbSel = i; refresh(); });
        ((TextView) v(R.id.hdr_sub)).setText(subtitle(db()));
        bind(db());
    }
}
