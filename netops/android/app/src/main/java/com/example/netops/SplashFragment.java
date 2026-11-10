package com.example.netops;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

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
        later(2000, () -> nav().replace("login"));
    }
}
