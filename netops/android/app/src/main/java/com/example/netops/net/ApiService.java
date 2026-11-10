package com.example.netops.net;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/Auth/login")
    Call<Models.LoginResponse> login(@Body Models.LoginRequest body);

    @GET("api/Home/overview")
    Call<Models.Overview> overview(@Header("Authorization") String bearer);
}
