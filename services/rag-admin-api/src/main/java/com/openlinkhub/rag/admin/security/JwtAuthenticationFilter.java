package com.openlinkhub.rag.admin.security;

import com.openlinkhub.rag.admin.auth.AdminUser;
import com.openlinkhub.rag.admin.auth.AuthContext;
import com.openlinkhub.rag.admin.common.AdminException;
import com.openlinkhub.rag.admin.common.StringUtils;
import com.openlinkhub.rag.admin.system.entity.SysUserEntity;
import com.openlinkhub.rag.admin.system.repository.SysUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, SysUserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (!StringUtils.isBlank(token)) {
                try {
                    Long userId = jwtTokenProvider.userIdFromToken(token);
                    SysUserEntity entity = userRepository.findWithRolesById(userId)
                            .orElseThrow(() -> new AdminException(HttpStatus.UNAUTHORIZED, "Invalid authorization token."));
                    if (entity.getStatus() != 1) {
                        throw new AdminException(HttpStatus.UNAUTHORIZED, "User is disabled.");
                    }
                    AdminUser adminUser = RbacSupport.toAdminUser(entity);
                    AuthContext.set(adminUser);
                    var authorities = adminUser.permissionStrings().stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(adminUser, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (AdminException exception) {
                    writeError(response, exception.status().value(), exception.getMessage());
                    return;
                } catch (Exception exception) {
                    writeError(response, HttpStatus.UNAUTHORIZED.value(), "Invalid authorization token.");
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return authorization;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String body = "{\"success\":false,\"message\":\"" + message.replace("\"", "\\\"") + "\",\"data\":null}";
        response.getWriter().write(body);
    }
}
