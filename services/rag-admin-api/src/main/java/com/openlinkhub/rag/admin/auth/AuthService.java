package com.openlinkhub.rag.admin.auth;

import com.openlinkhub.rag.admin.common.AdminException;
import com.openlinkhub.rag.admin.common.StringUtils;
import com.openlinkhub.rag.admin.config.RagAdminProperties;
import com.openlinkhub.rag.admin.security.JwtTokenProvider;
import com.openlinkhub.rag.admin.security.RbacSupport;
import com.openlinkhub.rag.admin.system.dto.MenuTreeNode;
import com.openlinkhub.rag.admin.system.entity.SysDeptEntity;
import com.openlinkhub.rag.admin.system.entity.SysMenuEntity;
import com.openlinkhub.rag.admin.system.entity.SysRoleEntity;
import com.openlinkhub.rag.admin.system.entity.SysUserEntity;
import com.openlinkhub.rag.admin.system.repository.SysDeptRepository;
import com.openlinkhub.rag.admin.system.repository.SysMenuRepository;
import com.openlinkhub.rag.admin.system.repository.SysRoleRepository;
import com.openlinkhub.rag.admin.system.repository.SysUserRepository;
import com.openlinkhub.rag.admin.system.service.MenuTreeBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysMenuRepository menuRepository;
    private final SysDeptRepository deptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RagAdminProperties properties;

    public AuthService(
            SysUserRepository userRepository,
            SysRoleRepository roleRepository,
            SysMenuRepository menuRepository,
            SysDeptRepository deptRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RagAdminProperties properties
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.deptRepository = deptRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        SysUserEntity user = userRepository.findWithRolesByUsername(request.username())
                .orElseThrow(() -> new AdminException(HttpStatus.UNAUTHORIZED, "Invalid username or password."));
        if (user.getStatus() != 1) {
            throw new AdminException(HttpStatus.UNAUTHORIZED, "User is disabled.");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AdminException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }
        AdminUser adminUser = RbacSupport.toAdminUser(user);
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername());
        return new LoginResponse(token, toProfile(adminUser));
    }

    @Transactional(readOnly = true)
    public UserProfile currentProfile() {
        AdminUser user = AuthContext.currentUser();
        if (user == null) {
            throw new AdminException(HttpStatus.UNAUTHORIZED, "Missing authorization token.");
        }
        return toProfile(user);
    }

    @Transactional(readOnly = true)
    public List<MenuTreeNode> currentMenus() {
        AdminUser user = requireUser();
        if (user.superAdmin()) {
            return MenuTreeBuilder.build(menuRepository.findAllByOrderBySortOrderAscIdAsc(), true);
        }
        Set<Long> allowedMenuIds = new HashSet<>();
        SysUserEntity entity = userRepository.findWithRolesById(Long.parseLong(user.id()))
                .orElseThrow(() -> new AdminException(HttpStatus.UNAUTHORIZED, "User not found."));
        for (SysRoleEntity role : entity.getRoles()) {
            for (SysMenuEntity menu : role.getMenus()) {
                allowedMenuIds.add(menu.getId());
            }
        }
        List<SysMenuEntity> menus = menuRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .filter(menu -> allowedMenuIds.contains(menu.getId()))
                .collect(Collectors.toList());
        return MenuTreeBuilder.build(menus, false);
    }

    @Transactional(readOnly = true)
    public Collection<Role> roles() {
        return roleRepository.findAllByOrderByIdAsc().stream()
                .map(role -> new Role(role.getCode(), role.getName(), Set.of()))
                .collect(Collectors.toList());
    }

    public void require(Permission permission) {
        AdminUser user = requireUser();
        if (!RbacSupport.hasPermission(user, permission)) {
            throw new AdminException(HttpStatus.FORBIDDEN, "Permission denied: " + permission.name());
        }
    }

    public void requireAuthority(String authority) {
        AdminUser user = requireUser();
        if (!RbacSupport.hasAuthority(user, authority)) {
            throw new AdminException(HttpStatus.FORBIDDEN, "Permission denied: " + authority);
        }
    }

    @Transactional
    public void ensureSeedAdmin() {
        RagAdminProperties.SeedAdmin seed = properties.seedAdmin();
        if (StringUtils.isBlank(seed.username()) || StringUtils.isBlank(seed.password())) {
            return;
        }
        if (userRepository.existsByUsername(seed.username())) {
            return;
        }
        SysUserEntity admin = new SysUserEntity();
        admin.setUsername(seed.username());
        admin.setPasswordHash(passwordEncoder.encode(seed.password()));
        admin.setDisplayName(seed.displayName());
        admin.setStatus((short) 1);
        deptRepository.findById(1L).ifPresent(admin::setDept);
        SysRoleEntity superAdmin = roleRepository.findByCode("SUPER_ADMIN")
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role missing"));
        admin.setRoles(new HashSet<>(Set.of(superAdmin)));
        userRepository.save(admin);
    }

    private AdminUser requireUser() {
        AdminUser user = AuthContext.currentUser();
        if (user == null) {
            throw new AdminException(HttpStatus.UNAUTHORIZED, "Missing authorization token.");
        }
        return user;
    }

    private UserProfile toProfile(AdminUser user) {
        List<String> roleCodes = user.roles().stream()
                .map(Role::code)
                .sorted()
                .collect(Collectors.toList());
        List<String> permissionNames = user.permissionStrings().stream()
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
}
