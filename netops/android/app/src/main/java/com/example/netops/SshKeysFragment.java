package com.example.netops;

import android.widget.TextView;

public class SshKeysFragment extends BaseFragment {
    private int state = 0; // 0 active, 1 revoked

    @Override protected int layout() { return R.layout.fragment_sshkeys; }

    @Override protected void init() {
        tap(R.id.ssh_copy, () -> { UI.copy(requireContext(), "SHA256:9f2c1a\u2026a41e"); Store.say("Fingerprint copied"); });

        new StateButton(v(R.id.ssh_revoke), 16)
                .idle("trash", C.RED, "Revoke Active Key", C.RED, 16, UI.rect(UI.alpha(C.RED, 0.14f), UI.alpha(C.RED, 0.5f), 1, 16))
                .done("Key revoked", 15)
                .onFire(() -> { state = 1; render(); });
        render();
    }

    private void chip(TextView t, String text, int color, boolean dotPrefix) {
        t.setText(dotPrefix ? "\u25cf " + text : text);
        t.setTextColor(color);
        t.setBackground(UI.rect(UI.alpha(color, 0.14f), 0, 0, 99));
    }

    private void render() {
        chip((TextView) v(R.id.ssh_hdr_chip), state == 0 ? "ACTIVE" : state == 1 ? "REVOKED" : "PENDING",
                state == 0 ? C.GREEN : state == 1 ? C.RED : C.AMBER, true);
        chip((TextView) v(R.id.ssh_key_chip), state == 0 ? "CONNECTED" : state == 1 ? "REVOKED" : "VERIFYING",
                state == 0 ? C.GREEN : state == 1 ? C.RED : C.AMBER, false);
        String srv = state == 0 ? "GRANTED" : state == 1 ? "NO ACCESS" : "PENDING";
        int sc = state == 0 ? C.GREEN : state == 1 ? C.RED : C.AMBER;
        chip((TextView) v(R.id.ssh_srv0_chip), srv, sc, false);
        chip((TextView) v(R.id.ssh_srv1_chip), srv, sc, false);
        ((TextView) v(R.id.ssh_key_sub)).setText(state == 2 ? "ED25519 \u00b7 added just now" : "ED25519 \u00b7 added 12 Sep 2026");
        ((TextView) v(R.id.ssh_last)).setText(state == 1 ? "never" : "2m ago \u00b7 Server 1");
        ((TextView) v(R.id.ssh_exp)).setText(state == 1 ? "\u2014" : "in 76 days");
    }
}
