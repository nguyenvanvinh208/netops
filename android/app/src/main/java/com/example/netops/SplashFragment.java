package com.example.netops;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.netops.net.ApiClient;
import com.example.netops.net.AuthModels;
import com.example.netops.net.Session;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashFragment extends BaseFragment {
    @Override protected int layout() { return R.layout.fragment_splash; }

    @Override protected void init() {
        final View fill = v(R.id.sp_fill);
        UI.fill(fill, 0.2f);
        ValueAnimator a = keep(ValueAnimator.ofFloat(0f, 1f));
        a.setDuration(1800);
        a.setInterpolator(new LinearInterpolator());
        a.addUpdateListener(an -> UI.fill(fill, 0.2f + 0.8f * (float) an.getAnimatedValue()));
        a.start();
        Session.init(requireContext());
        later(2000, this::autoLogin);
    }

    /** Đã nhớ thiết bị: đổi refresh token lấy JWT mới, vào thẳng app, không hỏi mật khẩu. */
    private void autoLogin() {
        if (!Session.hasSaved()) { nav().replace("login"); return; }
        ApiClient.plain().refresh(new AuthModels.RefreshRequest(Session.refreshToken)).enqueue(new Callback<AuthModels.Tokens>() {
            @Override public void onResponse(Call<AuthModels.Tokens> call, Response<AuthModels.Tokens> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null && r.body().token != null) {
                    Session.update(r.body());
                    nav().replace("env");
                } else {
                    if (r.code() == 401) Session.clear();   // phiên hết hạn / bị thu hồi
                    nav().replace("login");
                }
            }
            @Override public void onFailure(Call<AuthModels.Tokens> call, Throwable t) {
                if (!isAdded()) return;
                Store.say("Không kết nối được máy chủ");
                nav().replace("login");
            }
        });
    }
}
