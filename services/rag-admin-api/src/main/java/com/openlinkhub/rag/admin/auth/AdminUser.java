package com.openlinkhub.rag.admin.auth;

import java.util.HashSet;
import java.util.Set;

public final class AdminUser {

    private final String id;
    private final String username;
    private final String password;
    private final String displayName;
    private final Set<Role> roles;
    private final Set<String> permissionStrings;
    private final boolean superAdmin;

    public AdminUser(
            String id,
            String username,
            String password,
            String displayName,
            Set<Role> roles,
            Set<String> permissionStrings,
            boolean superAdmin
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.roles = roles;
        this.permissionStrings = permissionStrings == null ? Set.of() : Set.copyOf(permissionStrings);
        this.superAdmin = superAdmin;
    }

    public String id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String displayName() {
        return displayName;
    }

    public Set<Role> roles() {
        return roles;
    }

    public Set<String> permissionStrings() {
        return permissionStrings;
    }

    public boolean superAdmin() {
        return superAdmin;
    }

    public Set<Permission> permissions() {
        Set<Permission> result = new HashSet<Permission>();
        for (Role role : roles) {
            result.addAll(role.permissions());
        }
        return result;
    }
}
