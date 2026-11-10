package com.example.netops;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EnvFragment extends BaseFragment {
    @Override protected int layout() { return R.layout.fragment_env; }

    @Override protected void init() {
        render();
        tap(R.id.env_connect, () -> nav().go("connecting"));
    }

    private void render() {
        LinearLayout list = v(R.id.env_list);
        list.removeAllViews();
        for (int i = 0; i < Data.ENVS.length; i++) {
            final int idx = i;
            Data.Env e = Data.ENVS[i];
            boolean sel = Store.env == i;
            View it = UI.inflate(list, R.layout.item_env);
            UI.tint(it.findViewById(R.id.env_tile), e.iconColor, 16);
            UI.icon((ImageView) it.findViewById(R.id.env_icon), e.icon, e.iconColor);
            ((TextView) it.findViewById(R.id.env_name)).setText(e.name);
            UI.pill((TextView) it.findViewById(R.id.env_tag), e.tag, e.tagColor);
            ((TextView) it.findViewById(R.id.env_sub)).setText(e.sub);
            ((TextView) it.findViewById(R.id.env_servers)).setText(e.servers + " servers online");
            ((TextView) it.findViewById(R.id.env_ping)).setText(e.ping);

            it.setBackground(UI.rect(sel ? C.FIELD : C.CARD, sel ? C.CYAN : C.STROKE, sel ? 1.5f : 1, 22));
            if (sel) UI.glow(it, UI.alpha(C.CYAN, 0.4f), 12, 22); else UI.noGlow(it);
            View chk = it.findViewById(R.id.env_check);
            chk.setBackground(sel ? UI.grad(0, true, true) : UI.oval(0, C.DIM, 1.5f));
            it.findViewById(R.id.env_check_icon).setVisibility(sel ? View.VISIBLE : View.GONE);
            it.setOnClickListener(x -> { Store.env = idx; render(); });
            list.addView(it);
        }
    }
}
