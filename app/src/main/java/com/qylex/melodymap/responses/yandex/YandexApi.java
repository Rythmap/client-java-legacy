package com.qylex.melodymap.responses.yandex;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YandexApi {
    @GET("api/music/yandex.current_track")
    Call<YandexInfo> getCurrentTrack(@Query("token") String token);
}