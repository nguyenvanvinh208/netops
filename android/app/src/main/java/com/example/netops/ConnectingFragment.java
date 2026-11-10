package com.example.netops;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

public class ConnectingFragment extends BaseFragment {
    private static final String[] STEPS = {
            "Resolving host netops-prod.azure\u2026", "Establishing SSH Tunnel...", "Authenticating Certs...", "Opening SignalR channel..."};
    private int step = 1;

    @Override protected int layout() { return R.layout.fragment_connecting; }

    @Override protected void init() {
        Data.Env env = Data.ENVS[Store.env];
        ((TextView) v(R.id.cn_title)).setText("Connecting to " + env.name);
        ((TextView) v(R.id.cn_sub)).setText(env.sub + " \u00b7 " + Data.ENV_SERVERS[Store.env]);
        spin(v(R.id.cn_spin));
        int[] spins = {R.id.cn_s0_spin, R.id.cn_s1_spin, R.id.cn_s2_spin, R.id.cn_s3_spin};
        for (int id : spins) spin(v(id));
        tap(R.id.cn_cancel, () -> nav().back());
        render();
        advance();
    }

    private void spin(View target) {
        ObjectAnimator a = keep(ObjectAnimator.ofFloat(target, View.ROTATION, 0f, 360f));
        a.setDuration(1100);
        a.setInterpolator(new LinearInterpolator());
        a.setRepeatCount(ObjectAnimator.INFINITE);
        a.start();
    }

    private void advance() {
        if (step < 5) later(650, () -> { step++; render(); advance(); });
        else later(550, () -> nav().replace("dashboard"));
    }

    private void render() {
        int[] ok = {R.id.cn_s0_ok, R.id.cn_s1_ok, R.id.cn_s2_ok, R.id.cn_s3_ok};
        int[] sp = {R.id.cn_s0_spin, R.id.cn_s1_spin, R.id.cn_s2_spin, R.id.cn_s3_spin};
        int[] dot = {R.id.cn_s0_dot, R.id.cn_s1_dot, R.id.cn_s2_dot, R.id.cn_s3_dot};
        int[] tx = {R.id.cn_s0_t, R.id.cn_s1_t, R.id.cn_s2_t, R.id.cn_s3_t};
        for (int i = 0; i < 4; i++) {
            int state = i < step - 1 ? 2 : (i == step - 1 ? 1 : 0);
            int c = state == 2 ? C.GREEN : state == 1 ? C.CYAN : C.DIM;
            v(ok[i]).setVisibility(state == 2 ? View.VISIBLE : View.GONE);
            v(sp[i]).setVisibility(state == 1 ? View.VISIBLE : View.GONE);
            v(dot[i]).setVisibility(state == 0 ? View.VISIBLE : View.GONE);
            TextView t = v(tx[i]);
            t.setText(STEPS[i]);
            t.setTextColor(c);
        }
    }
}
