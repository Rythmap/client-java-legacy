package com.qylex.melomap.responses.account;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;

    private String detail;

    public String getAccessToken() {
        return accessToken;
    }

    public String getDetail() {
        return detail;
    }
}
