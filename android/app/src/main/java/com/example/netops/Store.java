package com.example.netops;

import java.util.HashMap;
import java.util.Map;

/** Trang thai dung chung toan app (thay cho cac frame bien the trong Figma). */
public final class Store {
    private Store() {}

    public static int env = 0;          // 0 Production, 1 Staging, 2 Development
    public static int dashServer = 0;   // 0 All, 1 Server 1, 2 Server 2
    public static int perfRange = 1;    // 0 1H, 1 24H, 2 7D, 3 30D
    public static int contServer = 0;
    public static int dbSel = 0;        // 0 PostgreSQL, 1 SQL Server
    public static int avatar = 0;
    public static int poll = 1;
    public static boolean autoHeal = true;
    public static boolean notif = true;
    public static boolean darkMode = true;
    public static boolean faceId = true;

    public static final String[] ENV_NAMES = {"Production Azure", "Staging AWS", "Development Local"};
    public static final Map<String, String> status = new HashMap<>();
    static {
        status.put("web-api", "running");
        status.put("postgres", "running");
        status.put("sql-server", "slow");
    }

    public interface Toaster { void show(String msg); }
    public static Toaster toaster;
    public static void say(String msg) { if (toaster != null) toaster.show(msg); }

    public static String st(String id) { String s = status.get(id); return s == null ? "running" : s; }
    public static void stop(String id) { status.put(id, "stopped"); }
    public static void start(String id) { status.put(id, "sql-server".equals(id) ? "slow" : "running"); }
    public static int runningCount() {
        int n = 0;
        for (String s : status.values()) if (!"stopped".equals(s)) n++;
        return n;
    }
}
