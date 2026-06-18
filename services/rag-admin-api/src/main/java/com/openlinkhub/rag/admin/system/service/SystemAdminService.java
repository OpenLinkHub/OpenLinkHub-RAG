package com.openlinkhub.rag.admin.system.service;

import com.openlinkhub.rag.admin.auth.AuthService;
import com.openlinkhub.rag.admin.common.AdminException;
import com.openlinkhub.rag.admin.common.PageResult;
import com.openlinkhub.rag.admin.common.StringUtils;
import com.openlinkhub.rag.admin.system.dto.MenuTreeNode;
import com.openlinkhub.rag.admin.system.dto.SystemDtos;
import com.openlinkhub.rag.admin.system.entity.SysDeptEntity;
import com.openlinkhub.rag.admin.system.entity.SysMenuEntity;
import com.openlinkhub.rag.admin.system.entity.SysRoleEntity;
import com.openlinkhub.rag.admin.system.entity.SysUserEntity;
import com.openlinkhub.rag.admin.system.repository.SysDeptRepository;
import com.openlinkhub.rag.admin.system.repository.SysMenuRepository;
import com.openlinkhub.rag.admin.system.repository.SysRoleRepository;
import com.openlinkhub.rag.admin.system.repository.SysUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SystemAdminService {

    private final AuthService authService;
    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysMenuRepository menuRepository;
    private final SysDeptRepository deptRepository;
    private final PasswordEncoder passwordEncoder;

    public SystemAdminService(
            AuthService authService,
            SysUserRepository userRepository,
            SysRoleRepository roleRepository,
            SysMenuRepository menuRepository,
            SysDeptRepository deptRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.deptRepository = deptRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResult<SystemDtos.UserView> listUsers(int page, int pageSize, String keyword) {
        authService.requireAuthority("system:user:list");
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        Page<SysUserEntity> result = userRepository.findAll(
                RbacSpecifications.userKeyword(keyword),
                PageRequest.of(safePage - 1, safeSize, Sort.by("id").ascending())
        );
        List<SystemDtos.UserView> items = result.getContent().stream()
                .map(this::toUserView)
                .collect(Collectors.toList());
        return new PageResult<>(items, result.getTotalElements(), safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public List<SystemDtos.UserView> listUsers() {
        return listUsers(1, Integer.MAX_VALUE, null).getItems();
    }

    @Transactional
    public SystemDtos.UserView createUser(SystemDtos.UserRequest request) {
        authService.requireAuthority("system:user:create");
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Username already exists.");
        }
        if (StringUtils.isBlank(request.getPassword())) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Password is required.");
        }
        SysUserEntity entity = new SysUserEntity();
        applyUser(entity, request, true);
        return toUserView(userRepository.save(entity));
    }

    @Transactional
    public SystemDtos.UserView updateUser(Long id, SystemDtos.UserRequest request) {
        authService.requireAuthority("system:user:update");
        SysUserEntity entity = userRepository.findWithRolesById(id)
                .orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND, "User not found."));
        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Username already exists.");
        }
        applyUser(entity, request, false);
        return toUserView(userRepository.save(entity));
    }

    @Transactional
    public void deleteUser(Long id) {
        authService.requireAuthority("system:user:delete");
        SysUserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND, "User not found."));
        if ("admin".equalsIgnoreCase(entity.getUsername())) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Cannot delete built-in admin user.");
        }
        userRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public PageResult<SystemDtos.RoleView> listRoles(int page, int pageSize, String keyword) {
        authService.requireAuthority("system:role:list");
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        Page<SysRoleEntity> result = roleRepository.findAll(
                RbacSpecifications.roleKeyword(keyword),
                PageRequest.of(safePage - 1, safeSize, Sort.by("id").ascending())
        );
        List<SystemDtos.RoleView> items = result.getContent().stream()
                .map(this::toRoleView)
                .collect(Collectors.toList());
        return new PageResult<>(items, result.getTotalElements(), safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public List<SystemDtos.RoleOptionView> listRoleOptions() {
        authService.requireAuthority("system:user:list");
        return roleRepository.findAllByOrderByIdAsc().stream()
                .map(role -> new SystemDtos.RoleOptionView(role.getId(), role.getCode(), role.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public SystemDtos.RoleView createRole(SystemDtos.RoleRequest request) {
        authService.requireAuthority("system:role:create");
        if (roleRepository.findByCode(request.getCode()).isPresent()) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Role code already exists.");
        }
        SysRoleEntity entity = new SysRoleEntity();
        applyRole(entity, request);
        return toRoleView(roleRepository.save(entity));
    }

    @Transactional
    public SystemDtos.RoleView updateRole(Long id, SystemDtos.RoleRequest request) {
        authService.requireAuthority("system:role:update");
        SysRoleEntity entity = roleRepository.findWithMenusById(id)
                .orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND, "Role not found."));
        if ("SUPER_ADMIN".equals(entity.getCode()) && !"SUPER_ADMIN".equals(request.getCode())) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Cannot rename SUPER_ADMIN role.");
        }
        if (roleRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Role code already exists.");
        }
        applyRole(entity, request);
        return toRoleView(roleRepository.save(entity));
    }

    @Transactional
    public void deleteRole(Long id) {
        authService.requireAuthority("system:role:delete");
        SysRoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND, "Role not found."));
        if ("SUPER_ADMIN".equals(entity.getCode())) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Cannot delete SUPER_ADMIN role.");
        }
        roleRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<MenuTreeNode> listMenuTree() {
        authService.requireAuthority("system:menu:list");
        return MenuTreeBuilder.build(menuRepository.findAllByOrderBySortOrderAscIdAsc(), true);
    }

    @Transactional
    public MenuTreeNode createMenu(SystemDtos.MenuRequest request) {
        authService.requireAuthority("system:menu:create");
        SysMenuEntity entity = new SysMenuEntity();
        applyMenu(entity, request);
        return MenuTreeBuilder.build(List.of(menuRepository.save(entity)), true).get(0);
    }

    @Transactional
    public MenuTreeNode updateMenu(Long id, SystemDtos.MenuRequest request) {
        authService.requireAuthority("system:menu:update");
        SysMenuEntity entity = menuRepository.findById(id)
                .orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND, "Menu not found."));
        if (Objects.equals(id, request.getParentId())) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Menu cannot be its own parent.");
        }
        applyMenu(entity, request);
        return MenuTreeBuilder.build(List.of(menuRepository.save(entity)), true).get(0);
    }

    @Transactional
    public void deleteMenu(Long id) {
        authService.requireAuthority("system:menu:delete");
        SysMenuEntity entity = menuRepository.findById(id)
                .orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND, "Menu not found."));
        if (menuRepository.existsByParentId(id)) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Delete child menus first.");
        }
        menuRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<SystemDtos.DeptView> listDeptTree() {
        authService.requireAuthority("system:dept:list");
        return buildDeptTree(deptRepository.findAllByOrderBySortOrderAscIdAsc());
    }

    @Transactional
    public SystemDtos.DeptView createDept(SystemDtos.DeptRequest request) {
        authService.requireAuthority("system:dept:create");
        if (deptRepository.findByCode(request.getCode()).isPresent()) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Department code already exists.");
        }
        SysDeptEntity entity = new SysDeptEntity();
        applyDept(entity, request);
        return toDeptView(deptRepository.save(entity), List.of());
    }

    @Transactional
    public SystemDtos.DeptView updateDept(Long id, SystemDtos.DeptRequest request) {
        authService.requireAuthority("system:dept:update");
        SysDeptEntity entity = deptRepository.findById(id)
                .orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND, "Department not found."));
        if (deptRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Department code already exists.");
        }
        if (Objects.equals(id, request.getParentId())) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Department cannot be its own parent.");
        }
        applyDept(entity, request);
        return toDeptView(deptRepository.save(entity), List.of());
    }

    @Transactional
    public void deleteDept(Long id) {
        authService.requireAuthority("system:dept:delete");
        if (id == 1L) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Cannot delete root department.");
        }
        SysDeptEntity entity = deptRepository.findById(id)
                .orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND, "Department not found."));
        if (deptRepository.existsByParentId(id)) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Delete child departments first.");
        }
        deptRepository.delete(entity);
    }

    private void applyUser(SysUserEntity entity, SystemDtos.UserRequest request, boolean creating) {
        entity.setUsername(request.getUsername());
        entity.setDisplayName(request.getDisplayName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setStatus(request.getStatus());
        if (!StringUtils.isBlank(request.getPassword())) {
            entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        } else if (creating) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Password is required.");
        }
        if (request.getDeptId() != null) {
            entity.setDept(deptRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new AdminException(HttpStatus.BAD_REQUEST, "Department not found.")));
        } else {
            entity.setDept(null);
        }
        entity.setRoles(resolveRoles(request.getRoleIds()));
    }

    private Set<SysRoleEntity> resolveRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        List<SysRoleEntity> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "One or more roles not found.");
        }
        return new HashSet<>(roles);
    }

    private void applyRole(SysRoleEntity entity, SystemDtos.RoleRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
        if (request.getMenuIds() != null) {
            List<SysMenuEntity> menus = menuRepository.findAllById(request.getMenuIds());
            if (menus.size() != request.getMenuIds().size()) {
                throw new AdminException(HttpStatus.BAD_REQUEST, "One or more menus not found.");
            }
            entity.setMenus(new HashSet<>(menus));
        }
    }

    private void applyMenu(SysMenuEntity entity, SystemDtos.MenuRequest request) {
        entity.setParentId(request.getParentId());
        entity.setName(request.getName());
        entity.setPath(request.getPath());
        entity.setPermission(request.getPermission());
        entity.setMenuType(request.getMenuType());
        entity.setIcon(request.getIcon());
        entity.setSortOrder(request.getSortOrder());
        entity.setVisible(request.isVisible());
        entity.setStatus(request.getStatus());
    }

    private void applyDept(SysDeptEntity entity, SystemDtos.DeptRequest request) {
        entity.setParentId(request.getParentId());
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setSortOrder(request.getSortOrder());
        entity.setStatus(request.getStatus());
    }

    private SystemDtos.UserView toUserView(SysUserEntity entity) {
        List<Long> roleIds = entity.getRoles().stream().map(SysRoleEntity::getId).sorted().collect(Collectors.toList());
        List<String> roleNames = entity.getRoles().stream().map(SysRoleEntity::getName).sorted().collect(Collectors.toList());
        return new SystemDtos.UserView(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getDept() == null ? null : entity.getDept().getId(),
                entity.getDept() == null ? null : entity.getDept().getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getStatus(),
                roleIds,
                roleNames
        );
    }

    private SystemDtos.RoleView toRoleView(SysRoleEntity entity) {
        List<Long> menuIds = entity.getMenus().stream().map(SysMenuEntity::getId).sorted().collect(Collectors.toList());
        return new SystemDtos.RoleView(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getStatus(),
                entity.getRemark(),
                menuIds
        );
    }

    private List<SystemDtos.DeptView> buildDeptTree(List<SysDeptEntity> depts) {
        Map<Long, List<SysDeptEntity>> childrenMap = depts.stream()
                .filter(dept -> dept.getParentId() != null)
                .collect(Collectors.groupingBy(SysDeptEntity::getParentId));
        return depts.stream()
                .filter(dept -> dept.getParentId() == null)
                .sorted(Comparator.comparingInt(SysDeptEntity::getSortOrder).thenComparing(SysDeptEntity::getId))
                .map(dept -> toDeptView(dept, buildChildren(dept.getId(), childrenMap)))
                .collect(Collectors.toList());
    }

    private List<SystemDtos.DeptView> buildChildren(Long parentId, Map<Long, List<SysDeptEntity>> childrenMap) {
        List<SysDeptEntity> children = childrenMap.getOrDefault(parentId, List.of());
        List<SystemDtos.DeptView> result = new ArrayList<>();
        children.stream()
                .sorted(Comparator.comparingInt(SysDeptEntity::getSortOrder).thenComparing(SysDeptEntity::getId))
                .forEach(child -> result.add(toDeptView(child, buildChildren(child.getId(), childrenMap))));
        return result;
    }

    private SystemDtos.DeptView toDeptView(SysDeptEntity entity, List<SystemDtos.DeptView> children) {
        return new SystemDtos.DeptView(
                entity.getId(),
                entity.getParentId(),
                entity.getName(),
                entity.getCode(),
                entity.getSortOrder(),
                entity.getStatus(),
                children
        );
    }
}
