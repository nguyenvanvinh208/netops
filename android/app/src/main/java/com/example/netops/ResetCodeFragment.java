package com.example.netops;

import com.example.netops.net.ApiClient;
import com.example.netops.net.AuthModels;
import com.example.netops.net.Session;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Quên mật khẩu, bước 2: nhập mã 6 số nhận qua email. */
public class ResetCodeFragment extends OtpFragment {
    private boolean busy;

    @Override protected String title() { return "Check your email"; }
    @Override protected String body() { return "We sent a 6-digit reset code to " + Session.mask(Session.pendingEmail); }
    @Override protected String timerLabel() { return "Code expires in"; }
    @Override protected String btnText() { return "Verify code"; }
    @Override protected String linkText() { return "Resend code"; }
    @Override protected int boxStroke() { return UI.alpha(C.BLUE, 0.5f); }
    @Override protected String initialCode() { return ""; }
    @Override protected int ttlSeconds() { return 600; }

    @Override protected void onVerify() {
        final String code = enteredCode();
        if (code.length() < 6) { Store.say("Nhập đủ 6 số"); return; }
        if (busy) return;
        busy = true;
        ApiClient.plain().verifyResetCode(new AuthModels.VerifyResetRequest(Session.pendingEmail, code)).enqueue(new Callback<AuthModels.ResetGrant>() {
            @Override public void onResponse(Call<AuthModels.ResetGrant> call, Response<AuthModels.ResetGrant> r) {
                busy = false;
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null && r.body().resetToken != null) {
                    Session.resetToken = r.body().resetToken;
                    nav().go("newpass");
                } else {
                    Store.say(AuthModels.errText(r, "Mã không đúng"));
                }
            }
            @Override public void onFailure(Call<AuthModels.ResetGrant> call, Throwable t) {
                busy = false;
                if (isAdded()) Store.say("Không kết nối được máy chủ");
            }
        });
    }

    @Override protected void onLink() {
        ApiClient.plain().forgotPassword(new AuthModels.EmailRequest(Session.pendingEmail)).enqueue(new Callback<AuthModels.Msg>() {
            @Override public void onResponse(Call<AuthModels.Msg> call, Response<AuthModels.Msg> r) {
                if (!isAdded()) return;
                if (r.isSuccessful()) { restartTimer(); Store.say("Nếu email tồn tại, mã mới đã được gửi"); }
                else Store.say(AuthModels.errText(r, "Không gửi lại được"));
            }
            @Override public void onFailure(Call<AuthModels.Msg> call, Throwable t) {
                if (isAdded()) Store.say("Không kết nối được máy chủ");
            }
        });
    }
}
