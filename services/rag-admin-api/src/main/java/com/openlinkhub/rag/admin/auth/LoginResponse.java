package com.openlinkhub.rag.admin.auth;

public final class LoginResponse {

    private final String token;
    private final UserProfile user;

    public LoginResponse(String token, UserProfile user) {
        this.token = token;
        this.user = user;
    }

    public String token() {
        return token;
    }

    public String getToken() {
        return token;
    }

    public UserProfile user() {
        return user;
    }

    public UserProfile getUser() {
        return user;
    }
}
