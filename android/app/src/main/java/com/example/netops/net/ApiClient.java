package com.example.netops.net;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private ApiClient() {}

    /** Địa chỉ API trên Server 1 (HTTPS qua Caddy). Phải kết thúc bằng dấu / */
    public static final String BASE_URL = "https://netops-api.koreacentral.cloudapp.azure.com/";

    private static ApiService service, plainService;

    /** Dùng cho các API cần đăng nhập: JWT hết hạn (401) thì tự đổi refresh token rồi gọi lại. */
    public static synchronized ApiService get() {
        if (service == null) service = build(new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
                .authenticator(new TokenAuthenticator()).build());
        return service;
    }

    /** Không có Authenticator: dùng cho login / refresh / logout. */
    public static synchronized ApiService plain() {
        if (plainService == null) plainService = build(new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build());
        return plainService;
    }

    private static ApiService build(OkHttpClient client) {
        return new Retrofit.Builder().baseUrl(BASE_URL).client(client)
                .addConverterFactory(GsonConverterFactory.create()).build().create(ApiService.class);
    }

    /** Khi server trả 401 cho request có JWT: lấy JWT mới bằng refresh token rồi thử lại đúng 1 lần. */
    private static final class TokenAuthenticator implements Authenticator {
        @Override public Request authenticate(Route route, Response response) throws IOException {
            Request req = response.request();
            String sent = req.header("Authorization");
            if (sent == null) return null;                       // login sai mật khẩu: không refresh
            if (response.priorResponse() != null) return null;   // đã thử lại rồi, tránh lặp vô hạn
            synchronized (Session.class) {
                String cur = Session.token;
                if (cur != null && !sent.equals("Bearer " + cur))  // luồng khác vừa refresh xong
                    return req.newBuilder().header("Authorization", "Bearer " + cur).build();
                String rt = Session.refreshToken;
                if (rt == null) return null;
                retrofit2.Response<AuthModels.Tokens> r = plain().refresh(new AuthModels.RefreshRequest(rt)).execute();
                if (r.isSuccessful() && r.body() != null && r.body().token != null) {
                    Session.update(r.body());
                    return req.newBuilder().header("Authorization", "Bearer " + Session.token).build();
                }
                if (r.code() == 401) Session.clear();            // phiên đã bị thu hồi/hết hạn
                return null;
            }
        }
    }
}
