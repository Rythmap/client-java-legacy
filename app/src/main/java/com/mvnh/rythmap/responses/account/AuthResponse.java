package com.mvnh.rythmap.responses.account;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;

    private String detail;

    private String status;

    public String getAccessToken() {
        return accessToken;
    }

    public String getDetail() {
        return detail;
    }

    private String getStatus() {
        return status;
    }
}
