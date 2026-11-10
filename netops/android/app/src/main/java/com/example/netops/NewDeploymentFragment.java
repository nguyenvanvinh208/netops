package com.example.netops;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewDeploymentFragment extends BaseFragment {
    private int imgIdx = 0, server = 0;
    private boolean heal = true, menuOpen = false;
    private static final String[] FREE = {"3.2 GB RAM free", "1.1 GB RAM free"};
    private static final float[] FRAC = {0.68f, 0.28f};

    @Override protected int layout() { return R.layout.fragment_newdeploy; }

    @Override protected void init() {
        String a = arg();
        for (int i = 0; i < Data.IMAGES.length; i++) if (Data.IMAGES[i].id.equals(a)) imgIdx = i;

        final View nameBox = v(R.id.nd_name_box);
        EditText name = v(R.id.nd_name);
        name.setOnFocusChangeListener((x, f) -> nameBox.setBackground(UI.rect(C.CARD, f ? UI.alpha(C.CYAN, 0.6f) : C.STROKE, 1, 16)));

        tap(R.id.nd_img_row, () -> { menuOpen = !menuOpen; render(); });
        tap(R.id.nd_srv0, () -> { server = 0; render(); });
        tap(R.id.nd_srv1, () -> { server = 1; render(); });
        tap(R.id.nd_heal, () -> { heal = !heal; render(); });
        tap(R.id.nd_deploy, () -> {
            Store.say("Deploying " + name.getText() + " to Server " + (server + 1) + "\u2026");
            nav().replace("containers");
        });
        render();
    }

    private void render() {
        Data.Img img = Data.IMAGES[imgIdx];
        ((TextView) v(R.id.nd_img_text)).setText(img.id);
        UI.pill((TextView) v(R.id.nd_img_tag), img.tag, img.tile);

        LinearLayout menu = v(R.id.nd_menu);
        menu.setVisibility(menuOpen ? View.VISIBLE : View.GONE);
        menu.removeAllViews();
        for (int i = 0; i < Data.IMAGES.length; i++) {
            final int idx = i;
            Data.Img m = Data.IMAGES[i];
            View row = UI.inflate(menu, R.layout.item_menu_row);
            TextView n = row.findViewById(R.id.mr_name);
            n.setText(m.id);
            n.setTextColor(i == imgIdx ? C.CYAN : C.TEXT);
            n.setTypeface(null, i == imgIdx ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            UI.pill((TextView) row.findViewById(R.id.mr_tag), m.tag, m.tile);
            row.setOnClickListener(x -> { imgIdx = idx; menuOpen = false; render(); });
            menu.addView(row);
        }

        int[] cards = {R.id.nd_srv0, R.id.nd_srv1}, tiles = {R.id.nd_srv0_tile, R.id.nd_srv1_tile};
        int[] icons = {R.id.nd_srv0_icon, R.id.nd_srv1_icon}, chks = {R.id.nd_srv0_chk, R.id.nd_srv1_chk};
        int[] chkIcons = {R.id.nd_srv0_chkicon, R.id.nd_srv1_chkicon}, names = {R.id.nd_srv0_name, R.id.nd_srv1_name};
        int[] frees = {R.id.nd_srv0_free, R.id.nd_srv1_free}, bars = {R.id.nd_srv0_bar, R.id.nd_srv1_bar};
        for (int i = 0; i < 2; i++) {
            boolean sel = server == i;
            View card = v(cards[i]);
            card.setBackground(UI.rect(sel ? C.FIELD : C.CARD, sel ? C.CYAN : C.STROKE, sel ? 1.5f : 1, 18));
            if (sel) UI.glow(card, UI.alpha(C.CYAN, 0.4f), 10, 18); else UI.noGlow(card);
            UI.tint(v(tiles[i]), sel ? C.CYAN : C.MUTED, 12);
            UI.icon((ImageView) v(icons[i]), "server", sel ? C.CYAN : C.MUTED);
            v(chks[i]).setBackground(sel ? UI.grad(0, true, true) : UI.oval(0, C.STROKE, 2));
            v(chkIcons[i]).setVisibility(sel ? View.VISIBLE : View.GONE);
            ((TextView) v(names[i])).setText("Server " + (i + 1));
            ((TextView) v(frees[i])).setText(FREE[i]);
            View bar = v(bars[i]);
            bar.setBackground(UI.rect(i == 0 ? C.GREEN : C.AMBER, 0, 0, 3));
            UI.fill(bar, FRAC[i]);
        }

        View box = v(R.id.nd_heal_box);
        box.setBackground(heal ? UI.grad(8, true, false) : UI.rect(0, C.STROKE, 2, 8));
        v(R.id.nd_heal_chk).setVisibility(heal ? View.VISIBLE : View.GONE);
    }
}
