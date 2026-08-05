package com.example.security;


import com.example.entity.SysUser;
import com.example.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private final SysUserMapper sysUserMapper;

    public CustomUserDetailService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = username.trim();
        SysUser sysUser = sysUserMapper.selectByUsername(normalizedUsername);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名或密码错误");

        }
        return new LoginUser(sysUser);
    }



}
