package com.example.netops;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class NewPasswordFragment extends BaseFragment {
    private EditText p1;

    @Override protected int layout() { return R.layout.fragment_newpass; }

    @Override protected void init() {
        p1 = v(R.id.np1_et);
        EditText p2 = v(R.id.np2_et);
        UI.bindFocus(v(R.id.np1), p1, (ImageView) v(R.id.np1_icon), false);
        UI.bindFocus(v(R.id.np2), p2, (ImageView) v(R.id.np2_icon), false);
        UI.passwordToggle(p1, (ImageView) v(R.id.np1_eye));
        UI.passwordToggle(p2, (ImageView) v(R.id.np2_eye));
        p1.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable e) { render(); }
        });
        tap(R.id.np_update, () -> nav().go("passdone"));
        render();
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
