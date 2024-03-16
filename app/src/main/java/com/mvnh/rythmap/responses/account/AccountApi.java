package com.mvnh.rythmap.responses.account;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AccountApi {
    @GET("melomap/account.info")
    Call<AccountInfo> getAccountInfo(@Query("token") String token);

    @POST("melomap/account.register")
    Call<AuthResponse> register(@Body AccountRegister request);

    @POST("melomap/account.login")
    Call<AuthResponse> login(@Body AccountLogin request);
}
