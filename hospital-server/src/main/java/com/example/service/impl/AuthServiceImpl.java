package com.example.service.impl;


import com.example.dto.LoginRequest;
import com.example.exception.BusinessException;
import com.example.security.JwtUtil;
import com.example.security.LoginUser;
import com.example.service.AuthService;
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


    // Spring 创建 AuthServiceImpl 时，把 AuthenticationManager 和 JwtUtil 传进来。
    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil) {
        // 保存认证管理器，login 方法中会使用它检查账号密码。
        this.authenticationManager = authenticationManager;
        // 保存 JWT 工具对象，认证成功后会用它生成 token。
        this.jwtUtil = jwtUtil;
    }


    // 对应 AuthService 中的 login；request 是 AuthController 接收到的登录 DTO。
    @Override
    public LoginVO login(LoginRequest request) {
        // try 包住认证过程，下面会分别处理账号停用和账号密码错误。
        try {
            // 从 LoginRequest 取得用户名和密码，封装成 Spring Security 认识的认证对象。
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword());

            // 把认证对象交给 AuthenticationManager；它会查询用户并使用密码加密器校验密码。
            Authentication authentication = authenticationManager.authenticate(authenticationToken);


            // 认证成功后，从 Authentication 中取得项目自定义的 LoginUser。
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();

            // 把登录用户交给 JwtUtil，生成前端后续请求要放在 Authorization 中的 JWT。
            String token = jwtUtil.generateToken(loginUser);


            // 把 token 和用户基本信息封装成 LoginVO，最后由 AuthController 返回给前端。
            return new LoginVO(token, loginUser.getUserId(), loginUser.getUsername(), loginUser.getRealName(), loginUser.getRole(), loginUser.getDoctorId());
        // AuthenticationManager 发现数据库中的账号已停用时，会抛出 DisabledException。
        } catch (
                DisabledException e) {
            // 转成项目统一的 BusinessException，让前端看到容易理解的提示。
            throw new BusinessException("账号已停用");
        // 用户不存在、密码错误等认证失败会进入这个 catch。
        } catch (
                AuthenticationException e) {
            // 不向前端暴露具体安全细节，统一提示用户名或密码错误。
            throw new BusinessException("用户名或密码错误");
        }

    }
}
