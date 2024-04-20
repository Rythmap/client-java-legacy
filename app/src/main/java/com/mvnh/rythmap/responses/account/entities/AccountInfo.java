package com.mvnh.rythmap.responses.account.entities;

import com.google.gson.annotations.SerializedName;

public class AccountInfo {
    @SerializedName("token_valid")
    private boolean tokenValid;
    private String nickname;
    private String email;
    @SerializedName("email_confirmed")
    private boolean emailConfirmed;

    public boolean isTokenValid() {
        return tokenValid;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "tokenValid=" + tokenValid +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", emailConfirmed=" + emailConfirmed +
                '}';
    }
}