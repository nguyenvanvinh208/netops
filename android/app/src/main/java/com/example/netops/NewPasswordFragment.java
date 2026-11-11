package com.example.netops;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.netops.net.ApiClient;
import com.example.netops.net.AuthModels;
import com.example.netops.net.Session;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewPasswordFragment extends BaseFragment {
    private EditText p1, p2;
    private boolean busy;

    @Override protected int layout() { return R.layout.fragment_newpass; }

    @Override protected void init() {
        p1 = v(R.id.np1_et);
        p2 = v(R.id.np2_et);
        UI.bindFocus(v(R.id.np1), p1, (ImageView) v(R.id.np1_icon), false);
        UI.bindFocus(v(R.id.np2), p2, (ImageView) v(R.id.np2_icon), false);
        UI.passwordToggle(p1, (ImageView) v(R.id.np1_eye));
        UI.passwordToggle(p2, (ImageView) v(R.id.np2_eye));
        p1.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable e) { render(); }
        });
        tap(R.id.np_update, this::submit);
        render();
    }

    private void submit() {
        if (busy) return;
        String a = p1.getText().toString(), b = p2.getText().toString();
        boolean digit = false, letter = false;
        for (char c : a.toCharArray()) { if (Character.isDigit(c)) digit = true; if (Character.isLetter(c)) letter = true; }
        if (a.length() < 8 || !digit || !letter) { Store.say("Mật khẩu tối thiểu 8 ký tự, gồm chữ và số"); return; }
        if (!a.equals(b)) { Store.say("Mật khẩu nhập lại không khớp"); return; }
        busy = true;
        ApiClient.plain().resetPassword(new AuthModels.ResetRequest(Session.pendingEmail, Session.resetToken, a)).enqueue(new Callback<AuthModels.Msg>() {
            @Override public void onResponse(Call<AuthModels.Msg> call, Response<AuthModels.Msg> r) {
                busy = false;
                if (!isAdded()) return;
                if (r.isSuccessful()) {
                    Session.resetToken = null;
                    nav().go("passdone");
                } else {
                    Store.say(AuthModels.errText(r, "Không đổi được mật khẩu"));
                }
            }
            @Override public void onFailure(Call<AuthModels.Msg> call, Throwable t) {
                busy = false;
                if (isAdded()) Store.say("Không kết nối được máy chủ");
            }
        });
    }

    private void render() {
        String s = p1.getText().toString();
        boolean okLen = s.length() >= 8, okNum = false, okSym = false, upper = false;
        for (char ch : s.toCharArray()) {
            if (Character.isDigit(ch)) okNum = true;
            if (!Character.isLetterOrDigit(ch)) okSym = true;
            if (Character.isUpperCase(ch)) upper = true;
        }
        int score = (okLen ? 1 : 0) + (okNum ? 1 : 0) + (okSym ? 1 : 0) + (upper ? 1 : 0);
        String label = score <= 1 ? "Weak" : score == 2 ? "Fair" : score == 3 ? "Good" : "Strong";
        int col = score <= 1 ? C.RED : score == 2 ? C.AMBER : C.GREEN;
        int[] bars = {R.id.np_b0, R.id.np_b1, R.id.np_b2, R.id.np_b3};
        for (int i = 0; i < 4; i++) v(bars[i]).setBackground(UI.rect(i < score ? col : C.STROKE, 0, 0, 2));
        TextView l = v(R.id.np_label);
        l.setText(label);
        l.setTextColor(col);
        int[] reqs = {R.id.np_r0, R.id.np_r1, R.id.np_r2};
        int[] icons = {R.id.np_r0_i, R.id.np_r1_i, R.id.np_r2_i};
        int[] texts = {R.id.np_r0_t, R.id.np_r1_t, R.id.np_r2_t};
        boolean[] ok = {okLen, okNum, okSym};
        for (int i = 0; i < 3; i++) {
            int c = ok[i] ? C.GREEN : C.DIM;
            v(reqs[i]).setBackground(UI.rect(UI.alpha(c, 0.10f), 0, 0, 10));
            UI.iconTint((ImageView) v(icons[i]), c);
            ((TextView) v(texts[i])).setTextColor(ok[i] ? C.TEXT : C.MUTED);
        }
    }
}
