package com.openlinkhub.rag.admin.system;

import com.openlinkhub.rag.admin.common.ApiResponse;
import com.openlinkhub.rag.admin.common.PageResult;
import com.openlinkhub.rag.admin.system.dto.MenuTreeNode;
import com.openlinkhub.rag.admin.system.dto.SystemDtos;
import com.openlinkhub.rag.admin.system.service.SystemAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/system")
public class SystemAdminController {

    private final SystemAdminService systemAdminService;

    public SystemAdminController(SystemAdminService systemAdminService) {
        this.systemAdminService = systemAdminService;
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<SystemDtos.UserView>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(systemAdminService.listUsers(page, pageSize, keyword));
    }

    @PostMapping("/users")
    public ApiResponse<SystemDtos.UserView> createUser(@Valid @RequestBody SystemDtos.UserRequest request) {
        return ApiResponse.ok(systemAdminService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<SystemDtos.UserView> updateUser(@PathVariable Long id, @Valid @RequestBody SystemDtos.UserRequest request) {
        return ApiResponse.ok(systemAdminService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        systemAdminService.deleteUser(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/roles")
    public ApiResponse<PageResult<SystemDtos.RoleView>> roles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(systemAdminService.listRoles(page, pageSize, keyword));
    }

    @GetMapping("/roles/options")
    public ApiResponse<List<SystemDtos.RoleOptionView>> roleOptions() {
        return ApiResponse.ok(systemAdminService.listRoleOptions());
    }

    @PostMapping("/roles")
    public ApiResponse<SystemDtos.RoleView> createRole(@Valid @RequestBody SystemDtos.RoleRequest request) {
        return ApiResponse.ok(systemAdminService.createRole(request));
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<SystemDtos.RoleView> updateRole(@PathVariable Long id, @Valid @RequestBody SystemDtos.RoleRequest request) {
        return ApiResponse.ok(systemAdminService.updateRole(id, request));
    }

    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        systemAdminService.deleteRole(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/menus/tree")
    public ApiResponse<List<MenuTreeNode>> menuTree() {
        return ApiResponse.ok(systemAdminService.listMenuTree());
    }

    @PostMapping("/menus")
    public ApiResponse<MenuTreeNode> createMenu(@Valid @RequestBody SystemDtos.MenuRequest request) {
        return ApiResponse.ok(systemAdminService.createMenu(request));
    }

    @PutMapping("/menus/{id}")
    public ApiResponse<MenuTreeNode> updateMenu(@PathVariable Long id, @Valid @RequestBody SystemDtos.MenuRequest request) {
        return ApiResponse.ok(systemAdminService.updateMenu(id, request));
    }

    @DeleteMapping("/menus/{id}")
    public ApiResponse<Void> deleteMenu(@PathVariable Long id) {
        systemAdminService.deleteMenu(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/depts/tree")
    public ApiResponse<List<SystemDtos.DeptView>> deptTree() {
        return ApiResponse.ok(systemAdminService.listDeptTree());
    }

    @PostMapping("/depts")
    public ApiResponse<SystemDtos.DeptView> createDept(@Valid @RequestBody SystemDtos.DeptRequest request) {
        return ApiResponse.ok(systemAdminService.createDept(request));
    }

    @PutMapping("/depts/{id}")
    public ApiResponse<SystemDtos.DeptView> updateDept(@PathVariable Long id, @Valid @RequestBody SystemDtos.DeptRequest request) {
        return ApiResponse.ok(systemAdminService.updateDept(id, request));
    }

    @DeleteMapping("/depts/{id}")
    public ApiResponse<Void> deleteDept(@PathVariable Long id) {
        systemAdminService.deleteDept(id);
        return ApiResponse.ok(null);
    }
}
