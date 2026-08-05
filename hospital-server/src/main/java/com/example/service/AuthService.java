package com.example.service;


import com.example.dto.LoginRequest;
import com.example.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginRequest request);
}
