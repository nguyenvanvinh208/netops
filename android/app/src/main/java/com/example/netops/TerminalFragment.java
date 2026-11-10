package com.example.netops;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Terminal SSH gia lap: go help / clear / docker ps / uptime / whoami. */
public class TerminalFragment extends BaseFragment {
    private Data.CInfo c;
    private LinearLayout lines;
    private ScrollView scroll;
    private EditText input;
    private String last = "";

    @Override protected int layout() { return R.layout.fragment_terminal; }

    @Override protected void init() {
        c = Data.cinfo(arg());
        String host = "srv" + c.server;
        ((TextView) v(R.id.hdr_title)).setText(c.id);
        ((TextView) v(R.id.hdr_sub)).setText("root@" + host + " \u00b7 SSH connected");
        ((TextView) v(R.id.tm_path)).setText("  root@" + host + ":~/" + c.id);
        lines = v(R.id.tm_lines);
        scroll = v(R.id.tm_scroll);
        input = v(R.id.tm_et);

        add("$ docker logs -f " + c.id, C.CYAN);
        if (c.id.equals("web-api")) {
            add("[14:05:02] INFO  Listening on :80", C.TEXT); add("[14:05:02] INFO  Application started", C.TEXT);
            add("[14:05:09] GET /api/v2/servers 200 12ms", C.MUTED); add("[14:05:11] GET /api/v2/containers 200 18ms", C.MUTED);
            add("[14:05:14] WARN  slow query 312ms (Sync)", C.AMBER); add("[14:05:16] POST /api/v2/deploy 202 41ms", C.MUTED);
            add("[14:05:19] INFO  health ok cpu=38% mem=62%", C.GREEN); add("[14:05:22] GET /api/v2/alerts 200 9ms", C.MUTED);
            add("[14:05:25] ERROR sql-server timeout (1/3)", C.RED); add("[14:05:27] INFO  reconnected sql-server", C.GREEN);
            add("[14:05:30] GET /api/v2/metrics 200 7ms", C.MUTED); add("[14:05:33] INFO  auto-heal: no action needed", C.GREEN);
        } else {
            add("[14:05:02] LOG  database system is ready", C.TEXT); add("[14:05:09] LOG  checkpoint complete: wrote 412 buffers", C.MUTED);
            add("[14:05:14] WARN  slow query 312ms", C.AMBER); add("[14:05:19] INFO  health ok cpu=" + c.cpu + "% mem=" + c.ram + "%", C.GREEN);
            add("[14:05:27] LOG  connection authorized: user=netops", C.MUTED); add("[14:05:33] INFO  auto-heal: no action needed", C.GREEN);
        }

        input.setOnEditorActionListener((tv, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || (ev != null && ev.getKeyCode() == KeyEvent.KEYCODE_ENTER)) { run(input.getText().toString().trim()); return true; }
            return false;
        });
        tap(R.id.key_esc, () -> setInput(""));
        tap(R.id.key_ctrl, () -> Store.say("CTRL armed"));
        tap(R.id.key_tab, () -> { String in = input.getText().toString(); if (!in.isEmpty() && "docker ps".startsWith(in)) setInput("docker ps"); });
        tap(R.id.key_up, () -> { if (!last.isEmpty()) setInput(last); });
        tap(R.id.key_down, () -> setInput(""));
        tap(R.id.key_left, () -> { String in = input.getText().toString(); if (!in.isEmpty()) setInput(in.substring(0, in.length() - 1)); });
        tap(R.id.key_right, () -> run(input.getText().toString().trim()));
    }

    private void setInput(String s) { input.setText(s); input.setSelection(s.length()); }

    private void add(String text, int color) {
        TextView t = (TextView) UI.inflate(lines, R.layout.item_term_line);
        t.setText(text.replace("  ", " \u00a0"));
        t.setTextColor(color);
        lines.addView(t);
        scroll.post(() -> scroll.smoothScrollTo(0, scroll.getChildAt(0).getHeight()));
    }

    private void run(String cmd) {
        add("$ " + cmd, C.CYAN);
        last = cmd;
        if (cmd.isEmpty()) { /* khong lam gi */ }
        else if (cmd.equals("clear")) lines.removeAllViews();
        else if (cmd.equals("help")) add("commands: help, clear, docker ps, uptime, whoami", C.MUTED);
        else if (cmd.equals("uptime")) add(" 14:06:01 up " + c.up + ", load average: 0.38, 0.41, 0.40", C.TEXT);
        else if (cmd.equals("whoami")) add("root", C.TEXT);
        else if (cmd.equals("docker ps")) { add("CONTAINER ID   IMAGE         STATUS", C.MUTED); add("a41e9c2b7d10   " + c.image + "   Up " + c.up, C.GREEN); }
        else add("bash: " + cmd.split(" ")[0] + ": command not found", C.RED);
        setInput("");
    }
}
