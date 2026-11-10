package com.example.netops.net;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private ApiClient() {}

    /** Địa chỉ API trên Server 1 (HTTPS qua Caddy). Phải kết thúc bằng dấu / */
    public static final String BASE_URL = "https://netops-api.koreacentral.cloudapp.azure.com/";

    private static ApiService service;

    public static ApiService get() {
        if (service == null) {
            service = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService.class);
        }
        return service;
    }
}
