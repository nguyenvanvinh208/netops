package com.example.netops;

import android.os.Build;

import com.example.netops.net.ApiClient;
import com.example.netops.net.AuthModels;
import com.example.netops.net.Session;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Nhập mã 6 số gửi qua email để kích hoạt tài khoản (dùng lại giao diện OTP). */
public class VerifyEmailFragment extends OtpFragment {
    private boolean busy;

    @Override protected String title() { return "Verify your email"; }
    @Override protected String body() { return "We sent a 6-digit code to " + Session.mask(Session.pendingEmail)
            + ".\nIt can take a minute to arrive."; }
    @Override protected String timerLabel() { return "Code expires in"; }
    @Override protected String btnText() { return "Verify & Continue"; }
    @Override protected String linkText() { return "Resend code"; }
    @Override protected int boxStroke() { return UI.alpha(C.BLUE, 0.5f); }
    @Override protected String initialCode() { return ""; }
    @Override protected int ttlSeconds() { return 600; }

    @Override protected void onVerify() {
        final String code = enteredCode();
        if (code.length() < 6) { Store.say("Nhập đủ 6 số"); return; }
        if (busy) return;
        busy = true;
        final String email = Session.pendingEmail;
        ApiClient.plain().verifyEmail(new AuthModels.VerifyEmailRequest(email, code, Build.MODEL)).enqueue(new Callback<AuthModels.Tokens>() {
            @Override public void onResponse(Call<AuthModels.Tokens> call, Response<AuthModels.Tokens> r) {
                busy = false;
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null && r.body().token != null) {
                    Session.save(r.body(), email, true);
                    Store.say("Đã xác minh email");
                    nav().replace("env");
                } else {
                    Store.say(AuthModels.errText(r, "Xác minh thất bại"));
                }
            }
            @Override public void onFailure(Call<AuthModels.Tokens> call, Throwable t) {
                busy = false;
                if (isAdded()) Store.say("Không kết nối được máy chủ");
            }
        });
    }

    @Override protected void onLink() {
        ApiClient.plain().resendCode(new AuthModels.EmailRequest(Session.pendingEmail)).enqueue(new Callback<AuthModels.Msg>() {
            @Override public void onResponse(Call<AuthModels.Msg> call, Response<AuthModels.Msg> r) {
                if (!isAdded()) return;
                if (r.isSuccessful()) { restartTimer(); Store.say("Đã gửi lại mã"); }
                else Store.say(AuthModels.errText(r, "Không gửi lại được"));
            }
            @Override public void onFailure(Call<AuthModels.Msg> call, Throwable t) {
                if (isAdded()) Store.say("Không kết nối được máy chủ");
            }
        });
    }
}
