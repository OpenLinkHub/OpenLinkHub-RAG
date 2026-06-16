package com.openlinkhub.rag.admin.auth;

public final class AuthContext {

    private static final ThreadLocal<AdminUser> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AdminUser user) {
        CURRENT_USER.set(user);
    }

    public static AdminUser currentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
