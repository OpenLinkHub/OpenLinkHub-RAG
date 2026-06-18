package com.openlinkhub.rag.admin.auth;

import com.openlinkhub.rag.admin.common.ApiResponse;
import com.openlinkhub.rag.admin.system.dto.MenuTreeNode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfile> me() {
        return ApiResponse.ok(authService.currentProfile());
    }

    @GetMapping("/roles")
    public ApiResponse<Collection<Role>> roles() {
        return ApiResponse.ok(authService.roles());
    }

    @GetMapping("/menus")
    public ApiResponse<List<MenuTreeNode>> menus() {
        return ApiResponse.ok(authService.currentMenus());
    }
}
