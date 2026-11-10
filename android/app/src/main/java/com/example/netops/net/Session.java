package com.example.netops.net;

/** Giữ JWT sau khi đăng nhập và bản Overview mới nhất (trong bộ nhớ). */
public final class Session {
    private Session() {}
    public static String token;
    public static Models.Overview overview;
    public static String bearer() { return token == null ? "" : "Bearer " + token; }
}
