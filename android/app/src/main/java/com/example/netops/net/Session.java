package com.example.netops.net;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Phiên đăng nhập. JWT giữ trong RAM; refresh token ("nhớ thiết bị") lưu mã hóa trong máy
 * nếu người dùng bật "Remember session".
 */
public final class Session {
    private Session() {}

    public static volatile String token;          // JWT ngắn hạn (30 phút)
    public static volatile String refreshToken;   // đổi lấy JWT mới, sống 60 ngày
    public static volatile String email;
    public static volatile Models.Overview overview;

    // Dữ liệu tạm giữa các màn hình đăng ký / quên mật khẩu (không lưu xuống máy)
    public static String pendingEmail;
    public static String resetToken;

    private static SharedPreferences prefs;
    private static boolean remember = true;

    public static String bearer() { return token == null ? "" : "Bearer " + token; }

    public static synchronized void init(Context c) {
        if (prefs != null) return;
        Context app = c.getApplicationContext();
        try {
            MasterKey mk = new MasterKey.Builder(app).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            prefs = EncryptedSharedPreferences.create(app, "netops_session", mk,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            prefs = app.getSharedPreferences("netops_session_plain", Context.MODE_PRIVATE); // dự phòng khi Keystore lỗi
        }
        refreshToken = prefs.getString("rt", null);
        email = prefs.getString("email", null);
        remember = prefs.getBoolean("remember", true);
    }

    public static boolean hasSaved() { return refreshToken != null && !refreshToken.isEmpty(); }

    /** Sau khi đăng nhập / xác minh email thành công. */
    public static synchronized void save(AuthModels.Tokens t, String mail, boolean rememberDevice) {
        token = t.token;
        refreshToken = t.refreshToken;
        email = mail;
        remember = rememberDevice;
        persist();
    }

    /** Sau khi refresh: server cấp refresh token mới (xoay vòng) -> phải lưu lại. */
    public static synchronized void update(AuthModels.Tokens t) {
        token = t.token;
        if (t.refreshToken != null) refreshToken = t.refreshToken;
        persist();
    }

    private static void persist() {
        if (prefs == null) return;
        SharedPreferences.Editor e = prefs.edit();
        if (remember) e.putString("rt", refreshToken).putString("email", email).putBoolean("remember", true);
        else e.clear();
        e.apply();
    }

    public static synchronized void clear() {
        token = null; refreshToken = null; email = null;overview = null;
        if (prefs != null) prefs.edit().clear().apply();
    }

    /** Đăng xuất: báo server thu hồi phiên rồi xóa dữ liệu trong máy. */
    public static void logout() {
        String rt = refreshToken;
        clear();
        if (rt == null) return;
        ApiClient.plain().logout(new AuthModels.RefreshRequest(rt)).enqueue(new Callback<AuthModels.Msg>() {
            @Override public void onResponse(Call<AuthModels.Msg> c, Response<AuthModels.Msg> r) {}
            @Override public void onFailure(Call<AuthModels.Msg> c, Throwable t) {}
        });
    }

    /** "vinh.nguyen@gmail.com" -> "vi***@gmail.com" */
    public static String mask(String mail) {
        if (mail == null) return "";
        int at = mail.indexOf('@');
        if (at < 2) return mail;
        return mail.substring(0, 2) + "***" + mail.substring(at);
    }
}
