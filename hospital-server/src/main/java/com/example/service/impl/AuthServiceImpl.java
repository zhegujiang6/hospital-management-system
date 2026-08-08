package com.example.service.impl;


import com.example.dto.LoginRequest;
import com.example.exception.BusinessException;
import com.example.security.JwtUtil;
import com.example.security.LoginUser;
import com.example.service.AuthService;
import com.example.service.LoginAttemptService;
import com.example.vo.LoginVO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
/*
 * 登录认证业务实现类。
 * AuthController 把前端提交的 LoginRequest 交给 AuthService，最终执行这里的 login。
 * 它不直接查询用户表，而是把账号密码交给 Spring Security 的 AuthenticationManager。
 * 认证成功后，再使用 JwtUtil 生成前端后续请求需要携带的 JWT。
 */
public class AuthServiceImpl implements AuthService {

    // Spring Security 的认证入口，负责调用后面的用户查询和密码校验逻辑。
    private final AuthenticationManager authenticationManager;
    // 项目中的 JWT 工具类，负责生成登录令牌。
    private final JwtUtil jwtUtil;

    private final LoginAttemptService loginAttemptService;


    // Spring 创建 AuthServiceImpl 时，把 AuthenticationManager 和 JwtUtil 传进来。
    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            LoginAttemptService loginAttemptService) {
        // 保存认证管理器，login 方法中会使用它检查账号密码。
        this.authenticationManager = authenticationManager;
        // 保存 JWT 工具对象，认证成功后会用它生成 token。
        this.jwtUtil = jwtUtil;
        this.loginAttemptService = loginAttemptService;
    }


    // 对应 AuthService 中的 login；request 是 AuthController 接收到的登录 DTO。
    @Override
    public LoginVO login(LoginRequest request) {

        // 统一整理用户名，后面认证和Redis都使用同一个值。
        String username =
                request.getUsername().trim();

        // 正式验证密码之前，先查看Redis里的失败次数。
        if (loginAttemptService.isBlocked(username)) {
            throw new BusinessException(
                    "登录失败次数过多，请稍后再试"
            );
        }

        try {
            // 把前端传来的用户名、密码包装成Security认证对象。
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            request.getPassword()
                    );

            // 交给Spring Security查询账号并校验密码。
            Authentication authentication =
                    authenticationManager.authenticate(
                            authenticationToken
                    );

            // 认证成功后取得当前登录用户。
            LoginUser loginUser =
                    (LoginUser) authentication.getPrincipal();

            // 根据登录用户生成JWT。
            String token =
                    jwtUtil.generateToken(loginUser);

            // 密码正确，删除这个账号以前的失败次数。
            loginAttemptService.clearFailures(username);

            // 返回token和用户基本信息。
            return new LoginVO(
                    token,
                    loginUser.getUserId(),
                    loginUser.getUsername(),
                    loginUser.getRealName(),
                    loginUser.getRole(),
                    loginUser.getDoctorId()
            );

        } catch (DisabledException e) {

            // 账号停用不属于密码连续输入错误，
            // 所以这里不增加Redis失败次数。
            throw new BusinessException(
                    "账号已停用"
            );

        } catch (AuthenticationException e) {

            // 用户不存在或密码错误时，
            // Redis里的失败次数原子加一。
            int remainingAttempts =
                    loginAttemptService.recordFailure(
                            username
                    );

            // 返回0说明已经达到5次。
            if (remainingAttempts == 0) {
                throw new BusinessException(
                        "登录失败次数过多，请稍后再试"
                );
            }

            throw new BusinessException(
                    "用户名或密码错误，还可以尝试"
                            + remainingAttempts
                            + "次"
            );
        }
    }

}
