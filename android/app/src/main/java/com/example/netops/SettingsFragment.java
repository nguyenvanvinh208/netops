package com.example.netops;

import android.widget.LinearLayout;

public class SettingsFragment extends BaseFragment {
    @Override protected int layout() { return R.layout.fragment_settings; }

    @Override protected void init() {
        NetToggle n = v(R.id.st_notif), d = v(R.id.st_dark), f = v(R.id.st_face);
        n.setChecked(Store.notif); n.setOnChange(c -> Store.notif = c);
        d.setChecked(Store.darkMode); d.setOnChange(c -> Store.darkMode = c);
        f.setChecked(Store.faceId); f.setOnChange(c -> Store.faceId = c);
        pollChips();
    }

    private void pollChips() {
        UI.chips((LinearLayout) v(R.id.st_poll), new String[]{"5s", "15s", "30s", "60s"}, Store.poll, false, i -> { Store.poll = i; pollChips(); });
    }
}
