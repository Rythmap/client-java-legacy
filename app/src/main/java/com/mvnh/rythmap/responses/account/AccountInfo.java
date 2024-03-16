package com.mvnh.rythmap.responses.account;

import com.google.gson.annotations.SerializedName;

public class AccountInfo {
    @SerializedName("token_valid")
    private boolean tokenValid;
    private String username;
    private String email;
    @SerializedName("email_confirmed")
    private boolean emailConfirmed;

    public boolean isTokenValid() {
        return tokenValid;
    }

    public String getUsername() {
        return username;
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
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", emailConfirmed=" + emailConfirmed +
                '}';
    }
}