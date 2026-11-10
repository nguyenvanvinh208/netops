package com.example.netops;

import android.widget.EditText;
import android.widget.ImageView;

import com.example.netops.net.ApiClient;
import com.example.netops.net.Models;
import com.example.netops.net.Session;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends BaseFragment {
    private boolean busy;

    @Override protected int layout() { return R.layout.fragment_login; }

    @Override protected void init() {
        final EditText mail = v(R.id.lg_mail_et), pass = v(R.id.lg_pass_et);
        UI.bindFocus(v(R.id.lg_mail), mail, (ImageView) v(R.id.lg_mail_icon), false);
        UI.bindFocus(v(R.id.lg_pass), pass, (ImageView) v(R.id.lg_pass_icon), false);
        UI.passwordToggle(pass, (ImageView) v(R.id.lg_pass_eye));
        tap(R.id.lg_forgot, () -> nav().go("forgot"));
        tap(R.id.lg_login, () -> login(mail.getText().toString().trim(), pass.getText().toString()));
    }

    private void login(String email, String password) {
        if (busy) return;
        if (email.isEmpty() || password.isEmpty()) { Store.say("Nhập email và mật khẩu"); return; }
        busy = true;
        ApiClient.get().login(new Models.LoginRequest(email, password)).enqueue(new Callback<Models.LoginResponse>() {
            @Override public void onResponse(Call<Models.LoginResponse> call, Response<Models.LoginResponse> r) {
                busy = false;
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null && r.body().token != null) {
                    Session.token = r.body().token;
                    nav().go("otp");
                } else {
                    Store.say(r.code() == 401 ? "Email hoặc mật khẩu không đúng" : "Lỗi máy chủ (" + r.code() + ")");
                }
            }
            @Override public void onFailure(Call<Models.LoginResponse> call, Throwable t) {
                busy = false;
                if (isAdded()) Store.say("Không kết nối được máy chủ");
            }
        });
    }
}
