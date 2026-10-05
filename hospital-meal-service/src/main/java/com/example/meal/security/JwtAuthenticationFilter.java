package com.example.meal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 读取请求中的JWT，并建立当前请求的登录身份。
 */
@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    /**
     * 每个HTTP请求只执行一次JWT验证。
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (!StringUtils.hasText(authorizationHeader)
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorizationHeader.substring(7);

        try {
            Claims claims =
                    jwtUtil.parseToken(token);

            Long userId =
                    convertToLong(claims.get("userId"));

            Long patientId =
                    convertToLong(claims.get("patientId"));

            String username =
                    claims.getSubject();

            String role =
                    claims.get("role", String.class);

            boolean identityComplete =
                    userId != null
                            && StringUtils.hasText(username)
                            && StringUtils.hasText(role);

            boolean patientIdentityComplete =
                    !"PATIENT".equals(role)
                            || patientId != null;

            if (identityComplete
                    && patientIdentityComplete
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                MealLoginUser loginUser =
                        new MealLoginUser(
                                userId,
                                username,
                                role,
                                patientId
                        );

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                loginUser,
                                null,
                                List.of(authority)
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (JwtException
                 | IllegalArgumentException exception) {

            // JWT无效或已经过期，不建立登录身份。
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 将JWT中的数字安全转换成Long。
     *
     * JWT解析后，小数字有时是Integer，
     * 因此不能直接强制转换成Long。
     */
    private Long convertToLong(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.valueOf(value.toString());
    }
}