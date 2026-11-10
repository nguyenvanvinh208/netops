package com.example.netops;

import java.util.ArrayList;

/** Back-stack don gian, khong can them thu vien navigation. */
public final class Nav {
    public static final class Route {
        public final String id;
        public final String arg;
        public Route(String id, String arg) { this.id = id; this.arg = arg; }
    }

    public interface Listener { void onNav(); }

    private static final Nav INSTANCE = new Nav();
    public static Nav get() { return INSTANCE; }

    private final ArrayList<Route> stack = new ArrayList<>();
    private Listener listener;

    private Nav() { stack.add(new Route("splash", null)); }

    public void setListener(Listener l) { listener = l; }
    public Route current() { return stack.get(stack.size() - 1); }
    public int size() { return stack.size(); }

    public void go(String id) { go(id, null); }
    public void go(String id, String arg) { stack.add(new Route(id, arg)); fire(); }

    public void replace(String id) { replace(id, null); }
    public void replace(String id, String arg) { stack.clear(); stack.add(new Route(id, arg)); fire(); }

    public boolean back() {
        if (stack.size() > 1) { stack.remove(stack.size() - 1); fire(); return true; }
        return false;
    }

    public void popTo(String id) {
        while (stack.size() > 1 && !current().id.equals(id)) stack.remove(stack.size() - 1);
        fire();
    }

    /** Bam icon tren BottomNav -> ve man goc cua tab. */
    public void tab(int i) {
        String[] roots = {"dashboard", "containers", "database", "alerts", "profile"};
        replace(roots[i]);
    }

    private void fire() { if (listener != null) listener.onNav(); }
}
