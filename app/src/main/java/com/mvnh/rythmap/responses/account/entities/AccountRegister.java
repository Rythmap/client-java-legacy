package com.mvnh.rythmap.responses.account.entities;

public class AccountRegister {
    private String nickname;
    private String password;
    private String email;

    public AccountRegister(String nickname, String password, String email) {
        this.nickname = nickname;
        this.password = password;
        this.email = email;
    }
}
