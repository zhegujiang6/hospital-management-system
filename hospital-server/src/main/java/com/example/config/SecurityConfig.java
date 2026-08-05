package com.example.config;

import com.example.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security安全配置类
 * 配置认证、授权规则以及过滤器链
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤链
     * 定义CSRF策略、会话管理、请求授权规则以及JWT过滤器
     *
     * @param http                  Spring Security的Http安全配置对象
     * @param jwtAuthenticationFilter JWT认证过滤器
     * @return 构建完成的安全过滤链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http
                // 禁用CSRF保护（前后端分离架构，使用JWT令牌无需CSRF）
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用表单登录
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用HTTP基础认证
                .httpBasic(AbstractHttpConfigurer::disable)

                // 配置会话管理为无状态（使用JWT令牌，不依赖Session）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // 配置请求授权规则
                .authorizeHttpRequests(authorize ->
                        authorize
                                // 登录接口允许匿名访问
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/auth/login"
                                ).permitAll()

                                // Swagger接口文档允许匿名访问
                                .requestMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                ).permitAll()

                                // Spring默认的错误处理地址允许访问
                                .requestMatchers("/error")
                                .permitAll()

                                // 科室、医生、患者、排班、挂号、支付管理接口仅管理员可访问
                                .requestMatchers(
                                        "/departments/**",
                                        "/doctors/**",
                                        "/patients/**",
                                        "/schedules/**",
                                        "/registrations/**",
                                        "/payments/**"
                                ).hasRole("ADMIN")

                                // 其余所有接口都需要认证后才能访问
                                .anyRequest()
                                .authenticated()

                                // 接口文档允许匿名访问

                )


                // 将JWT认证过滤器添加到用户名密码认证过滤器之前
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * 配置认证管理器
     * 用于处理用户认证请求（如登录时验证用户名密码）
     *
     * @param authenticationConfiguration 认证配置对象
     * @return 认证管理器实例
     * @throws Exception 获取认证管理器时可能抛出的异常
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {

        return authenticationConfiguration
                .getAuthenticationManager();
    }

    /**
     * 配置密码编码器
     * 使用BCrypt算法对密码进行加密存储和验证
     *
     * @return BCrypt密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}