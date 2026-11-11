package com.example.netops;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/** Man 2FA. ResetCodeFragment dung lai layout nay voi chu va mau vien khac. */
public class OtpFragment extends BaseFragment {
    private String code = "";
    private int sec = 24, total = 30;
    private EditText et;
    private ViewGroup[] boxes = new ViewGroup[6];

    protected String title() { return "Two-factor verification"; }
    protected String body() { return "Enter the 6-digit code from your\nAuthenticator app to continue."; }
    protected String timerLabel() { return "Code refreshes in"; }
    protected String btnText() { return "Verify & Continue"; }
    protected String linkText() { return "Use a backup code instead"; }
    protected int boxStroke() { return C.STROKE; }
    protected void onVerify() { nav().go("env"); }
    /** Mã điền sẵn (bản 2FA mẫu dùng "482"; các màn mã thật để rỗng). */
    protected String initialCode() { return "482"; }
    /** >0: đếm ngược thật tới 0 (giây). 0: vòng 30 giây kiểu Authenticator. */
    protected int ttlSeconds() { return 0; }
    /** Mã người dùng đã nhập. */
    protected String enteredCode() { return code; }
    /** Đặt lại đồng hồ sau khi gửi lại mã. */
    protected void restartTimer() { sec = total; }
    protected void onLink() { et.setText(""); }

    @Override protected int layout() { return R.layout.fragment_otp; }

    @Override protected void init() {
        ((TextView) v(R.id.hero_title)).setText(title());
        ((TextView) v(R.id.hero_body)).setText(body());
        ((TextView) v(R.id.otp_label)).setText(timerLabel());
        ((TextView) v(R.id.otp_btn_t)).setText(btnText());
        ((TextView) v(R.id.otp_link)).setText(linkText());
        int[] ids = {R.id.ob0, R.id.ob1, R.id.ob2, R.id.ob3, R.id.ob4, R.id.ob5};
        for (int i = 0; i < 6; i++) boxes[i] = v(ids[i]);

        code = initialCode();
        if (ttlSeconds() > 0) { total = ttlSeconds(); sec = total; }
        et = v(R.id.otp_et);
        et.setText(code);
        et.setSelection(code.length());
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable e) {
                StringBuilder sb = new StringBuilder();
                for (char ch : e.toString().toCharArray()) if (Character.isDigit(ch) && sb.length() < 6) sb.append(ch);
                code = sb.toString();
                render();
            }
        });
        tap(R.id.otp_btn, this::onVerify);
        tap(R.id.otp_link, this::onLink);
        render();
        tick();
    }

    private void render() {
        for (int i = 0; i < 6; i++) {
            boolean filled = i < code.length(), current = i == code.length();
            ViewGroup box = boxes[i];
            TextView t = (TextView) box.getChildAt(0);
            View cursor = box.getChildAt(1);
            box.setBackground(UI.rect(filled ? C.FIELD : C.CARD, current ? C.CYAN : boxStroke(), current ? 2 : 1, 14));
            if (current) UI.glow(box, UI.alpha(C.CYAN, 0.5f), 8, 14); else UI.noGlow(box);
            t.setText(filled ? String.valueOf(code.charAt(i)) : "");
            cursor.setVisibility(!filled && current ? View.VISIBLE : View.GONE);
        }
    }

    private void tick() {
        ((RingView) v(R.id.otp_ring)).setProgress(sec / (float) total);
        ((TextView) v(R.id.otp_time)).setText(String.format("%02d:%02d", sec / 60, sec % 60));
        later(1000, () -> {
            if (ttlSeconds() > 0) { if (sec > 0) sec--; }
            else sec = sec <= 1 ? 30 : sec - 1;
            tick();
        });
    }
}
