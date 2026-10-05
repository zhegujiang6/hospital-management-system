package com.example.auth;


import com.example.common.Result;
import com.example.security.LoginUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO loginVO = authService.login(request);
        return Result.success(loginVO);
    }

    /**
     * 返回后端当前认证通过的用户身份。
     */
    @GetMapping("/me")
    public Result<CurrentUserVO> getCurrentUser(
            @AuthenticationPrincipal
            LoginUser loginUser) {

        CurrentUserVO currentUser =
                new CurrentUserVO(
                        loginUser.getUserId(),
                        loginUser.getUsername(),
                        loginUser.getRealName(),
                        loginUser.getRole(),
                        loginUser.getDoctorId(),
                        loginUser.getPatientId()
                );

        return Result.success(currentUser);
    }

}
