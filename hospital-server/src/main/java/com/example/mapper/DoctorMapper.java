package com.example.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Doctor;
import com.example.vo.DoctorVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    @Select("""
            SELECT
                d.id,
                d.doctor_no AS doctorNo,
                d.department_id AS departmentId,
                dp.name AS departmentName,
                d.name,
                d.gender,
                d.title,
                d.phone,
                d.specialty,
                d.introduction,
                d.status,
                d.create_time AS createTime,
                d.update_time AS updateTime
            FROM doctor d
            INNER JOIN department dp
                ON d.department_id = dp.id
            ORDER BY d.id DESC
            """)
    List<DoctorVO> selectDoctorList();


    @Select("""
        SELECT
            d.id,
            d.doctor_no AS doctorNo,
            d.department_id AS departmentId,
            dp.name AS departmentName,
            d.name,
            d.gender,
            d.title,
            d.phone,
            d.specialty,
            d.introduction,
            d.status,
            d.create_time AS createTime,
            d.update_time AS updateTime
        FROM doctor d
        INNER JOIN department dp
            ON d.department_id = dp.id
        WHERE d.id = #{id}
        """)
    DoctorVO selectDoctorDetail(@Param("id") Long id);
}
