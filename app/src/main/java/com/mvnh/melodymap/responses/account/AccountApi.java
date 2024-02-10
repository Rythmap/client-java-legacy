package com.mvnh.melodymap.responses.account;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AccountApi {
    @GET("api/melomap/account.info")
    Call<AccountInfo> getAccountInfo(@Query("token") String token);

    @POST("api/melomap/account.register")
    Call<AuthResponse> register(@Body AccountRegister request);

    @POST("api/melomap/account.login")
    Call<AuthResponse> login(@Body AccountLogin request);
}
