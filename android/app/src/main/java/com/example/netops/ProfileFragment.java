package com.example.netops;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileFragment extends BaseFragment {
    private static final String[] NAMES = {"Classic", "Shield", "Server", "Bolt"};

    @Override protected int layout() { return R.layout.fragment_profile; }

    @Override protected void init() {
        UI.avatar((ViewGroup) v(R.id.pf_avatar), Store.avatar, false);
        tap(R.id.pf_avatar_box, this::openSheet);
        tap(R.id.pf_link_ssh, () -> nav().go("sshkeys"));
        tap(R.id.pf_link_set, () -> nav().go("settings"));
        tap(R.id.pf_logout, () -> { com.example.netops.net.Session.logout(); nav().replace("login"); });
    }

    /** Bang chon avatar (lop phu day man hinh). */
    private void openSheet() {
        final FrameLayout host = act().overlayHost();
        final View sheet = LayoutInflater.from(act()).inflate(R.layout.view_avatar_sheet, host, false);
        View card = sheet.findViewById(R.id.sh_card);
        card.setPadding(card.getPaddingLeft(), card.getPaddingTop(), card.getPaddingRight(), UI.dp(34) + act().bottomInset());
        final LinearLayout row = sheet.findViewById(R.id.sh_avatars);
        fillAvatars(row);
        final Runnable close = () -> host.removeView(sheet);
        sheet.setOnClickListener(x -> close.run());
        sheet.findViewById(R.id.sh_done).setOnClickListener(x -> close.run());
        host.addView(sheet);
    }

    private void fillAvatars(final LinearLayout row) {
        row.removeAllViews();
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            boolean sel = Store.avatar == i;
            ViewGroup opt = (ViewGroup) UI.inflate(row, R.layout.item_avatar_opt);
            UI.avatar((ViewGroup) opt.getChildAt(0), i, sel);
            TextView name = (TextView) opt.getChildAt(1);
            name.setText(NAMES[i]);
            name.setTextColor(sel ? C.CYAN : C.MUTED);
            opt.setOnClickListener(x -> {
                Store.avatar = idx;
                fillAvatars(row);
                UI.avatar((ViewGroup) v(R.id.pf_avatar), Store.avatar, false);
            });
            row.addView(opt);
        }
    }
}
