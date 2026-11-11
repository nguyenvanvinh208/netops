package com.example.netops.net;

import com.google.gson.Gson;

import retrofit2.Response;

/** Các lớp dữ liệu cho đăng ký / đăng nhập / quên mật khẩu (khớp JSON của AuthController). */
public final class AuthModels {
    private AuthModels() {}

    public static class LoginRequest {
        public String email, password, deviceName;
        public LoginRequest(String email, String password, String deviceName) { this.email = email; this.password = password; this.deviceName = deviceName; }
    }

    public static class VerifyEmailRequest {
        public String email, code, deviceName;
        public VerifyEmailRequest(String email, String code, String deviceName) { this.email = email; this.code = code; this.deviceName = deviceName; }
    }
    public static class EmailRequest {
        public String email;
        public EmailRequest(String email) { this.email = email; }
    }
    public static class VerifyResetRequest {
        public String email, code;
        public VerifyResetRequest(String email, String code) { this.email = email; this.code = code; }
    }
    public static class ResetRequest {
        public String email, resetToken, newPassword;
        public ResetRequest(String email, String resetToken, String newPassword) { this.email = email; this.resetToken = resetToken; this.newPassword = newPassword; }
    }
    public static class RefreshRequest {
        public String refreshToken;
        public RefreshRequest(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class Tokens {
        public String message, token, refreshToken, email, role;
        public int expiresInSeconds;
    }
    public static class ResetGrant { public String resetToken; }
    public static class Msg {
        public String message, code, email;
        public int expiresInSeconds, resendAfterSeconds;
    }

    /** Đọc {message, code} từ phản hồi lỗi của server. */
    public static Msg err(Response<?> r) {
        try {
            if (r.errorBody() != null) {
                Msg m = new Gson().fromJson(r.errorBody().string(), Msg.class);
                if (m != null) return m;
            }
        } catch (Exception ignored) {}
        return new Msg();
    }

    public static String errText(Response<?> r, String fallback) {
        Msg m = err(r);
        return m.message != null ? m.message : fallback + " (" + r.code() + ")";
    }
}
