package com.openlinkhub.rag.admin.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class SystemDtos {

    private SystemDtos() {
    }

    public static final class UserView {
        private final Long id;
        private final String username;
        private final String displayName;
        private final Long deptId;
        private final String deptName;
        private final String email;
        private final String phone;
        private final short status;
        private final List<Long> roleIds;
        private final List<String> roleNames;

        public UserView(
                Long id,
                String username,
                String displayName,
                Long deptId,
                String deptName,
                String email,
                String phone,
                short status,
                List<Long> roleIds,
                List<String> roleNames
        ) {
            this.id = id;
            this.username = username;
            this.displayName = displayName;
            this.deptId = deptId;
            this.deptName = deptName;
            this.email = email;
            this.phone = phone;
            this.status = status;
            this.roleIds = roleIds;
            this.roleNames = roleNames;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Long getDeptId() {
            return deptId;
        }

        public String getDeptName() {
            return deptName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public short getStatus() {
            return status;
        }

        public List<Long> getRoleIds() {
            return roleIds;
        }

        public List<String> getRoleNames() {
            return roleNames;
        }
    }

    public static final class UserRequest {
        @NotBlank
        private String username;
        private String password;
        @NotBlank
        private String displayName;
        private Long deptId;
        private String email;
        private String phone;
        @NotNull
        private Short status = 1;
        private List<Long> roleIds;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Long getDeptId() {
            return deptId;
        }

        public void setDeptId(Long deptId) {
            this.deptId = deptId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Short getStatus() {
            return status;
        }

        public void setStatus(Short status) {
            this.status = status;
        }

        public List<Long> getRoleIds() {
            return roleIds;
        }

        public void setRoleIds(List<Long> roleIds) {
            this.roleIds = roleIds;
        }
    }

    public static final class RoleView {
        private final Long id;
        private final String code;
        private final String name;
        private final short status;
        private final String remark;
        private final List<Long> menuIds;

        public RoleView(Long id, String code, String name, short status, String remark, List<Long> menuIds) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.status = status;
            this.remark = remark;
            this.menuIds = menuIds;
        }

        public Long getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public short getStatus() {
            return status;
        }

        public String getRemark() {
            return remark;
        }

        public List<Long> getMenuIds() {
            return menuIds;
        }
    }

    public static final class RoleOptionView {
        private final Long id;
        private final String code;
        private final String name;

        public RoleOptionView(Long id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    public static final class RoleRequest {
        @NotBlank
        private String code;
        @NotBlank
        private String name;
        @NotNull
        private Short status = 1;
        private String remark;
        private List<Long> menuIds;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Short getStatus() {
            return status;
        }

        public void setStatus(Short status) {
            this.status = status;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public List<Long> getMenuIds() {
            return menuIds;
        }

        public void setMenuIds(List<Long> menuIds) {
            this.menuIds = menuIds;
        }
    }

    public static final class MenuRequest {
        private Long parentId;
        @NotBlank
        private String name;
        private String path;
        private String permission;
        @NotBlank
        private String menuType = "C";
        private String icon;
        private int sortOrder;
        private boolean visible = true;
        @NotNull
        private Short status = 1;

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

        public String getMenuType() {
            return menuType;
        }

        public void setMenuType(String menuType) {
            this.menuType = menuType;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public Short getStatus() {
            return status;
        }

        public void setStatus(Short status) {
            this.status = status;
        }
    }

    public static final class DeptView {
        private final Long id;
        private final Long parentId;
        private final String name;
        private final String code;
        private final int sortOrder;
        private final short status;
        private final List<DeptView> children;

        public DeptView(Long id, Long parentId, String name, String code, int sortOrder, short status, List<DeptView> children) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
            this.code = code;
            this.sortOrder = sortOrder;
            this.status = status;
            this.children = children;
        }

        public Long getId() {
            return id;
        }

        public Long getParentId() {
            return parentId;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        public short getStatus() {
            return status;
        }

        public List<DeptView> getChildren() {
            return children;
        }
    }

    public static final class DeptRequest {
        private Long parentId;
        @NotBlank
        private String name;
        @NotBlank
        private String code;
        private int sortOrder;
        @NotNull
        private Short status = 1;

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
        }

        public Short getStatus() {
            return status;
        }

        public void setStatus(Short status) {
            this.status = status;
        }
    }
}
