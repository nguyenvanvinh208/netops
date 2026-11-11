package com.example.netops;

import android.os.Build;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.netops.net.ApiClient;
import com.example.netops.net.AuthModels;
import com.example.netops.net.Session;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends BaseFragment {
    private boolean busy;

    @Override protected int layout() { return R.layout.fragment_login; }

    @Override protected void init() {
        Session.init(requireContext());
        final EditText mail = v(R.id.lg_mail_et), pass = v(R.id.lg_pass_et);
        final NetToggle remember = v(R.id.lg_toggle);
        if (Session.email != null) mail.setText(Session.email);
        UI.bindFocus(v(R.id.lg_mail), mail, (ImageView) v(R.id.lg_mail_icon), false);
        UI.bindFocus(v(R.id.lg_pass), pass, (ImageView) v(R.id.lg_pass_icon), false);
        UI.passwordToggle(pass, (ImageView) v(R.id.lg_pass_eye));
        tap(R.id.lg_forgot, () -> nav().go("forgot"));
        tap(R.id.lg_login, () -> login(mail.getText().toString().trim(), pass.getText().toString(), remember.isChecked()));
    }

    private void login(final String email, String password, final boolean remember) {
        if (busy) return;
        if (email.isEmpty() || password.isEmpty()) { Store.say("Nhập email và mật khẩu"); return; }
        busy = true;
        ApiClient.plain().login(new AuthModels.LoginRequest(email, password, Build.MODEL)).enqueue(new Callback<AuthModels.Tokens>() {
            @Override public void onResponse(Call<AuthModels.Tokens> call, Response<AuthModels.Tokens> r) {
                busy = false;
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null && r.body().token != null) {
                    Session.save(r.body(), email.toLowerCase(), remember);
                    nav().replace("env");
                } else if (r.code() == 403 && "EMAIL_NOT_VERIFIED".equals(AuthModels.err(r).code)) {
                    Session.pendingEmail = email.toLowerCase();
                    Store.say("Email chưa xác minh, đã gửi mã mới");
                    nav().go("verifyemail");
                } else {
                    Store.say(r.code() == 401 ? "Email hoặc mật khẩu không đúng" : "Lỗi máy chủ (" + r.code() + ")");
                }
            }
            @Override public void onFailure(Call<AuthModels.Tokens> call, Throwable t) {
                busy = false;
                if (isAdded()) Store.say("Không kết nối được máy chủ");
            }
        });
    }
}
