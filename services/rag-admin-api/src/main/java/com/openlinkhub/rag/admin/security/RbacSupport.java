package com.openlinkhub.rag.admin.security;

import com.openlinkhub.rag.admin.auth.AdminUser;
import com.openlinkhub.rag.admin.auth.Permission;
import com.openlinkhub.rag.admin.auth.Role;
import com.openlinkhub.rag.admin.system.entity.SysMenuEntity;
import com.openlinkhub.rag.admin.system.entity.SysRoleEntity;
import com.openlinkhub.rag.admin.system.entity.SysUserEntity;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class RbacSupport {

    private static final Map<Permission, String> PERMISSION_PREFIX = Map.ofEntries(
            Map.entry(Permission.USER_MANAGE, "system:user:"),
            Map.entry(Permission.ROLE_MANAGE, "system:role:"),
            Map.entry(Permission.KB_CREATE, "kb:"),
            Map.entry(Permission.KB_UPDATE, "kb:"),
            Map.entry(Permission.KB_AUTHORIZE, "kb:"),
            Map.entry(Permission.ENDPOINT_MANAGE, "system:endpoint:"),
            Map.entry(Permission.DOC_UPLOAD, "doc:upload"),
            Map.entry(Permission.DOC_DELETE, "doc:delete"),
            Map.entry(Permission.DOC_REPROCESS, "doc:reprocess"),
            Map.entry(Permission.QUERY_EXECUTE, "query:execute"),
            Map.entry(Permission.MODEL_MANAGE, "model:manage"),
            Map.entry(Permission.AUDIT_VIEW, "audit:")
    );

    private RbacSupport() {
    }

    public static AdminUser toAdminUser(SysUserEntity entity) {
        Set<SysRoleEntity> roleEntities = entity.getRoles();
        boolean superAdmin = roleEntities.stream().anyMatch(role -> "SUPER_ADMIN".equals(role.getCode()));
        Set<String> permissionStrings = collectPermissions(roleEntities, superAdmin);
        Set<Role> roles = roleEntities.stream()
                .map(role -> new Role(role.getCode(), role.getName(), mapLegacyPermissions(permissionStrings, superAdmin)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new AdminUser(
                String.valueOf(entity.getId()),
                entity.getUsername(),
                null,
                entity.getDisplayName(),
                roles,
                permissionStrings,
                superAdmin
        );
    }

    public static Set<String> collectPermissions(Set<SysRoleEntity> roles, boolean superAdmin) {
        if (superAdmin) {
            return Set.of("*");
        }
        Set<String> permissions = new LinkedHashSet<>();
        for (SysRoleEntity role : roles) {
            for (SysMenuEntity menu : role.getMenus()) {
                if (menu.getPermission() != null && !menu.getPermission().isBlank()) {
                    permissions.add(menu.getPermission());
                }
            }
        }
        return permissions;
    }

    public static boolean hasPermission(AdminUser user, Permission permission) {
        if (user == null) {
            return false;
        }
        if (user.superAdmin() || user.permissionStrings().contains("*")) {
            return true;
        }
        String prefix = PERMISSION_PREFIX.get(permission);
        if (prefix != null) {
            for (String granted : user.permissionStrings()) {
                if (granted.equals(prefix) || granted.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }
        return user.permissionStrings().contains(permission.name().toLowerCase());
    }

    public static boolean hasAuthority(AdminUser user, String authority) {
        if (user == null) {
            return false;
        }
        if (user.superAdmin() || user.permissionStrings().contains("*")) {
            return true;
        }
        if (user.permissionStrings().contains(authority)) {
            return true;
        }
        int idx = authority.lastIndexOf(':');
        if (idx > 0) {
            String prefix = authority.substring(0, idx + 1);
            for (String granted : user.permissionStrings()) {
                if (granted.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Set<Permission> mapLegacyPermissions(Set<String> permissionStrings, boolean superAdmin) {
        if (superAdmin) {
            return EnumSet.allOf(Permission.class);
        }
        Set<Permission> result = EnumSet.noneOf(Permission.class);
        for (Permission permission : Permission.values()) {
            if (hasPermission(new AdminUser("0", "x", null, "x", Set.of(), permissionStrings, false), permission)) {
                result.add(permission);
            }
        }
        return result;
    }
}
