package com.openlinkhub.rag.admin.auth;

import com.openlinkhub.rag.admin.common.AdminException;
import com.openlinkhub.rag.admin.common.StringUtils;
import com.openlinkhub.rag.admin.config.RagAdminProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final Map<String, AdminUser> usersByUsername = new ConcurrentHashMap<String, AdminUser>();
    private final Map<String, AdminUser> usersByToken = new ConcurrentHashMap<String, AdminUser>();
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(RagAdminProperties properties) {
        Role superAdmin = new Role(
                "SUPER_ADMIN",
                "超级管理员",
                new HashSet<Permission>(Arrays.asList(Permission.values()))
        );
        RagAdminProperties.SeedAdmin seed = properties.seedAdmin();
        AdminUser admin = new AdminUser(
                "u-admin",
                seed.username(),
                seed.password(),
                seed.displayName(),
                Collections.singleton(superAdmin)
        );
        usersByUsername.put(admin.username(), admin);
    }

    public LoginResponse login(LoginRequest request) {
        AdminUser user = usersByUsername.get(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new AdminException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }
        String token = nextToken();
        usersByToken.put(token, user);
        return new LoginResponse(token, toProfile(user));
    }

    public AdminUser authenticate(String token) {
        if (StringUtils.isBlank(token)) {
            throw new AdminException(HttpStatus.UNAUTHORIZED, "Missing authorization token.");
        }
        AdminUser user = usersByToken.get(token);
        if (user == null) {
            throw new AdminException(HttpStatus.UNAUTHORIZED, "Invalid authorization token.");
        }
        return user;
    }

    public UserProfile currentProfile() {
        AdminUser user = AuthContext.currentUser();
        if (user == null) {
            throw new AdminException(HttpStatus.UNAUTHORIZED, "Missing authorization token.");
        }
        return toProfile(user);
    }

    public Collection<Role> roles() {
        Set<Role> roleSet = new HashSet<Role>();
        for (AdminUser user : usersByUsername.values()) {
            roleSet.addAll(user.roles());
        }
        return new ArrayList<Role>(roleSet);
    }

    public void require(Permission permission) {
        AdminUser user = AuthContext.currentUser();
        if (user == null || !user.permissions().contains(permission)) {
            throw new AdminException(HttpStatus.FORBIDDEN, "Permission denied: " + permission.name());
        }
    }

    private UserProfile toProfile(AdminUser user) {
        List<String> roleCodes = user.roles().stream()
                .map(Role::code)
                .collect(Collectors.toList());
        List<String> permissionNames = user.permissions().stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toList());
        return new UserProfile(
                user.id(),
                user.username(),
                user.displayName(),
                roleCodes,
                permissionNames
        );
    }

    private String nextToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
