package com.example.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final CustomUserDetailService
            customUserDetailService;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            CustomUserDetailService customUserDetailService) {

        this.jwtUtil = jwtUtil;
        this.customUserDetailService =
                customUserDetailService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 读取前端请求头
        String authorizationHeader =
                request.getHeader("Authorization");

        // 2. 没有JWT就直接继续访问后面的过滤器
        if (!StringUtils.hasText(authorizationHeader)
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 3. 删除前面的“Bearer ”
        String token =
                authorizationHeader.substring(7);

        try {
            // 4. 验证JWT，并取出用户名
            String username =
                    jwtUtil.getUsername(token);

            boolean notAuthenticated =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null;

            if (StringUtils.hasText(username)
                    && notAuthenticated) {

                // 5. 根据用户名重新查询数据库
                LoginUser loginUser =
                        (LoginUser) customUserDetailService
                                .loadUserByUsername(username);

                if (loginUser.isEnabled()) {
                    // 6. 创建已经认证成功的身份对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    loginUser,
                                    null,
                                    loginUser.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // 7. 保存当前请求的登录身份
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (JwtException
                 | AuthenticationException
                 | IllegalArgumentException exception) {

            // JWT错误、过期或用户不存在，清空身份
            SecurityContextHolder.clearContext();
        }

        // 8. 继续执行后面的过滤器和Controller
        filterChain.doFilter(request, response);
    }
}
