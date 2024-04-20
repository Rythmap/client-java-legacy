package com.mvnh.rythmap.responses.account.entities;

public class AccountLogin {
    private String nickname;
    private String password;

    public AccountLogin(String nickname, String password) {
        this.nickname = nickname;
        this.password = password;
    }
}