package com.example.netops;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RestoreFragment extends DbBaseFragment {
    @Override protected int layout() { return R.layout.fragment_restore; }
    @Override protected int tabIndex() { return 1; }
    @Override protected String subtitle(Data.Db db) { return db.name + " \u00b7 restore points"; }

    @Override protected void bind(Data.Db db) {
        ((RingView) v(R.id.rs_ring)).setProgress(db.points / 30f);
        ((TextView) v(R.id.rs_num)).setText(String.valueOf(db.points));
        ((TextView) v(R.id.rs_sub)).setText("Latest 2h ago \u00b7 Total " + db.total);
        LinearLayout list = v(R.id.rs_list);
        list.removeAllViews();
        for (final Data.Snap s : db.snaps) {
            int tile = s.manual ? C.VIOLET : C.CYAN;
            View it = UI.inflate(list, R.layout.item_snapshot);
            UI.tint(it.findViewById(R.id.sn_tile), tile, 13);
            UI.icon((ImageView) it.findViewById(R.id.sn_icon), "archive", tile);
            ((TextView) it.findViewById(R.id.sn_title)).setText(s.title);
            ((TextView) it.findViewById(R.id.sn_src)).setText(s.src);
            ((TextView) it.findViewById(R.id.sn_size)).setText(s.size);
            UI.pill((TextView) it.findViewById(R.id.sn_kind), s.manual ? "MANUAL" : "AUTO", tile);

            new StateButton(it.findViewById(R.id.sn_restore), 12)
                    .idle("restore", C.CYAN, "Restore", C.CYAN, 13, UI.rect(UI.alpha(C.CYAN, 0.12f), UI.alpha(C.CYAN, 0.5f), 1, 12))
                    .done("Restoring\u2026", 13)
                    .onFire(() -> Store.say("Restoring " + s.title));
            new StateButton(it.findViewById(R.id.sn_download), 12)
                    .idle("download", C.TEXT, "Download", C.TEXT, 13, UI.rect(C.CARD, C.STROKE, 1, 12))
                    .done("Saved", 13)
                    .onFire(() -> Store.say("Saved " + s.size + " snapshot"));
            list.addView(it);
        }
    }
}
