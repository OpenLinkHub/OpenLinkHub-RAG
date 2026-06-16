package com.openlinkhub.rag.admin.auth;

import java.util.List;

public final class UserProfile {

    private final String id;
    private final String username;
    private final String displayName;
    private final List<String> roles;
    private final List<String> permissions;

    public UserProfile(String id, String username, String displayName, List<String> roles, List<String> permissions) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.roles = roles;
        this.permissions = permissions;
    }

    public String id() {
        return id;
    }

    public String getId() {
        return id;
    }

    public String username() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public String displayName() {
        return displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> roles() {
        return roles;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> permissions() {
        return permissions;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}
