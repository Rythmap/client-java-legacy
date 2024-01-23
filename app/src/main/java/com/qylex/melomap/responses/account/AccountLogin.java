package com.qylex.melomap.responses.account;

public class AccountLogin {
    private String username;
    private String password;

    public AccountLogin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
