package com.mvnh.rythmap.responses.account;

import com.mvnh.rythmap.responses.account.entities.AccountInfo;
import com.mvnh.rythmap.responses.account.entities.AccountLogin;
import com.mvnh.rythmap.responses.account.entities.AccountRegister;
import com.mvnh.rythmap.responses.account.entities.AuthResponse;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AccountApi {
    @GET("account.info")
    Call<AccountInfo> getAccountInfo(@Query("token") String token);

    @POST("account.register")
    Call<AuthResponse> register(@Body AccountRegister request);

    @POST("account.login")
    Call<AuthResponse> login(@Body AccountLogin request);

    @Multipart
    @POST("upload-avatar")
    Call<ResponseBody> uploadAvatar(@Query("token") String token, @Part MultipartBody.Part file);

    @GET("avatar/{nickname}")
    Call<ResponseBody> getAvatar(@Path("nickname") String nickname);
}
