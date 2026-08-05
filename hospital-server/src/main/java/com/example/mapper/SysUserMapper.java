package com.example.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.SysUser;
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
                status,
                create_time,
                update_time
            FROM sys_user
            WHERE username = #{username}
            LIMIT 1
            """)
    SysUser selectByUsername(@Param("username") String username);


}
