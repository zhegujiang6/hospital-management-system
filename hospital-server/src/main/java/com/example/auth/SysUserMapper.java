package com.example.auth;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper  extends BaseMapper<SysUser> {

    @Select("""
            SELECT
                id,
                username,
                password,
                real_name,
                role,
                doctor_id,
                patient_id,
                status,
                create_time,
                update_time
            FROM sys_user
            WHERE username = #{username}
            LIMIT 1
            """)
    SysUser selectByUsername(@Param("username") String username);


}
