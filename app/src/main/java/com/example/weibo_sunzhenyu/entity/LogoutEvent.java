package com.example.weibo_sunzhenyu.entity;

public class LogoutEvent {
    private boolean login;

    public LogoutEvent(boolean login) {
        this.login = login;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }
}
