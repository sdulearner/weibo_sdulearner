package com.example.weibo_sunzhenyu.entity;

public class LoginEvent {
    private boolean login;

    public LoginEvent(boolean login) {
        this.login = login;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }
}
