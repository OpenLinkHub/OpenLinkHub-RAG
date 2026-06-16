package com.openlinkhub.rag.admin.auth;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.Set;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class Role {

    private final String code;
    private final String name;
    private final Set<Permission> permissions;

    public Role(String code, String name, Set<Permission> permissions) {
        this.code = code;
        this.name = name;
        this.permissions = permissions;
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public Set<Permission> permissions() {
        return permissions;
    }
}
