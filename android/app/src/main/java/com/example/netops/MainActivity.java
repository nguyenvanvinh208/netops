package com.example.netops;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/** Mot Activity duy nhat: chua Fragment (moi man hinh), thanh dieu huong duoi, lop phu (dialog) va toast. */
public class MainActivity extends AppCompatActivity implements Nav.Listener {
    private FrameLayout container, overlayHost, toastHost;
    private View bottomNav, toast;
    private TextView toastText;
    private LinearLayout navRow, pillWrap;
    private int topInset, bottomInset, imeInset, leftInset, rightInset;
    private boolean wantNav;
    private final Runnable hideToast = () -> toast.setVisibility(View.GONE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        // Ve tran ca vung tai tho / camera khi xoay ngang (canh dai cua man hinh)
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        } else if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 29) {
            getWindow().setNavigationBarContrastEnforced(false);
            getWindow().setStatusBarContrastEnforced(false);
        }
        setContentView(R.layout.activity_main);

        View rootView = findViewById(R.id.root);
        WindowInsetsControllerCompat ctl = WindowCompat.getInsetsController(getWindow(), rootView);
        ctl.setAppearanceLightStatusBars(false);
        ctl.setAppearanceLightNavigationBars(false);

        container = findViewById(R.id.fragment_container);
        overlayHost = findViewById(R.id.overlay_host);
        toastHost = findViewById(R.id.toast_host);
        toast = findViewById(R.id.toast);
        toastText = findViewById(R.id.toast_text);
        bottomNav = findViewById(R.id.bottom_nav);
        navRow = bottomNav.findViewById(R.id.nav_row);
        pillWrap = bottomNav.findViewById(R.id.nav_pill_wrap);

        for (int i = 0; i < navRow.getChildCount(); i++) {
            final int idx = i;
            navRow.getChildAt(i).setOnClickListener(v -> Nav.get().tab(idx));
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            topInset = sb.top;
            leftInset = sb.left;
            rightInset = sb.right;
            bottomInset = sb.bottom;
            imeInset = ime.bottom;
            updateInsets();
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (overlayHost.getChildCount() > 0) { overlayHost.removeViewAt(overlayHost.getChildCount() - 1); return; }
                if (!Nav.get().back()) { setEnabled(false); getOnBackPressedDispatcher().onBackPressed(); }
            }
        });

        Store.toaster = this::showToast;
        Nav.get().setListener(this);
        show(Nav.get().current());
    }

    @Override public void onNav() { show(Nav.get().current()); }

    private void show(Nav.Route r) {
        overlayHost.removeAllViews();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, Router.create(r))
                .commitAllowingStateLoss();
        applyNav(Router.tabOf(r.id));
    }

    private void applyNav(int tab) {
        wantNav = tab >= 0;
        for (int i = 0; i < navRow.getChildCount(); i++) {
            LinearLayout cell = (LinearLayout) navRow.getChildAt(i);
            boolean sel = i == tab;
            int col = sel ? C.CYAN : C.DIM;
            cell.setBackground(sel ? UI.rect(UI.alpha(C.CYAN, 0.12f), 0, 0, 16) : null);
            FrameLayout f = (FrameLayout) cell.getChildAt(0);
            ((ImageView) f.getChildAt(0)).setImageTintList(ColorStateList.valueOf(col));
            ((TextView) cell.getChildAt(1)).setTextColor(col);
        }
        updateInsets();
    }

    private void updateInsets() {
        boolean navVisible = wantNav && imeInset == 0;
        bottomNav.setVisibility(navVisible ? View.VISIBLE : View.GONE);
        findViewById(R.id.root).setPadding(leftInset, 0, rightInset, 0);
        pillWrap.setPadding(0, 0, 0, bottomInset);
        container.setPadding(0, topInset, 0, navVisible ? 0 : Math.max(bottomInset, imeInset));
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) toast.getLayoutParams();
        lp.bottomMargin = UI.dp(104) + bottomInset;
        toast.setLayoutParams(lp);
    }

    public FrameLayout overlayHost() { return overlayHost; }
    public int bottomInset() { return bottomInset; }

    public void showToast(String msg) {
        toastText.setText(msg);
        toast.setVisibility(View.VISIBLE);
        toast.removeCallbacks(hideToast);
        toast.postDelayed(hideToast, 2200);
    }
}
