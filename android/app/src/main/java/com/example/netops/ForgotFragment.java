package com.example.netops;

import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.netops.net.ApiClient;
import com.example.netops.net.AuthModels;
import com.example.netops.net.Session;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Quên mật khẩu, bước 1: nhập email -> server gửi mã 6 số. */
public class ForgotFragment extends BaseFragment {
    private boolean busy;

    @Override protected int layout() { return R.layout.fragment_forgot; }

    @Override protected void init() {
        final EditText et = v(R.id.fg_in_et);
        UI.bindFocus(v(R.id.fg_in), et, (ImageView) v(R.id.fg_in_icon), true);
        tap(R.id.fg_send, () -> send(et.getText().toString().trim().toLowerCase()));
        tap(R.id.fg_back, () -> nav().popTo("login"));
    }

    private void send(final String email) {
        if (busy) return;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { Store.say("Email không hợp lệ"); return; }
        busy = true;
        ApiClient.plain().forgotPassword(new AuthModels.EmailRequest(email)).enqueue(new Callback<AuthModels.Msg>() {
            @Override public void onResponse(Call<AuthModels.Msg> call, Response<AuthModels.Msg> r) {
                busy = false;
                if (!isAdded()) return;
                if (r.isSuccessful()) {
                    Session.pendingEmail = email;
                    nav().go("resetcode");
                } else {
                    Store.say(AuthModels.errText(r, "Không gửi được mã"));
                }
            }
            @Override public void onFailure(Call<AuthModels.Msg> call, Throwable t) {
                busy = false;
                if (isAdded()) Store.say("Không kết nối được máy chủ");
            }
        });
    }
}
