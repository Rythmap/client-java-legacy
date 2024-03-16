package com.mvnh.rythmap.responses.account;

public class AccountLogin {
    private String username;
    private String password;

    public AccountLogin(String username, String password) {
        this.username = username;
        this.password = password;
    }
}