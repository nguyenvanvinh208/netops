package com.example.netops;

import android.widget.EditText;
import android.widget.ImageView;

public class LoginFragment extends BaseFragment {
    @Override protected int layout() { return R.layout.fragment_login; }

    @Override protected void init() {
        EditText mail = v(R.id.lg_mail_et), pass = v(R.id.lg_pass_et);
        UI.bindFocus(v(R.id.lg_mail), mail, (ImageView) v(R.id.lg_mail_icon), false);
        UI.bindFocus(v(R.id.lg_pass), pass, (ImageView) v(R.id.lg_pass_icon), false);
        UI.passwordToggle(pass, (ImageView) v(R.id.lg_pass_eye));
        tap(R.id.lg_forgot, () -> nav().go("forgot"));
        tap(R.id.lg_login, () -> nav().go("otp"));
    }
}
