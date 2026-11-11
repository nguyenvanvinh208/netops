package com.example.netops.net;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    // ----- đăng ký / đăng nhập -----
    @POST("api/Auth/register")
    Call<AuthModels.Msg> register(@Body AuthModels.RegisterRequest body);

    @POST("api/Auth/verify-email")
    Call<AuthModels.Tokens> verifyEmail(@Body AuthModels.VerifyEmailRequest body);

    @POST("api/Auth/resend-code")
    Call<AuthModels.Msg> resendCode(@Body AuthModels.EmailRequest body);

    @POST("api/Auth/login")
    Call<AuthModels.Tokens> login(@Body AuthModels.LoginRequest body);

    @POST("api/Auth/refresh")
    Call<AuthModels.Tokens> refresh(@Body AuthModels.RefreshRequest body);

    @POST("api/Auth/logout")
    Call<AuthModels.Msg> logout(@Body AuthModels.RefreshRequest body);

    // ----- quên mật khẩu -----
    @POST("api/Auth/forgot-password")
    Call<AuthModels.Msg> forgotPassword(@Body AuthModels.EmailRequest body);

    @POST("api/Auth/verify-reset-code")
    Call<AuthModels.ResetGrant> verifyResetCode(@Body AuthModels.VerifyResetRequest body);

    @POST("api/Auth/reset-password")
    Call<AuthModels.Msg> resetPassword(@Body AuthModels.ResetRequest body);

    // ----- dữ liệu -----
    @GET("api/Home/overview")
    Call<Models.Overview> overview(@Header("Authorization") String bearer);
}
