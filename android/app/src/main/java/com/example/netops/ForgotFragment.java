package com.example.netops;

import android.widget.EditText;
import android.widget.ImageView;

public class ForgotFragment extends BaseFragment {
    @Override protected int layout() { return R.layout.fragment_forgot; }

    @Override protected void init() {
        UI.bindFocus(v(R.id.fg_in), (EditText) v(R.id.fg_in_et), (ImageView) v(R.id.fg_in_icon), true);
        tap(R.id.fg_send, () -> nav().go("resetcode"));
        tap(R.id.fg_back, () -> nav().popTo("login"));
    }
}
